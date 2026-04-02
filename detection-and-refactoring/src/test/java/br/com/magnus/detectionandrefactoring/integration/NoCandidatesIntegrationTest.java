package br.com.magnus.detectionandrefactoring.integration;

import br.com.magnus.config.starter.projects.BaseProject;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.detectionandrefactoring.consumer.ProcessRefactorCandidate;
import br.com.magnus.detectionandrefactoring.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NoCandidatesIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProcessRefactorCandidate processRefactorCandidate;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void shouldMarkProjectAsNoCandidatesWhenNoPatternIsFound() {
        var projectId = UUID.randomUUID().toString();
        var zipBytes = ZipHelper.createZip(Map.of(
                "sample/HelloWorld.java", """
                        package sample;

                        public class HelloWorld {
                            public String greet(String name) {
                                return "Hello " + name;
                            }
                        }
                        """
        ));

        var s3Client = createS3Client();
        s3Client.putObject(
                req -> req.bucket("projects").key(projectId),
                RequestBody.fromInputStream(new ByteArrayInputStream(zipBytes), zipBytes.length)
        );

        var status = new LinkedHashSet<ProjectStatus>();
        status.add(ProjectStatus.EVALUATING_CANDIDATES);

        projectRepository.save(BaseProject.builder()
                .id(projectId)
                .name("hello-world")
                .bucket("projects")
                .status(status)
                .build());

        processRefactorCandidate.process(projectId);

        var updatedProject = projectRepository.findById(projectId).orElseThrow();
        assertThat(updatedProject.getStatus()).contains(ProjectStatus.NO_CANDIDATES);
        assertThat(updatedProject.getCandidatesInformation()).isNullOrEmpty();
        assertThat(redisTemplate.hasKey("rqueue-pattern:measure-pattern")).isFalse();

        s3Client.close();
    }
}
