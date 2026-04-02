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
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ZafeirisTemplateMethodIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProcessRefactorCandidate processRefactorCandidate;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void testTemplateMethodPatternRefactoring() throws IOException {
        var projectId = UUID.randomUUID().toString();

        var inputFiles = Map.ofEntries(
                Map.entry("jade/imtp/leap/JICP/JICPPeer.java", Zafeiris.PARENT),
                Map.entry("jade/imtp/leap/JICP/JICPSPeer.java", Zafeiris.CHILD)
        );
        var zipBytes = ZipHelper.createZip(inputFiles);

        var s3Client = createS3Client();
        s3Client.putObject(
                req -> req.bucket("projects").key(projectId),
                RequestBody.fromInputStream(new ByteArrayInputStream(zipBytes), zipBytes.length)
        );

        var status = new LinkedHashSet<ProjectStatus>();
        status.add(ProjectStatus.EVALUATING_CANDIDATES);

        var baseProject = BaseProject.builder()
                .id(projectId)
                .name("template-method-test")
                .bucket("projects")
                .status(status)
                .build();
        projectRepository.save(baseProject);

        processRefactorCandidate.process(projectId);

        var updatedProject = projectRepository.findById(projectId).orElseThrow();
        assertThat(updatedProject.getStatus()).contains(
                br.com.magnus.config.starter.projects.ProjectStatus.REFACTORED);

        assertThat(updatedProject.getCandidatesInformation()).isNotEmpty();
        var candidateInfo = updatedProject.getCandidatesInformation().iterator().next();
        assertThat(candidateInfo.getId()).isNotBlank();
        assertThat(candidateInfo.getDesignPattern()).isEqualTo(DesignPattern.TEMPLATE_METHOD);
        assertThat(candidateInfo.getFilesChanged())
                .isNotEmpty()
                .contains("jade/imtp/leap/JICP/JICPPeer.java", "jade/imtp/leap/JICP/JICPSPeer.java");

        try (var refactoredZip = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket("projects")
                        .key(candidateInfo.getId())
                        .build()
        )) {
            var refactoredContent = ZipHelper.extractZip(refactoredZip);

            assertThat(refactoredContent).containsKey("jade/imtp/leap/JICP/JICPPeer.java");
            assertThat(refactoredContent).containsKey("jade/imtp/leap/JICP/JICPSPeer.java");

            var refactoredParent = refactoredContent.get("jade/imtp/leap/JICP/JICPPeer.java");
            assertThat(refactoredParent)
                    .contains("public final TransportAddress activate")
                    .contains("protected void beforeActivate")
                    .contains("protected TransportAddress afterActivate")
                    .contains("beforeActivate(l, peerID, p);")
                    .contains("return afterActivate(l, peerID, p, superReturnVar);");

            var refactoredChild = refactoredContent.get("jade/imtp/leap/JICP/JICPSPeer.java");
            assertThat(refactoredChild)
                    .contains("protected void beforeActivate")
                    .contains("protected TransportAddress afterActivate")
                    .contains("ctx = SSLHelper.createContext();")
                    .contains("return ta;");
        }

        var queueKey = "rqueue-pattern:measure-pattern";
        var queuedMessages = redisTemplate.opsForList().range(queueKey, 0, -1);
        assertThat(queuedMessages).singleElement().asString().contains(projectId);

        s3Client.close();
    }
}
