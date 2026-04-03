package br.com.magnus.projectsyncbff.refactor;

import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.config.starter.projects.CandidateInformation;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CandidateSelection {
    private final CandidateInformation candidate;
    private final List<FileSelection> files;
    private final boolean selected;
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

    public BigDecimal getMetricValue(String metric) {
        return candidate.getMetricValue(metric);
    }

    public int getSelectedFilesCount() {
        return (int) files.stream().filter(FileSelection::isSelected).count();
    }
}
