package br.com.magnus.detectionandrefactoring.refactor.methods.weiL;

import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;

public interface RefactoringExecutor {

    boolean isApplicable(RefactoringCandidate candidate);

    void refactor(RefactorFiles refactorFiles);
}
