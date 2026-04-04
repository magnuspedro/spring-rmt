package br.com.magnus.detectionandrefactoring.refactor.methods;

import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.detectionandrefactoring.configuration.RefactoringProperties;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideEtAl2000;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * Detection methods manager for Cinneide et al. 2000 design patterns.
 * <p>
 * Detects and refactors Factory Method, Singleton, Abstract Factory,
 * Strategy, and Bridge patterns as described by Cinneide et al.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DetectionMethodsManagerCinneide implements DetectionMethodsManager {

    /**
     * Executor key for Cinneide refactoring operations.
     */
    private static final String CINNEIDE_EXECUTOR_KEY = "cinneide";

    private final CinneideEtAl2000 cinneideEtAl2000;
    private final RefactoringProperties refactoringProperties;

    @Qualifier("cinneideRefactoringExecutor")
    private final Executor cinneideRefactoringExecutor;

    @Override
    public List<RefactorFiles> refactor(Project project) {
        var candidates = cinneideEtAl2000.extractCandidates(project.getOriginalContent());

        if (hasNoCandidates(candidates)) {
            log.info("No candidates found for Cinneide");
            return List.of();
        }

        var refactored = executeRefactoring(project, candidates);

        if (refactored.isEmpty()) {
            log.info("No candidates successfully refactored for Cinneide");
            return List.of();
        }

        log.info("Candidates for Cinneide {}", RefactoringUtils.extractClassNames(candidates));
        log.info("Candidates Refactored with success");
        return refactored;
    }

    /**
     * Executes refactoring for all candidates in parallel.
     *
     * @param project    the project to refactor
     * @param candidates the candidates to process
     * @return list of successfully refactored file groups
     */
    private List<RefactorFiles> executeRefactoring(Project project, List<RefactoringCandidate> candidates) {
        return DetectionMethodsManager.executeInParallel(
                        candidates,
                        cinneideRefactoringExecutor,
                        refactoringProperties.getParallelism(CINNEIDE_EXECUTOR_KEY),
                        candidate -> refactorCandidate(project, candidate))
                .stream()
                .flatMap(Optional::stream)
                .toList();
    }

    /**
     * Refactors a single candidate using the Cinneide algorithm.
     *
     * @param project   the project to refactor
     * @param candidate the candidate to refactor
     * @return Optional containing refactored files if successful, empty otherwise
     */
    private Optional<RefactorFiles> refactorCandidate(Project project, RefactoringCandidate candidate) {
        try {
            var files = RefactoringUtils.cloneProjectFiles(project);
            var refactorFiles = RefactoringUtils.createRefactorFiles(project, candidate);
            cinneideEtAl2000.refactor(refactorFiles);
            return RefactoringUtils.hasChanges(refactorFiles) ? Optional.of(refactorFiles) : Optional.empty();
        } catch (RuntimeException ex) {
            log.warn("Skipping invalid Cinneide candidate {}: {}",
                    candidate.getClassName(), ex.getMessage());
            return Optional.empty();
        }
    }
}
