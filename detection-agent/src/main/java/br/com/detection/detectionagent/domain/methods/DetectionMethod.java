package br.com.detection.detectionagent.domain.methods;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.patterns.DesignPattern;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface DetectionMethod {

    Collection<RefactoringCandidate> extractCandidates(List<JavaFile> javaFiles);

    void refactor(List<JavaFile> javaFiles, RefactoringCandidate candidates);

    Set<DesignPattern> getDesignPatterns();
}
