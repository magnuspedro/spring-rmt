package br.com.magnus.projectsyncbff.refactor;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Builder
public class ProjectSelection {
    private final List<CandidateSelection> candidates;
    private final List<String> requestedFileKeys;
    private final List<String> selectedFileKeys;
    private final int selectableFileCount;
    private final int blockedCount;
    private final boolean downloadable;

    public boolean hasCandidates() {
        return candidates != null && !candidates.isEmpty();
    }

    public boolean hasAggregateMetrics() {
        return !selectedMetricGroups().isEmpty();
    }

    public BigDecimal getAggregateMetricValue(String metric) {
        var metricGroups = selectedMetricGroups();
        if (metricGroups.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        var total = metricGroups.stream()
                .map(file -> file.getMetricValue(metric))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(metricGroups.size()), 2, RoundingMode.HALF_UP);
    }

    private List<FileSelection> selectedMetricGroups() {
        return Optional.ofNullable(candidates)
                .orElseGet(List::of)
                .stream()
                .flatMap(candidate -> candidate.getFiles().stream())
                .filter(FileSelection::isSelected)
                .filter(FileSelection::hasMetrics)
                .collect(Collectors.toMap(FileSelection::getSelectionGroupId,
                        file -> file,
                        (left, right) -> left))
                .values()
                .stream()
                .toList();
    }
}
