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
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WeiStrategyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProcessRefactorCandidate processRefactorCandidate;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void testStrategyPatternRefactoring() throws IOException {
        var projectId = UUID.randomUUID().toString();

        var javaFiles = Wei.createJavaFilesStrategy();
        var inputFiles = javaFiles.stream()
                .collect(java.util.stream.Collectors.toMap(
                        f -> f.getPath() + f.getName(),
                        JavaFile::getOriginalClass
                ));
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
                .name("strategy-test")
                .bucket("projects")
                .status(status)
                .build();
        projectRepository.save(baseProject);

        processRefactorCandidate.process(projectId);

        var updatedProject = projectRepository.findById(projectId).orElseThrow();
        assertThat(updatedProject.getStatus()).contains(
                br.com.magnus.config.starter.projects.ProjectStatus.REFACTORED);

        assertThat(updatedProject.getCandidatesInformation()).isNotEmpty();
        var candidateInfo = updatedProject.getCandidatesInformation().stream()
                .filter(candidate -> candidate.getDesignPattern() == DesignPattern.STRATEGY)
                .findFirst()
                .orElseThrow();
        assertThat(candidateInfo.getId()).isNotBlank();
        assertThat(candidateInfo.getDesignPattern()).isEqualTo(DesignPattern.STRATEGY);
        assertThat(candidateInfo.getFilesChanged())
                .isNotEmpty()
                .anyMatch(path -> path.endsWith("MovieTicket.java"));

        try (var refactoredZip = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket("projects")
                        .key(candidateInfo.getId())
                        .build()
        )) {
            var refactoredContent = ZipHelper.extractZip(refactoredZip);

            assertThat(refactoredContent.keySet())
                    .anyMatch(path -> path.endsWith("/MovieTicket.java"))
                    .anyMatch(path -> path.endsWith("/Strategy.java"))
                    .anyMatch(path -> path.endsWith("/ConcreteStrategyS.java"))
                    .anyMatch(path -> path.endsWith("/ConcreteStrategyC.java"))
                    .anyMatch(path -> path.endsWith("/ConcreteStrategyM.java"));

            assertThat(refactoredContent.values())
                    .anySatisfy(source -> assertThat(source).contains("public abstract class Strategy"))
                    .anySatisfy(source -> assertThat(source).contains("return strategy.calculate(this.price);"))
                    .anySatisfy(source -> assertThat(source).contains("return price * 0.8;"))
                    .anySatisfy(source -> assertThat(source).contains("return price - 10;"))
                    .anySatisfy(source -> assertThat(source).contains("return price * 0.5;"));
        }

        var queueKey = "rqueue-pattern:measure-pattern";
        var queuedMessages = redisTemplate.opsForList().range(queueKey, 0, -1);
        assertThat(queuedMessages).singleElement().asString().contains(projectId);

        s3Client.close();
    }
}
