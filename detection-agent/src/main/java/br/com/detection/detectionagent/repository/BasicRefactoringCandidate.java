package br.com.detection.detectionagent.repository;

import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.patterns.DesignPattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BasicRefactoringCandidate implements RefactoringCandidate {
    private final String id;
    private final Reference reference;
    private final String pkg;
    private final String className;
    private final DesignPattern eligiblePattern;

    public static List<RefactoringCandidate> from(List<RefactoringCandidate> refactoringCandidates) {
        return refactoringCandidates.stream()
                .map(refactoringCandidate -> new BasicRefactoringCandidate(refactoringCandidate.getId(), refactoringCandidate.getReference(), refactoringCandidate.getPkg(), refactoringCandidate.getClassName(), refactoringCandidate.getEligiblePattern()))
                .map(RefactoringCandidate.class::cast)
                .toList();
    }
}
