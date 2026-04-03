package br.com.magnus.projectsyncbff.refactor;

import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Builder
public class FileSelection {
    private final String key;
    private final String file;
    private final List<String> dependencyFiles;
    private final List<String> selectionGroupKeys;
    private final List<String> blockingReasons;
    private final List<QualityAttributeResult> metrics;
    private final boolean requested;
    private final boolean selected;
    private final boolean locked;
    private final boolean blocked;

    public BigDecimal getMetricValue(String metric) {
        return Optional.ofNullable(metrics)
                .orElseGet(List::of)
                .stream()
                .filter(result -> result.qualityAttributeName().equals(metric))
                .map(QualityAttributeResult::changePercentage)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    public boolean hasMetrics() {
        return metrics != null && !metrics.isEmpty();
    }

    public String getSelectionGroupId() {
        return Optional.ofNullable(selectionGroupKeys)
                .orElseGet(List::of)
                .stream()
                .sorted()
                .collect(Collectors.joining("|"));
    }
}
