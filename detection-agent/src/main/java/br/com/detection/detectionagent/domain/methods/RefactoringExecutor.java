package br.com.detection.detectionagent.domain.methods;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.ExtractionMethod;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import com.github.javaparser.ast.CompilationUnit;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.List;

public interface RefactoringExecutor {

    boolean isApplicable(RefactoringCandidate candidate);

    void refactor(RefactoringCandidate candidate, List<JavaFile> dataHandler, ExtractionMethod extractionMethod);

    default void writeChanges(CompilationUnit cUnit, Path file) {
        try (FileWriter fileWriter = new FileWriter(file.toFile())) {
            fileWriter.write(cUnit.toString());
            fileWriter.flush();
        } catch (Exception e) {
            throw new RefactoringExecutorException("Failed to write changes", e);
        }
    }

}
