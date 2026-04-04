package br.com.magnus.config.starter.projects;

import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.members.metrics.BasicQualityAttributeResult;
import br.com.magnus.config.starter.members.metrics.FileMetrics;
import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;
import br.com.magnus.config.starter.patterns.DesignPattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Builder
@ToString
public final class CandidateInformation {
    private final Reference reference;
    private final String id;
    @Builder.Default
    private final Set<String> filesChanged = new HashSet<>();
    private final DesignPattern designPattern;
    @Setter
    private List<QualityAttributeResult> metrics;
    @Setter
    private Map<String, FileMetrics> fileMetrics = new LinkedHashMap<>();

    public BigDecimal getMetricValue(String metric) {
        if (this.metrics == null) {
            throw new IllegalStateException("Metrics not set for candidate: " + this.id);
        }
        return this.metrics.stream()
                .filter(m -> m.qualityAttributeName().equals(metric))
                .map(QualityAttributeResult::changePercentage)
                .findFirst()
                .orElseThrow();
    }

    public List<QualityAttributeResult> getFileMetrics(String file) {
        if (this.fileMetrics == null) {
            return List.of();
        }
        var metrics = this.fileMetrics.getOrDefault(file, FileMetrics.builder().build());
        if (metrics == null || metrics.getMetrics() == null) {
            return List.of();
        }
        return metrics.getMetrics().stream()
                .map(metric -> (QualityAttributeResult) metric)
                .toList();
    }

    public void setFileMetricsByFile(Map<String, List<QualityAttributeResult>> metricsByFile) {
        var mappedMetrics = new LinkedHashMap<String, FileMetrics>();
        metricsByFile.forEach((file, metrics) -> mappedMetrics.put(file, FileMetrics.builder()
                .metrics(metrics.stream()
                        .map(metric -> new BasicQualityAttributeResult(metric.qualityAttributeName(), metric.changePercentage()))
                        .toList())
                .build()));
        this.fileMetrics = mappedMetrics;
    }
}
