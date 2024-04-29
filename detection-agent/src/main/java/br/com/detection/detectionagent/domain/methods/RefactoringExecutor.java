package br.com.detection.detectionagent.domain.methods;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import com.github.javaparser.ast.CompilationUnit;

import java.nio.file.Path;
import java.util.List;

public interface RefactoringExecutor {

    boolean isApplicable(RefactoringCandidate candidate);

    void refactor(RefactoringCandidate candidate, List<JavaFile> javaFiles);

    default void writeChanges(CompilationUnit cUnit, Path file) {

    }

}
