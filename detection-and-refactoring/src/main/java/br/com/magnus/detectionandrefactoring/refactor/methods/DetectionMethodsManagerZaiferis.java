package br.com.magnus.detectionandrefactoring.refactor.methods;

import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.detectionandrefactoring.configuration.RefactoringProperties;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.ZafeirisEtAl2016;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.ZafeirisEtAl2016Candidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Detection methods manager for Zafeiris et al. 2016 extract method refactoring.
 * <p>
 * Detects and refactors extract method opportunities based on super method
 * invocations as described by Zafeiris et al.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DetectionMethodsManagerZaiferis implements DetectionMethodsManager {

    /**
     * Executor key for Zafeiris refactoring operations.
     */
    private static final String ZAFEIRIS_EXECUTOR_KEY = "zafeiris";

    private final ZafeirisEtAl2016 zafeirisEtAl2016;
    private final RefactoringProperties refactoringProperties;

    @Qualifier("zafeirisRefactoringExecutor")
    private final Executor zafeirisRefactoringExecutor;

    @Override
    public List<RefactorFiles> refactor(Project project) {
        var candidates = zafeirisEtAl2016.extractCandidates(project.getOriginalContent());

        if (hasNoCandidates(candidates)) {
            log.info("No candidates found for Zafeiris");
            return List.of();
        }

        var candidatesGroup = groupCandidatesByParentType(candidates);
        log.info("Candidates for Zafeiris {}", candidatesGroup.keySet());

        var refactoredFiles = executeRefactoring(project, candidatesGroup);
        log.info("Candidates Refactored with success");
        return refactoredFiles;
    }

    /**
     * Groups candidates by their parent type.
     * <p>
     * Uses Stream groupingBy for cleaner, more efficient grouping.
     *
     * @param candidates the candidates to group
     * @return map of parent type to list of candidates
     */
    private Map<String, List<RefactoringCandidate>> groupCandidatesByParentType(List<RefactoringCandidate> candidates) {
        return candidates.stream()
                .filter(ZafeirisEtAl2016Candidate.class::isInstance)
                .map(ZafeirisEtAl2016Candidate.class::cast)
                .collect(Collectors.groupingBy(
                        ZafeirisEtAl2016Candidate::getParentType,
                        Collectors.toList()));
    }

    /**
     * Executes refactoring for all candidate groups in parallel.
     *
     * @param project           the project to refactor
     * @param candidatesByGroup map of parent type to candidates
     * @return list of refactored file groups
     */
    private List<RefactorFiles> executeRefactoring(Project project, Map<String, List<RefactoringCandidate>> candidatesByGroup) {
        return DetectionMethodsManager.executeInParallel(
                        candidatesByGroup.entrySet().stream().toList(),
                        zafeirisRefactoringExecutor,
                        refactoringProperties.getParallelism(ZAFEIRIS_EXECUTOR_KEY),
                        entry -> refactorCandidateGroup(project, entry))
                .stream()
                .toList();
    }

    /**
     * Refactors a group of candidates sharing the same parent type.
     *
     * @param project the project to refactor
     * @param entry   map entry containing parent type and associated candidates
     * @return refactored files
     */
    private RefactorFiles refactorCandidateGroup(Project project, Map.Entry<String, List<RefactoringCandidate>> entry) {
        var refactorFiles = RefactorFiles.builder()
                .files(RefactoringUtils.cloneProjectFiles(project))
                .candidates(entry.getValue())
                .build();
        zafeirisEtAl2016.refactor(refactorFiles);
        return refactorFiles;
    }
}
