package br.com.magnus.detectionandrefactoring.integration;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.projects.BaseProject;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.detectionandrefactoring.consumer.ProcessRefactorCandidate;
import br.com.magnus.detectionandrefactoring.repository.ProjectRepository;
import fixtures.Wei;
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
 * Integration test for Wei Strategy pattern detection and refactoring.
 * Uses complete testcontainer environment (LocalStack S3 + Redis).
 * No mocking - all beans are real and exercise full application behavior.
 */
class WeiStrategyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProcessRefactorCandidate processRefactorCandidate;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void testStrategyPatternRefactoring() throws IOException {
        var projectId = UUID.randomUUID().toString();

        // Create input ZIP from fixture
        var javaFiles = Wei.createJavaFilesStrategy();
        var inputFiles = javaFiles.stream()
                .collect(java.util.stream.Collectors.toMap(
                        f -> f.getPath() + f.getName(),
                        JavaFile::getOriginalClass
                ));
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
                .name("strategy-test")
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
        assertThat(candidateInfo.getDesignPattern()).isEqualTo(DesignPattern.STRATEGY);

        // Verify refactored ZIP was uploaded to S3
        var refactoredZip = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket("projects")
                        .key(candidateInfo.getId())
                        .build()
        );

        var refactoredContent = extractZipContent(refactoredZip);

        // Assertions on refactored files
        assertThat(refactoredContent).containsKey("stratety/MovieTicket.java");
        assertThat(refactoredContent).containsKey("stratety/Strategy.java");
        assertThat(refactoredContent).containsKeys(
                "stratety/ConcreteStrategyS.java",
                "stratety/ConcreteStrategyC.java",
                "stratety/ConcreteStrategyM.java"
        );

        // Verify content matches expected refactoring
        assertThat(refactoredContent.get("stratety/Strategy.java"))
                .contains("public abstract class Strategy");

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
