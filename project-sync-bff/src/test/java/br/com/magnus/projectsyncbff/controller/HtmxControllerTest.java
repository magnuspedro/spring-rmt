package br.com.magnus.projectsyncbff.controller;

import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.members.metrics.BasicQualityAttributeResult;
import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.config.starter.projects.CandidateInformation;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.projectsyncbff.refactor.CandidateSelection;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
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
                .andExpect(content().string(containsString("Dependency-safe refactoring set")))
                .andExpect(content().string(containsString("Auto-selected")))
                .andExpect(content().string(containsString("MovieTicket.java")));
    }

    @Test
    @SneakyThrows
    void shouldRenderSelectionUpdateWithLockedDependentCandidate() {
        when(refactorProject.retrieve(eq("project-1"), eq(List.of("candidate-a")))).thenReturn(projectResults(true));

        mockMvc.perform(post("/project/{id}/selection", "project-1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("requestedId", "candidate-a"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Auto-selected")))
                .andExpect(content().string(containsString("Download refactored project")));
    }

    private ProjectResults projectResults(boolean selected) {
        var candidate = CandidateInformation.builder()
                .id("candidate-a")
                .designPattern(DesignPattern.STRATEGY)
                .reference(Reference.builder().title("Wei et al.").year(2014).authors(List.of("Wei")).build())
                .filesChanged(new LinkedHashSet<>(List.of("src/main/java/example/MovieTicket.java")))
                .metrics(metrics())
                .build();
        var dependent = CandidateInformation.builder()
                .id("candidate-b")
                .designPattern(DesignPattern.FACTORY_METHOD)
                .reference(Reference.builder().title("Wei et al.").year(2014).authors(List.of("Wei")).build())
                .filesChanged(new LinkedHashSet<>(List.of("src/main/java/example/Checkout.java")))
                .metrics(metrics())
                .build();

        var selection = ProjectSelection.builder()
                .candidates(List.of(
                        CandidateSelection.builder()
                                .candidate(candidate)
                                .requiredCandidateIds(Set.of("candidate-b"))
                                .dependencyReasons(List.of("Checkout.java also needs refactoring because it references MovieTicket."))
                                .blockingReasons(List.of())
                                .requested(selected)
                                .selected(selected)
                                .locked(false)
                                .blocked(false)
                                .build(),
                        CandidateSelection.builder()
                                .candidate(dependent)
                                .requiredCandidateIds(Set.of())
                                .dependencyReasons(List.of())
                                .blockingReasons(List.of())
                                .requested(false)
                                .selected(selected)
                                .locked(selected)
                                .blocked(false)
                                .build()
                ))
                .requestedCandidateIds(selected ? List.of("candidate-a") : List.of())
                .selectedCandidateIds(selected ? List.of("candidate-a", "candidate-b") : List.of())
                .autoSelectedCount(selected ? 1 : 0)
                .blockedCount(0)
                .downloadable(selected)
                .build();

        return ProjectResults.builder()
                .name("project.zip")
                .status(ProjectStatus.FINISHED)
                .duration("1.23")
                .candidatesInformation(List.of(candidate, dependent))
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
