package br.com.magnus.detectionandrefactoring.refactor.methods;

import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.detectionandrefactoring.configuration.RefactoringProperties;
import br.com.magnus.detectionandrefactoring.refactor.methods.weiL.WeiEtAl2014;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Detection methods manager for Wei et al. 2014 design patterns.
 * <p>
 * Detects and refactors Strategy and Factory patterns as described by Wei et al.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DetectionMethodsManagerWei implements DetectionMethodsManager {

    /**
     * Executor key for Wei refactoring operations.
     */
    private static final String WEI_EXECUTOR_KEY = "wei";

    private final WeiEtAl2014 weiEtAl2014;
    private final RefactoringProperties refactoringProperties;

    @Qualifier("weiRefactoringExecutor")
    private final Executor weiRefactoringExecutor;

    @Override
    public List<RefactorFiles> refactor(Project project) {
        var candidates = weiEtAl2014.extractCandidates(project.getOriginalContent());

        if (hasNoCandidates(candidates)) {
            log.info("No candidates found for Wei");
            return List.of();
        }

        log.info("Candidates for Wei {}", RefactoringUtils.extractClassNames(candidates));
        var refactoredFiles = executeRefactoring(project, candidates);
        log.info("Candidates Refactored with success");
        return refactoredFiles;
    }

    /**
     * Executes refactoring for all candidates in parallel.
     *
     * @param project    the project to refactor
     * @param candidates the candidates to process
     * @return list of refactored file groups
     */
    private List<RefactorFiles> executeRefactoring(Project project, List<RefactoringCandidate> candidates) {
        return DetectionMethodsManager.executeInParallel(
                        candidates,
                        weiRefactoringExecutor,
                        refactoringProperties.getParallelism(WEI_EXECUTOR_KEY),
                        candidate -> refactorCandidate(project, candidate))
                .stream()
                .toList();
    }

    /**
     * Refactors a single candidate using the Wei algorithm.
     *
     * @param project   the project to refactor
     * @param candidate the candidate to refactor
     * @return refactored files
     */
    private RefactorFiles refactorCandidate(Project project, RefactoringCandidate candidate) {
        var refactorFiles = RefactoringUtils.createRefactorFiles(project, candidate);
        weiEtAl2014.refactor(refactorFiles);
        return refactorFiles;
    }
}
