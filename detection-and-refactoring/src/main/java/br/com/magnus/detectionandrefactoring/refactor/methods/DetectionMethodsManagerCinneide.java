package br.com.magnus.detectionandrefactoring.refactor.methods;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.detectionandrefactoring.configuration.RefactoringProperties;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideEtAl2000;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetectionMethodsManagerCinneide implements DetectionMethodsManager {

    private final CinneideEtAl2000 cinneideEtAl2000;
    private final RefactoringProperties refactoringProperties;
    @Qualifier("cinneideRefactoringExecutor")
    private final Executor cinneideRefactoringExecutor;

    @Override
    public List<RefactorFiles> refactor(Project project) {
        final var candidates = cinneideEtAl2000.extractCandidates(project.getOriginalContent());

        if (hasNoCandidates(candidates)) {
            log.info("No candidates found for Cinneide");
            return List.of();
        }

        final var toRefactorList = DetectionMethodsManager.executeInParallel(
                        candidates,
                        cinneideRefactoringExecutor,
                        refactoringProperties.getParallelism("cinneide"),
                        candidate -> {
                    try {
                        final var files = project.getOriginalContent().stream()
                                .map(JavaFile::clone)
                                .collect(Collectors.toCollection(ArrayList::new));
                        final var refactorFiles = RefactorFiles.builder()
                                .files(files)
                                .candidates(List.of(candidate))
                                .build();
                        cinneideEtAl2000.refactor(refactorFiles);
                        return refactorFiles.filesChanged().isEmpty() ? null : refactorFiles;
                    } catch (RuntimeException ex) {
                        log.warn("Skipping invalid Cinneide candidate {} due to transformation error: {}", candidate.getClassName(), ex.getMessage());
                        return null;
                    }
                }).stream()
                .filter(java.util.Objects::nonNull)
                .toList();

        if (toRefactorList.isEmpty()) {
            log.info("No candidates successfully refactored for Cinneide");
            return List.of();
        }
        log.info("Candidates for Cinneide {}", candidates.stream().map(RefactoringCandidate::getClassName).toList());
        log.info("Candidates Refactored with success");
        return toRefactorList;
    }
}
