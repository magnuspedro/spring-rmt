package br.com.magnus.projectsyncbff.refactor;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.file.extractor.FileExtractor;
import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.members.metrics.BasicQualityAttributeResult;
import br.com.magnus.config.starter.members.metrics.FileMetrics;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.config.starter.projects.BaseProject;
import br.com.magnus.config.starter.projects.CandidateInformation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectSelectionPlannerTest {

    @Mock
    private FileExtractor fileExtractor;

    private ProjectSelectionPlanner planner;

    @BeforeEach
    void setUp() {
        planner = new ProjectSelectionPlanner(fileExtractor);
    }

    @Test
    void shouldAutoSelectDependentFilesInsideSameCandidate() {
        var candidate = candidate("candidate-a",
                "src/main/java/example/Checkout.java",
                "src/main/java/example/PaymentStrategy.java");
        var project = baseProject(List.of(candidate));
        when(fileExtractor.extract(project)).thenReturn(List.of(
                javaFile("src/main/java/example/Checkout.java", """
                        package example;

                        public class Checkout {
                            private PaymentStrategy paymentStrategy;
                        }
                        """),
                javaFile("src/main/java/example/PaymentStrategy.java", """
                        package example;

                        public interface PaymentStrategy {
                        }
                        """)
        ));

        var selection = planner.plan(project, List.of(ProjectSelectionPlanner.fileKey("candidate-a", "src/main/java/example/Checkout.java")));

        assertThat(selection.getSelectedFileKeys())
                .containsExactly(
                        ProjectSelectionPlanner.fileKey("candidate-a", "src/main/java/example/Checkout.java"),
                        ProjectSelectionPlanner.fileKey("candidate-a", "src/main/java/example/PaymentStrategy.java"));
        assertThat(selection.getCandidates().getFirst().getFiles())
                .filteredOn(FileSelection::getFile, "src/main/java/example/PaymentStrategy.java")
                .singleElement()
                .satisfies(file -> {
                    assertThat(file.isSelected()).isTrue();
                    assertThat(file.isLocked()).isTrue();
                });
    }

    @Test
    void shouldAutoSelectLinkedFilesFromEitherSideOfTheRelation() {
        var candidate = candidate("candidate-a",
                "src/main/java/example/Checkout.java",
                "src/main/java/example/PaymentStrategy.java");
        var project = baseProject(List.of(candidate));
        when(fileExtractor.extract(project)).thenReturn(List.of(
                javaFile("src/main/java/example/Checkout.java", """
                        package example;

                        public class Checkout {
                            private PaymentStrategy paymentStrategy;
                        }
                        """),
                javaFile("src/main/java/example/PaymentStrategy.java", """
                        package example;

                        public interface PaymentStrategy {
                        }
                        """)
        ));

        var selection = planner.plan(project, List.of(ProjectSelectionPlanner.fileKey("candidate-a", "src/main/java/example/PaymentStrategy.java")));

        assertThat(selection.getSelectedFileKeys())
                .containsExactlyInAnyOrder(
                        ProjectSelectionPlanner.fileKey("candidate-a", "src/main/java/example/Checkout.java"),
                        ProjectSelectionPlanner.fileKey("candidate-a", "src/main/java/example/PaymentStrategy.java"));
    }

    @Test
    void shouldAllowSelectingOnlyOneIndependentFileInSameCandidate() {
        var candidate = candidate("candidate-a",
                "src/main/java/example/IndependentOne.java",
                "src/main/java/example/IndependentTwo.java");
        var project = baseProject(List.of(candidate));
        when(fileExtractor.extract(project)).thenReturn(List.of(
                javaFile("src/main/java/example/IndependentOne.java", """
                        package example;

                        public class IndependentOne {
                        }
                        """),
                javaFile("src/main/java/example/IndependentTwo.java", """
                        package example;

                        public class IndependentTwo {
                        }
                        """)
        ));

        var selection = planner.plan(project, List.of(ProjectSelectionPlanner.fileKey("candidate-a", "src/main/java/example/IndependentOne.java")));

        assertThat(selection.getSelectedFileKeys())
                .containsExactly(ProjectSelectionPlanner.fileKey("candidate-a", "src/main/java/example/IndependentOne.java"));
    }

    @Test
    void shouldAllowFileWhenDependencyIsOutsideCandidateScope() {
        var candidate = candidate("candidate-a", "src/main/java/example/Checkout.java");
        var project = baseProject(List.of(candidate));
        when(fileExtractor.extract(project)).thenReturn(List.of(
                javaFile("src/main/java/example/Checkout.java", """
                        package example;

                        public class Checkout {
                            private PaymentStrategy paymentStrategy;
                        }
                        """),
                javaFile("src/main/java/example/PaymentStrategy.java", """
                        package example;

                        public interface PaymentStrategy {
                        }
                        """)
        ));

        var selection = planner.plan(project, List.of(ProjectSelectionPlanner.fileKey("candidate-a", "src/main/java/example/Checkout.java")));

        assertThat(selection.isDownloadable()).isTrue();
        assertThat(selection.getBlockedCount()).isZero();
        assertThat(selection.getSelectedFileKeys())
                .containsExactly(ProjectSelectionPlanner.fileKey("candidate-a", "src/main/java/example/Checkout.java"));
        assertThat(selection.getCandidates().getFirst().getFiles().getFirst().isBlocked()).isFalse();
    }

    @Test
    void shouldRejectSelectionsMissingDependentFiles() {
        var candidate = candidate("candidate-a",
                "src/main/java/example/Checkout.java",
                "src/main/java/example/PaymentStrategy.java");
        var project = baseProject(List.of(candidate));
        when(fileExtractor.extract(project)).thenReturn(List.of(
                javaFile("src/main/java/example/Checkout.java", """
                        package example;

                        public class Checkout {
                            private PaymentStrategy paymentStrategy;
                        }
                        """),
                javaFile("src/main/java/example/PaymentStrategy.java", """
                        package example;

                        public interface PaymentStrategy {
                        }
                        """)
        ));

        assertThatThrownBy(() -> planner.validateSelection(project,
                List.of(ProjectSelectionPlanner.fileKey("candidate-a", "src/main/java/example/Checkout.java"))))
                .isInstanceOf(SelectionValidationException.class)
                .hasMessageContaining("dependent files are missing")
                .satisfies(throwable -> assertThat(((SelectionValidationException) throwable).getMissingCandidateIds())
                        .containsExactly(ProjectSelectionPlanner.fileKey("candidate-a", "src/main/java/example/PaymentStrategy.java")));
    }

    @Test
    void shouldExposeFileLevelMetricsInSelection() {
        var candidate = candidate("candidate-a", "src/main/java/example/Checkout.java");
        candidate.setFileMetrics(Map.of(
                "src/main/java/example/Checkout.java", FileMetrics.builder()
                        .metrics(List.of(new BasicQualityAttributeResult("MAINTAINABILITY", java.math.BigDecimal.TEN)))
                        .build()));
        var project = baseProject(List.of(candidate));
        when(fileExtractor.extract(project)).thenReturn(List.of(javaFile("src/main/java/example/Checkout.java", """
                package example;

                public class Checkout {
                }
                """)));

        var selection = planner.plan(project, List.of(ProjectSelectionPlanner.fileKey("candidate-a", "src/main/java/example/Checkout.java")));

        assertThat(selection.getCandidates().getFirst().getFiles().getFirst().getMetricValue("MAINTAINABILITY"))
                .isEqualByComparingTo("10");
    }

    private BaseProject baseProject(List<CandidateInformation> candidates) {
        return BaseProject.builder()
                .id("project-1")
                .bucket("projects")
                .candidatesInformation(candidates)
                .build();
    }

    private CandidateInformation candidate(String id, String... files) {
        return CandidateInformation.builder()
                .id(id)
                .designPattern(DesignPattern.STRATEGY)
                .reference(Reference.builder().title("Wei et al.").year(2014).authors(List.of("Wei")).build())
                .filesChanged(new LinkedHashSet<>(List.of(files)))
                .build();
    }

    private JavaFile javaFile(String fullName, String content) {
        var lastSlash = fullName.lastIndexOf('/') + 1;
        return JavaFile.builder()
                .name(fullName.substring(lastSlash))
                .path(fullName.substring(0, lastSlash))
                .originalClass(content)
                .build();
    }
}
