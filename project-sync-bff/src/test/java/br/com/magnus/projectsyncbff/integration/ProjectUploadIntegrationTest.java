package br.com.magnus.projectsyncbff.integration;

import br.com.magnus.config.starter.projects.BaseProject;
import br.com.magnus.config.starter.projects.CandidateInformation;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.projectsyncbff.refactor.ProjectSelectionPlanner;
import br.com.magnus.projectsyncbff.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectUploadIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void shouldUploadProjectPersistItAndEnqueueDetection() throws Exception {
        var zipBytes = ZipHelper.createZip(Map.of(
                "src/main/java/example/MovieTicket.java", """
                        package example;

                        class MovieTicket {
                            private Double price;

                            public Double calculate(char type) {
                                if (type == 'S') {
                                    return price * 0.8;
                                }
                                return price;
                            }
                        }
                        """
        ));
        var fileName = "strategy.zip";
        var projectId = sha256(zipBytes);

        var part = new MockPart("file", fileName, zipBytes);
        part.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);

        mockMvc.perform(multipart("/rmt/api/v1/upload").part(part))
                .andExpect(status().isOk())
                .andExpect(content().string(projectId));

        var persistedProject = projectRepository.findById(projectId).orElseThrow();
        assertThat(persistedProject.getName()).isEqualTo(fileName);
        assertThat(persistedProject.getBucket()).isEqualTo("projects");
        assertThat(persistedProject.getStatus()).containsExactly(ProjectStatus.EVALUATING_CANDIDATES);

        var s3Client = createS3Client();
        try (var storedObject = s3Client.getObject(GetObjectRequest.builder()
                .bucket("projects")
                .key(projectId)
                .build())) {
            assertThat(storedObject.readAllBytes()).isEqualTo(zipBytes);
        }

        var queuedMessages = redisTemplate.opsForList().range("rqueue-pattern:detect-pattern", 0, -1);
        assertThat(queuedMessages).singleElement().asString().contains(projectId);
        s3Client.close();
    }

    @Test
    void shouldReturnPersistedProjectViaRestEndpoint() throws Exception {
        var projectId = UUID.randomUUID().toString();
        var project = BaseProject.builder()
                .id(projectId)
                .name("finished-project.zip")
                .bucket("projects")
                .status(new LinkedHashSet<>(List.of(ProjectStatus.FINISHED)))
                .candidatesInformation(List.of(CandidateInformation.builder()
                        .id("candidate-1")
                        .designPattern(DesignPattern.STRATEGY)
                        .reference(Reference.builder().title("Wei et al.").year(2014).authors(List.of("Wei")).build())
                        .build()))
                .build();
        projectRepository.save(project);

        mockMvc.perform(get("/rmt/api/v1/project/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("finished-project.zip"))
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.selection.downloadable").value(false))
                .andExpect(jsonPath("$.candidatesInformation[0].id").value("candidate-1"))
                .andExpect(jsonPath("$.candidatesInformation[0].designPattern").value("STRATEGY"));
    }

    @Test
    void shouldDownloadProjectWithSelectedCandidateFilesReplaced() throws Exception {
        var projectId = UUID.randomUUID().toString();
        var candidateId = UUID.randomUUID().toString();
        var originalZip = ZipHelper.createZip(Map.of(
                "src/main/java/example/MovieTicket.java", "class MovieTicket { int value() { return 1; } }",
                "src/main/java/example/Other.java", "class Other { }"
        ));
        var refactoredZip = ZipHelper.createZip(Map.of(
                "src/main/java/example/MovieTicket.java", "class MovieTicket { int value() { return 2; } }"
        ));

        var s3Client = createS3Client();
        s3Client.putObject(req -> req.bucket("projects").key(projectId), RequestBody.fromBytes(originalZip));
        s3Client.putObject(req -> req.bucket("projects").key(candidateId), RequestBody.fromBytes(refactoredZip));

        projectRepository.save(BaseProject.builder()
                .id(projectId)
                .name("project.zip")
                .bucket("projects")
                .status(new LinkedHashSet<>(List.of(ProjectStatus.FINISHED)))
                .candidatesInformation(List.of(CandidateInformation.builder()
                        .id(candidateId)
                        .designPattern(DesignPattern.STRATEGY)
                        .reference(Reference.builder().title("Wei et al.").year(2014).authors(List.of("Wei")).build())
                        .filesChanged(new LinkedHashSet<>(List.of("src/main/java/example/MovieTicket.java")))
                        .build()))
                .build());

        var downloadUrl = mockMvc.perform(post("/rmt/api/v1/project/{id}/download", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                ["%s"]
                                """.formatted(ProjectSelectionPlanner.fileKey(candidateId, "src/main/java/example/MovieTicket.java"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(downloadUrl).contains("refactored-projects").contains(projectId);

        try (var downloadedZip = s3Client.getObject(GetObjectRequest.builder()
                .bucket("refactored-projects")
                .key(projectId)
                .build())) {
            var zipContents = ZipHelper.extractZip(downloadedZip.readAllBytes());
            assertThat(zipContents.get("src/main/java/example/MovieTicket.java")).contains("return 2");
            assertThat(zipContents.get("src/main/java/example/Other.java")).contains("class Other");
        }

        s3Client.close();
    }

    @Test
    void shouldRejectDownloadWhenDependentFileIsMissing() throws Exception {
        var projectId = UUID.randomUUID().toString();
        var strategyCandidateId = UUID.randomUUID().toString();
        var originalZip = ZipHelper.createZip(Map.of(
                "src/main/java/example/PaymentStrategy.java", """
                        package example;

                        public interface PaymentStrategy {
                        }
                        """,
                "src/main/java/example/Checkout.java", """
                        package example;

                        public class Checkout {
                            private PaymentStrategy paymentStrategy;
                        }
                        """
        ));

        var s3Client = createS3Client();
        s3Client.putObject(req -> req.bucket("projects").key(projectId), RequestBody.fromBytes(originalZip));

        projectRepository.save(BaseProject.builder()
                .id(projectId)
                .name("project.zip")
                .bucket("projects")
                .status(new LinkedHashSet<>(List.of(ProjectStatus.FINISHED)))
                .candidatesInformation(List.of(CandidateInformation.builder()
                        .id(strategyCandidateId)
                        .designPattern(DesignPattern.STRATEGY)
                        .reference(Reference.builder().title("Wei et al.").year(2014).authors(List.of("Wei")).build())
                        .filesChanged(new LinkedHashSet<>(List.of(
                                "src/main/java/example/PaymentStrategy.java",
                                "src/main/java/example/Checkout.java")))
                        .build()))
                .build());

        mockMvc.perform(post("/rmt/api/v1/project/{id}/download", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                ["%s"]
                                """.formatted(ProjectSelectionPlanner.fileKey(strategyCandidateId, "src/main/java/example/Checkout.java"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Some dependent files are missing from the selection."))
                .andExpect(jsonPath("$.missingCandidateIds[0]").value(ProjectSelectionPlanner.fileKey(strategyCandidateId, "src/main/java/example/PaymentStrategy.java")));

        s3Client.close();
    }

    private String sha256(byte[] bytes) throws Exception {
        return new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(bytes)).toString(16);
    }
}
