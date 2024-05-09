package br.com.detection.detectionagent.domain.methods;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.patterns.DesignPattern;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface DetectionMethod {

    Collection<RefactoringCandidate> extractCandidates(List<JavaFile> javaFiles);

    void refactor(List<JavaFile> javaFiles, RefactoringCandidate candidates);

    Set<DesignPattern> getDesignPatterns();

    boolean supports(RefactoringCandidate refactoringCandidate);
}
