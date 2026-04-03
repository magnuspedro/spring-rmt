package br.com.magnus.projectsyncbff.controller;

import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.members.metrics.BasicQualityAttributeResult;
import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.config.starter.projects.CandidateInformation;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.projectsyncbff.refactor.CandidateSelection;
import br.com.magnus.projectsyncbff.refactor.FileSelection;
import br.com.magnus.projectsyncbff.refactor.ProjectResults;
import br.com.magnus.projectsyncbff.refactor.ProjectSelection;
import br.com.magnus.projectsyncbff.refactor.RefactorProject;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
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
class HtmxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RefactorProject refactorProject;

    @Test
    @SneakyThrows
    void shouldRenderIndexPage() {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Analyze candidates")));
    }

    @Test
    @SneakyThrows
    void shouldTest200() {
        var content = "Hello, World!".getBytes();
        var filename = "hello.txt";

        var part = new MockPart("file", filename, content);
        part.getHeaders().setContentType(MediaType.TEXT_PLAIN);

        mockMvc.perform(multipart("/upload")
                .part(part)
        ).andExpect(status().isOk());

        Mockito.verify(refactorProject, Mockito.atLeastOnce()).process(assertArg(it ->
                Assertions.assertAll("Verify project construction",
                        () -> assertThat(it.getSize(), is((long) content.length)),
                        () -> assertThat(it.getName(), is(filename)),
                        () -> assertThat(it.getContentType(), is(MediaType.TEXT_PLAIN_VALUE)),
                        () -> assertThat(it.getZipContent(), is(content)))
        ));
    }

    @Test
    @SneakyThrows
    void shouldRenderCandidateWorkspace() {
        when(refactorProject.retrieve("project-1")).thenReturn(projectResults(false));

        mockMvc.perform(get("/project/{id}", "project-1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Files in this refactoring")))
                .andExpect(content().string(containsString("Reference")))
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
                                                .blockingReasons(List.of())
                                                .requested(selected)
                                                .selected(selected)
                                                .locked(false)
                                                .blocked(false)
                                                .build(),
                                        FileSelection.builder()
                                                .key("candidate-a::src/main/java/example/PaymentStrategy.java")
                                                .file("src/main/java/example/PaymentStrategy.java")
                                                .dependencyFiles(List.of())
                                                .blockingReasons(List.of())
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
}
