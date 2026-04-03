package br.com.magnus.metricscalculator.consumer;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.members.metrics.BasicQualityAttributeResult;
import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.config.starter.projects.BaseProject;
import br.com.magnus.config.starter.projects.CandidateInformation;
import br.com.magnus.metricscalculator.qualityAttributes.QualityAttributesProcessor;
import br.com.magnus.metricscalculator.repository.ExtractProjects;
import br.com.magnus.metricscalculator.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricsProcessorTest {

    @Mock
    private ExtractProjects extractProjects;
    @Mock
    private QualityAttributesProcessor processor;
    @Mock
    private ProjectRepository projectRepository;
    private MetricsProcessor metricsProcessor;

    @BeforeEach
    void setup() {
        metricsProcessor = new MetricsProcessor(extractProjects, processor, projectRepository);
    }

    @Test
    @DisplayName("Should test for a project not found")
    void shouldTestForAProjectNotFound() {
        when(projectRepository.findById("id")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> metricsProcessor.process("id"));

        verify(extractProjects, never()).extractProject(any(), any());
        verify(processor, never()).extract(any(), any());
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should calculate metrics per linked file group and reuse them per file")
    void shouldCalculateMetricsPerLinkedFileGroup() {
        var candidate = CandidateInformation.builder()
                .id("candidate-a")
                .designPattern(DesignPattern.STRATEGY)
                .reference(Reference.builder().title("Wei et al.").year(2014).authors(List.of("Wei")).build())
                .filesChanged(new LinkedHashSet<>(List.of(
                        "src/main/java/example/Checkout.java",
                        "src/main/java/example/PaymentStrategy.java",
                        "src/main/java/example/Independent.java")))
                .build();
        var project = BaseProject.builder()
                .id("id")
                .bucket("bucket")
                .candidatesInformation(List.of(candidate))
                .build();

        var originalFiles = List.of(
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
                        """),
                javaFile("src/main/java/example/Independent.java", """
                        package example;

                        public class Independent {
                        }
                        """));
        var refactoredFiles = originalFiles;

        when(projectRepository.findById("id")).thenReturn(Optional.of(project));
        when(extractProjects.loadProjectFiles("id", "bucket")).thenReturn(originalFiles);
        when(extractProjects.loadProjectFiles("candidate-a", "bucket")).thenReturn(refactoredFiles);
        when(extractProjects.extractProject(ArgumentMatchers.<List<JavaFile>>any()))
                .thenReturn(Path.of("/tmp/original"),
                        Path.of("/tmp/candidate"),
                        Path.of("/tmp/group-linked-original"),
                        Path.of("/tmp/group-linked-candidate"),
                        Path.of("/tmp/group-independent-original"),
                        Path.of("/tmp/group-independent-candidate"));
        when(processor.extract(any(), any()))
                .thenReturn(metrics("1"), metrics("2"), metrics("3"));

        metricsProcessor.process("id");

        assertThat(candidate.getMetrics()).containsExactlyElementsOf(metrics("1"));
        assertThat(candidate.getFileMetrics("src/main/java/example/Checkout.java"))
                .containsExactlyElementsOf(metrics("2"));
        assertThat(candidate.getFileMetrics("src/main/java/example/PaymentStrategy.java"))
                .containsExactlyElementsOf(metrics("2"));
        assertThat(candidate.getFileMetrics("src/main/java/example/Independent.java"))
                .containsExactlyElementsOf(metrics("3"));
        verify(processor).extract(Path.of("/tmp/original"), Path.of("/tmp/candidate"));
        verify(projectRepository).save(project);
    }

    private List<QualityAttributeResult> metrics(String value) {
        return List.of(new BasicQualityAttributeResult("MAINTAINABILITY", new BigDecimal(value)));
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
