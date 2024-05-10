package br.com.detection.detectionagent.refactor.methods.weiL;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;

import java.util.List;

public interface RefactoringExecutor {

    boolean isApplicable(RefactoringCandidate candidate);

    void refactor(RefactoringCandidate candidate, List<JavaFile> javaFiles);
}