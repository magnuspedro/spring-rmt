package br.com.magnus.metricscalculator.integration;

import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.config.starter.projects.BaseProject;
import br.com.magnus.config.starter.projects.CandidateInformation;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.metricscalculator.consumer.MetricsRedisConsumer;
import br.com.magnus.metricscalculator.repository.ProjectRepository;
import br.com.magnus.config.starter.message.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsCalculationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MetricsRedisConsumer metricsRedisConsumer;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void shouldCalculateMetricsForRefactoredCandidateAndFinishProject() {
        var projectId = UUID.randomUUID().toString();
        var candidateId = UUID.randomUUID().toString();
        var originalZip = ZipHelper.createZip(Map.of(
                "example/MovieTicket.java", """
                        package example;

                        class MovieTicket {
                            private Double price;

                            public Double calculate(char type) {
                                if (type == 'S') {
                                    return price * 0.8;
                                } else if (type == 'C') {
                                    return price - 10;
                                } else if (type == 'M') {
                                    return price * 0.5;
                                }
                                return -1D;
                            }
                        }
                        """
        ));
        var refactoredZip = ZipHelper.createZip(Map.of(
                "example/MovieTicket.java", """
                        package example;

                        class MovieTicket {
                            private Double price;

                            public Double calculate(Strategy strategy) {
                                return strategy.calculate();
                            }
                        }
                        """,
                "Strategy.java", """
                        public abstract class Strategy {
                            public abstract Double calculate();
                        }
                        """,
                "ConcreteStrategyS.java", """
                        public class ConcreteStrategyS extends Strategy {
                            public Double calculate() {
                                return 1D;
                            }
                        }
                        """
        ));

        var s3Client = createS3Client();
        s3Client.putObject(req -> req.bucket("projects").key(projectId),
                RequestBody.fromInputStream(new ByteArrayInputStream(originalZip), originalZip.length));
        s3Client.putObject(req -> req.bucket("projects").key(candidateId),
                RequestBody.fromInputStream(new ByteArrayInputStream(refactoredZip), refactoredZip.length));

        projectRepository.save(BaseProject.builder()
                .id(projectId)
                .name("metrics-project")
                .bucket("projects")
                .status(new LinkedHashSet<>(List.of(ProjectStatus.REFACTORED)))
                .candidatesInformation(List.of(CandidateInformation.builder()
                        .id(candidateId)
                        .designPattern(DesignPattern.STRATEGY)
                        .reference(Reference.builder().title("Wei et al.").year(2014).authors(List.of("Wei")).build())
                        .filesChanged(new LinkedHashSet<>(List.of(
                                "example/MovieTicket.java",
                                "Strategy.java",
                                "ConcreteStrategyS.java"
                        )))
                        .build()))
                .build());

        metricsRedisConsumer.listener(new Message(projectId));

        var updatedProject = projectRepository.findById(projectId).orElseThrow();
        assertThat(updatedProject.getStatus()).contains(ProjectStatus.FINISHED);
        assertThat(updatedProject.getCandidatesInformation()).hasSize(1);

        var candidate = updatedProject.getCandidatesInformation().getFirst();
        assertThat(candidate.getId()).isEqualTo(candidateId);
        assertThat(candidate.getDesignPattern()).isEqualTo(DesignPattern.STRATEGY);
        assertThat(candidate.getFilesChanged()).containsExactlyInAnyOrder(
                "example/MovieTicket.java",
                "Strategy.java",
                "ConcreteStrategyS.java"
        );
        assertThat(candidate.getMetrics()).isNotNull().hasSize(3);
        assertThat(candidate.getMetrics())
                .extracting(metric -> metric.qualityAttributeName())
                .containsExactlyInAnyOrder("MAINTAINABILITY", "RELIABILITY", "REUSABILITY");
        assertThat(candidate.getMetrics())
                .allSatisfy(metric -> assertThat(metric.changePercentage()).isGreaterThan(BigDecimal.ZERO));
        assertThat(candidate.getMetricValue("MAINTAINABILITY")).isEqualByComparingTo("166.67");
        assertThat(candidate.getMetricValue("RELIABILITY")).isEqualByComparingTo("250.00");
        assertThat(candidate.getMetricValue("REUSABILITY")).isEqualByComparingTo("100.00");

        s3Client.close();
    }
}
