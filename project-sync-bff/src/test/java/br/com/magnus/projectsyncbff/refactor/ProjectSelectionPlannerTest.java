package br.com.magnus.projectsyncbff.refactor;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.file.extractor.FileExtractor;
import br.com.magnus.config.starter.members.detectors.methods.Reference;
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
    void shouldAutoSelectDependentCandidates() {
        var project = baseProject(List.of(
                candidate("candidate-a", "src/main/java/example/PaymentStrategy.java"),
                candidate("candidate-b", "src/main/java/example/Checkout.java")
        ));
        when(fileExtractor.extract(project)).thenReturn(List.of(
                javaFile("src/main/java/example/PaymentStrategy.java", """
                        package example;

                        public interface PaymentStrategy {
                        }
                        """),
                javaFile("src/main/java/example/Checkout.java", """
                        package example;

                        public class Checkout {
                            private PaymentStrategy paymentStrategy;
                        }
                        """)
        ));

        var selection = planner.plan(project, List.of("candidate-a"));

        assertThat(selection.getSelectedCandidateIds()).containsExactly("candidate-a", "candidate-b");
        assertThat(selection.getAutoSelectedCount()).isEqualTo(1);
        assertThat(selection.getCandidates())
                .filteredOn(CandidateSelection::getId, "candidate-b")
                .singleElement()
                .satisfies(candidate -> {
                    assertThat(candidate.isSelected()).isTrue();
                    assertThat(candidate.isLocked()).isTrue();
                });
    }

    @Test
    void shouldBlockCandidateWhenDependentFileHasNoCandidate() {
        var project = baseProject(List.of(
                candidate("candidate-a", "src/main/java/example/PaymentStrategy.java")
        ));
        when(fileExtractor.extract(project)).thenReturn(List.of(
                javaFile("src/main/java/example/PaymentStrategy.java", """
                        package example;

                        public interface PaymentStrategy {
                        }
                        """),
                javaFile("src/main/java/example/Checkout.java", """
                        package example;

                        public class Checkout {
                            private PaymentStrategy paymentStrategy;
                        }
                        """)
        ));

        var selection = planner.plan(project, List.of("candidate-a"));

        assertThat(selection.isDownloadable()).isFalse();
        assertThat(selection.getBlockedCount()).isEqualTo(1);
        assertThat(selection.getCandidates())
                .filteredOn(CandidateSelection::getId, "candidate-a")
                .singleElement()
                .satisfies(candidate -> {
                    assertThat(candidate.isBlocked()).isTrue();
                    assertThat(candidate.getBlockingReasons()).isNotEmpty();
                });
    }

    @Test
    void shouldRejectSelectionsMissingDependentCandidates() {
        var project = baseProject(List.of(
                candidate("candidate-a", "src/main/java/example/PaymentStrategy.java"),
                candidate("candidate-b", "src/main/java/example/Checkout.java")
        ));
        when(fileExtractor.extract(project)).thenReturn(List.of(
                javaFile("src/main/java/example/PaymentStrategy.java", """
                        package example;

                        public interface PaymentStrategy {
                        }
                        """),
                javaFile("src/main/java/example/Checkout.java", """
                        package example;

                        public class Checkout {
                            private PaymentStrategy paymentStrategy;
                        }
                        """)
        ));

        assertThatThrownBy(() -> planner.validateSelection(project, List.of("candidate-a")))
                .isInstanceOf(SelectionValidationException.class)
                .hasMessageContaining("dependent candidates are missing")
                .satisfies(throwable -> assertThat(((SelectionValidationException) throwable).getMissingCandidateIds())
                        .containsExactly("candidate-b"));
    }

    private BaseProject baseProject(List<CandidateInformation> candidates) {
        return BaseProject.builder()
                .id("project-1")
                .bucket("projects")
                .candidatesInformation(candidates)
                .build();
    }

    private CandidateInformation candidate(String id, String file) {
        return CandidateInformation.builder()
                .id(id)
                .designPattern(DesignPattern.STRATEGY)
                .reference(Reference.builder().title("Wei et al.").year(2014).authors(List.of("Wei")).build())
                .filesChanged(new LinkedHashSet<>(List.of(file)))
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
