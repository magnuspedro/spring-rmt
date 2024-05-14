package br.com.magnus.config.starter.members.candidates;

import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.patterns.DesignPattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BasicRefactoringCandidate implements RefactoringCandidate {
    private String id;
    private Reference reference;
    private String pkg;
    private String className;
    private DesignPattern eligiblePattern;

    public static List<RefactoringCandidate> from(List<RefactoringCandidate> refactoringCandidates) {
        return refactoringCandidates.stream()
                .map(refactoringCandidate -> new BasicRefactoringCandidate(refactoringCandidate.getId(), refactoringCandidate.getReference(), refactoringCandidate.getPkg(), refactoringCandidate.getClassName(), refactoringCandidate.getEligiblePattern()))
                .map(RefactoringCandidate.class::cast)
                .toList();
    }
}
