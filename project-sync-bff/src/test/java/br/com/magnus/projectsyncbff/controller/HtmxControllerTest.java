package br.com.magnus.projectsyncbff.controller;

import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.members.metrics.BasicQualityAttributeResult;
import br.com.magnus.config.starter.members.metrics.FileMetrics;
import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.config.starter.projects.CandidateInformation;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.projectsyncbff.refactor.CandidateSelection;
import br.com.magnus.projectsyncbff.refactor.FileSelection;
import br.com.magnus.projectsyncbff.refactor.ProjectResults;
import br.com.magnus.projectsyncbff.refactor.ProjectSelection;
import br.com.magnus.projectsyncbff.refactor.RefactorProject;
import br.com.magnus.projectsyncbff.validation.UploadValidator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({MockitoExtension.class})
@WebMvcTest(HtmxController.class)
@AutoConfigureMockMvc(addFilters = false)
class HtmxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RefactorProject refactorProject;
    @MockitoBean
    private UploadValidator uploadValidator;

    @Test
    @SneakyThrows
    void shouldRenderIndexPage() {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Analyze candidates")))
                .andExpect(content().string(containsString("shouldIgnorePollingError")))
                .andExpect(content().string(containsString("target.classList.contains('processing-panel')")));
    }

    @Test
    @SneakyThrows
    void shouldTest200() {
        var content = zipFile("src/main/java/example/Hello.java", "class Hello {}");
        var filename = "hello.zip";
        var expectedId = new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(content)).toString(16);

        var part = new MockPart("file", filename, content);
        part.getHeaders().setContentType(MediaType.parseMediaType("application/zip"));

        mockMvc.perform(multipart("/upload")
                .part(part)
        ).andExpect(status().isOk());

        Mockito.verify(refactorProject, Mockito.atLeastOnce()).process(assertArg(it ->
                Assertions.assertAll("Verify project construction",
                        () -> assertThat(it.getSize(), is((long) content.length)),
                        () -> assertThat(it.getName(), is(filename)),
                        () -> assertThat(it.getContentType(), is("application/zip")),
                        () -> assertThat(it.getZipContent(), is(content)),
                        () -> assertThat(it.getId(), is(expectedId)))
        ));
    }

    @Test
    @SneakyThrows
    void shouldRenderCandidateWorkspace() {
        when(refactorProject.retrieve("project-1")).thenReturn(projectResults(false));

        mockMvc.perform(get("/project/{id}", "project-1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Files available in this refactoring")))
                .andExpect(content().string(containsString("Reference")))
                .andExpect(content().string(containsString("Linked files")))
                .andExpect(content().string(containsString("Refactoring set")))
                .andExpect(content().string(containsString("Select all files")))
                .andExpect(content().string(containsString("Strategy")))
                .andExpect(content().string(not(containsString("Strategy candidate"))))
                .andExpect(content().string(containsString("MovieTicket.java")));
    }

    @Test
    @SneakyThrows
    void shouldRenderSelectionUpdateWithLockedDependentFile() {
        var fileKey = "candidate-a::src/main/java/example/MovieTicket.java";
        when(refactorProject.retrieve(eq("project-1"), eq(List.of(fileKey)))).thenReturn(projectResults(true));

        mockMvc.perform(post("/project/{id}/selection", "project-1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("requestedFileKey", fileKey))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Auto-selected")))
                .andExpect(content().string(containsString("checked=\"checked\"")))
                .andExpect(content().string(containsString(">1.00<")))
                .andExpect(content().string(containsString("Download refactored project")));
    }

    private ProjectResults projectResults(boolean selected) {
        var candidate = CandidateInformation.builder()
                .id("candidate-a")
                .designPattern(DesignPattern.STRATEGY)
                .reference(Reference.builder().title("Wei et al.").year(2014).authors(List.of("Wei")).build())
                .filesChanged(new LinkedHashSet<>(List.of(
                        "src/main/java/example/MovieTicket.java",
                        "src/main/java/example/PaymentStrategy.java")))
                .metrics(metrics())
                .fileMetrics(Map.of(
                        "src/main/java/example/MovieTicket.java", fileMetrics(),
                        "src/main/java/example/PaymentStrategy.java", fileMetrics()))
                .build();

        var selection = ProjectSelection.builder()
                .candidates(List.of(
                        CandidateSelection.builder()
                                .candidate(candidate)
                                .files(List.of(
                                        FileSelection.builder()
                                                .key("candidate-a::src/main/java/example/MovieTicket.java")
                                                .file("src/main/java/example/MovieTicket.java")
                                                .dependencyFiles(List.of("src/main/java/example/PaymentStrategy.java"))
                                                .selectionGroupKeys(List.of(
                                                        "candidate-a::src/main/java/example/MovieTicket.java",
                                                        "candidate-a::src/main/java/example/PaymentStrategy.java"))
                                                .blockingReasons(List.of())
                                                .metrics(metrics())
                                                .requested(selected)
                                                .selected(selected)
                                                .locked(false)
                                                .blocked(false)
                                                .build(),
                                        FileSelection.builder()
                                                .key("candidate-a::src/main/java/example/PaymentStrategy.java")
                                                .file("src/main/java/example/PaymentStrategy.java")
                                                .dependencyFiles(List.of())
                                                .selectionGroupKeys(List.of(
                                                        "candidate-a::src/main/java/example/MovieTicket.java",
                                                        "candidate-a::src/main/java/example/PaymentStrategy.java"))
                                                .blockingReasons(List.of())
                                                .metrics(metrics())
                                                .requested(false)
                                                .selected(selected)
                                                .locked(selected)
                                                .blocked(false)
                                                .build()
                                ))
                                .selected(selected)
                                .blocked(false)
                                .build()
                ))
                .requestedFileKeys(selected ? List.of("candidate-a::src/main/java/example/MovieTicket.java") : List.of())
                .selectedFileKeys(selected ? List.of(
                        "candidate-a::src/main/java/example/MovieTicket.java",
                        "candidate-a::src/main/java/example/PaymentStrategy.java") : List.of())
                .selectableFileCount(2)
                .blockedCount(0)
                .downloadable(selected)
                .build();

        return ProjectResults.builder()
                .name("project.zip")
                .status(ProjectStatus.FINISHED)
                .duration("1.23")
                .candidatesInformation(List.of(candidate))
                .selection(selection)
                .build();
    }

    private List<QualityAttributeResult> metrics() {
        return List.of(
                new BasicQualityAttributeResult("MAINTAINABILITY", BigDecimal.ONE),
                new BasicQualityAttributeResult("REUSABILITY", BigDecimal.ONE),
                new BasicQualityAttributeResult("RELIABILITY", BigDecimal.ONE)
        );
    }

    private FileMetrics fileMetrics() {
        return FileMetrics.builder()
                .metrics(List.of(
                        new BasicQualityAttributeResult("MAINTAINABILITY", BigDecimal.ONE),
                        new BasicQualityAttributeResult("REUSABILITY", BigDecimal.ONE),
                        new BasicQualityAttributeResult("RELIABILITY", BigDecimal.ONE)))
                .build();
    }

    @SneakyThrows
    private byte[] zipFile(String name, String content) {
        var outputStream = new ByteArrayOutputStream();
        try (var zipOutputStream = new ZipOutputStream(outputStream)) {
            zipOutputStream.putNextEntry(new ZipEntry(name));
            zipOutputStream.write(content.getBytes());
            zipOutputStream.closeEntry();
        }
        return outputStream.toByteArray();
    }
}
