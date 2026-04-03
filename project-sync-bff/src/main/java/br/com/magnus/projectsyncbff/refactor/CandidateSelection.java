package br.com.magnus.projectsyncbff.refactor;

import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.config.starter.projects.CandidateInformation;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter
@Builder
public class CandidateSelection {
    private final CandidateInformation candidate;
    private final Set<String> requiredCandidateIds;
    private final List<String> dependencyReasons;
    private final List<String> blockingReasons;
    private final boolean requested;
    private final boolean selected;
    private final boolean locked;
    private final boolean blocked;

    public String getId() {
        return candidate.getId();
    }

    public DesignPattern getDesignPattern() {
        return candidate.getDesignPattern();
    }

    public Reference getReference() {
        return candidate.getReference();
    }

    public Set<String> getFilesChanged() {
        return candidate.getFilesChanged();
    }

    public BigDecimal getMetricValue(String metric) {
        return candidate.getMetricValue(metric);
    }

    public boolean isSelectable() {
        return !blocked;
    }
}
