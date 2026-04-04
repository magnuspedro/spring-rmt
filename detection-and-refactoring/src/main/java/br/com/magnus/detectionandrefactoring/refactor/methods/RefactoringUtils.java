package br.com.magnus.detectionandrefactoring.refactor.methods;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;

import java.util.List;

/**
 * Utility methods for refactoring operations.
 * <p>
 * Provides common functionality used across different refactoring implementations,
 * reducing code duplication and ensuring consistent behavior.
 */
public final class RefactoringUtils {

    private RefactoringUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a deep copy of project files for isolated refactoring.
     * <p>
     * Each file is cloned to ensure that refactoring operations do not
     * affect the original project content.
     *
     * @param project the project containing files to clone
     * @return list of cloned JavaFile instances
     */
    public static List<JavaFile> cloneProjectFiles(Project project) {
        return project.getOriginalContent().stream()
                .map(JavaFile::clone)
                .toList();
    }

    /**
     * Creates a RefactorFiles instance for a single candidate.
     * <p>
     * The files are cloned to ensure isolation during refactoring.
     *
     * @param project   the project containing source files
     * @param candidate the candidate to refactor
     * @return configured RefactorFiles ready for refactoring
     */
    public static RefactorFiles createRefactorFiles(Project project, RefactoringCandidate candidate) {
        return RefactorFiles.builder()
                .files(cloneProjectFiles(project))
                .candidates(List.of(candidate))
                .build();
    }

    /**
     * Checks if the refactoring produced any file changes.
     *
     * @param refactorFiles the result of a refactoring operation
     * @return true if files were changed, false otherwise
     */
    public static boolean hasChanges(RefactorFiles refactorFiles) {
        return !refactorFiles.filesChanged().isEmpty();
    }

    /**
     * Extracts class names from a list of candidates.
     *
     * @param candidates the candidates to extract names from
     * @return list of class names
     */
    public static List<String> extractClassNames(List<RefactoringCandidate> candidates) {
        return candidates.stream()
                .map(RefactoringCandidate::getClassName)
                .toList();
    }

    /**
     * Filters out null values from a list.
     *
     * @param list the list to filter
     * @param <T>  the type of elements
     * @return list without null values
     */
    public static <T> List<T> nonNull(List<T> list) {
        return list.stream()
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
