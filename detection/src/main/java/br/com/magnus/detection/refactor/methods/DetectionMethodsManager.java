package br.com.magnus.detection.refactor.methods;

import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;

import java.util.List;


public interface DetectionMethodsManager {
    void refactor(Project project);

    default boolean hasNoCandidates(List<RefactoringCandidate> candidates) {
        return candidates.isEmpty();
    }
}
