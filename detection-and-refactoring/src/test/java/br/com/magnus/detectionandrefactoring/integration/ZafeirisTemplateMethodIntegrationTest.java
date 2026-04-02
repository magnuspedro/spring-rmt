package br.com.magnus.detectionandrefactoring.integration;

import br.com.magnus.config.starter.projects.BaseProject;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.detectionandrefactoring.consumer.ProcessRefactorCandidate;
import br.com.magnus.detectionandrefactoring.repository.ProjectRepository;
import fixtures.Zafeiris;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Zafeiris Template Method pattern detection and refactoring.
 * Uses complete testcontainer environment (LocalStack S3 + Redis).
 * No mocking - all beans are real and exercise full application behavior.
 */
class ZafeirisTemplateMethodIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProcessRefactorCandidate processRefactorCandidate;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void testTemplateMethodPatternRefactoring() throws IOException {
        var projectId = UUID.randomUUID().toString();

        // Create input ZIP with Template Method class hierarchy
        var inputFiles = Map.ofEntries(
                Map.entry("jade/imtp/leap/JICP/JICPPeer.java", Zafeiris.PARENT),
                Map.entry("jade/imtp/leap/JICP/JICPSPeer.java", Zafeiris.CHILD)
        );
        var zipBytes = ZipHelper.createZip(inputFiles);

        // Upload to S3 (projects bucket)
        var s3Client = createS3Client();
        s3Client.putObject(
                req -> req.bucket("projects").key(projectId),
                RequestBody.fromInputStream(new ByteArrayInputStream(zipBytes), zipBytes.length)
        );

        // Save BaseProject to Redis
        var status = new LinkedHashSet<ProjectStatus>();
        status.add(ProjectStatus.EVALUATING_CANDIDATES);

        var baseProject = BaseProject.builder()
                .id(projectId)
                .name("template-method-test")
                .bucket("projects")
                .status(status)
                .build();
        projectRepository.save(baseProject);

        // Execute full refactoring pipeline (no mocks)
        processRefactorCandidate.process(projectId);

        // Verify BaseProject was updated with REFACTORED status
        var updatedProject = projectRepository.findById(projectId).orElseThrow();
        assertThat(updatedProject.getStatus()).contains(
                br.com.magnus.config.starter.projects.ProjectStatus.REFACTORED);

        // Verify candidate information
        assertThat(updatedProject.getCandidatesInformation()).isNotEmpty();
        var candidateInfo = updatedProject.getCandidatesInformation().iterator().next();
        assertThat(candidateInfo.getDesignPattern()).isEqualTo(DesignPattern.TEMPLATE_METHOD);

        // Verify refactored ZIP was uploaded to S3
        var refactoredZip = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket("projects")
                        .key(candidateInfo.getId())
                        .build()
        );

        var refactoredContent = extractZipContent(refactoredZip);

        // Assertions on refactored files
        assertThat(refactoredContent).containsKey("jade/imtp/leap/JICP/JICPPeer.java");
        assertThat(refactoredContent).containsKey("jade/imtp/leap/JICP/JICPSPeer.java");

        // Verify parent class has template method structure
        var refactoredParent = refactoredContent.get("jade/imtp/leap/JICP/JICPPeer.java");
        assertThat(refactoredParent).contains("public final TransportAddress activate");
        assertThat(refactoredParent).contains("protected void beforeActivate");
        assertThat(refactoredParent).contains("protected TransportAddress afterActivate");

        // Verify child class has hook implementations
        var refactoredChild = refactoredContent.get("jade/imtp/leap/JICP/JICPSPeer.java");
        assertThat(refactoredChild).contains("protected void beforeActivate");
        assertThat(refactoredChild).contains("protected TransportAddress afterActivate");

        // Verify message was enqueued to measure-pattern queue (in Redis)
        var queueKey = "rqueue-pattern:measure-pattern";
        assertThat(redisTemplate.hasKey(queueKey)).as("Queue message should be enqueued to measure-pattern").isTrue();

        s3Client.close();
    }

    private Map<String, String> extractZipContent(Object zipStream) throws IOException {
        var zip = new ZipInputStream((java.io.InputStream) zipStream);
        var content = new java.util.LinkedHashMap<String, String>();

        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                var bytes = new ByteArrayOutputStream();
                zip.transferTo(bytes);
                content.put(entry.getName(), bytes.toString());
            }
        }

        zip.close();
        return content;
    }
}
