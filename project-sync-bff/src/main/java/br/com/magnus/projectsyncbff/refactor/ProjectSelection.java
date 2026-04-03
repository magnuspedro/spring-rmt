package br.com.magnus.projectsyncbff.refactor;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProjectSelection {
    private final List<CandidateSelection> candidates;
    private final List<String> requestedCandidateIds;
    private final List<String> selectedCandidateIds;
    private final int autoSelectedCount;
    private final int blockedCount;
    private final boolean downloadable;

    public boolean hasCandidates() {
        return candidates != null && !candidates.isEmpty();
    }
}
