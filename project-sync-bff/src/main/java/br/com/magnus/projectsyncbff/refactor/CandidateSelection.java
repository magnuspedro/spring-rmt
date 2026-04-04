package br.com.magnus.projectsyncbff.refactor;

import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.config.starter.projects.CandidateInformation;
import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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

    public String getDisplayPatternName() {
        return Arrays.stream(candidate.getDesignPattern().name().toLowerCase(Locale.ROOT).split("_"))
                .map(part -> part.isEmpty() ? part : Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining(" "));
    }

    public Reference getReference() {
        return candidate.getReference();
    }

    public int getSelectedFilesCount() {
        return (int) files.stream().filter(FileSelection::isSelected).count();
    }
}
