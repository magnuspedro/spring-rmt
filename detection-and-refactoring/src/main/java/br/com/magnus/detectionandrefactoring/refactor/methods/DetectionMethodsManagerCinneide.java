package br.com.magnus.detectionandrefactoring.refactor.methods;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideEtAl2000;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetectionMethodsManagerCinneide implements DetectionMethodsManager {

    private final CinneideEtAl2000 cinneideEtAl2000;

    @Override
    public void refactor(Project project) {
        final var candidates = cinneideEtAl2000.extractCandidates(project.getOriginalContent());

        if (hasNoCandidates(candidates)) {
            log.info("No candidates found for Cinneide");
            return;
        }

        final var toRefactorList = new ArrayList<RefactorFiles>();
        for (var candidate : candidates) {
            try {
                final var files = project.getOriginalContent().stream()
                        .map(JavaFile::clone)
                        .collect(Collectors.toCollection(ArrayList::new));
                final var refactorFiles = RefactorFiles.builder()
                        .files(files)
                        .candidates(List.of(candidate))
                        .build();
                cinneideEtAl2000.refactor(refactorFiles);
                if (!refactorFiles.filesChanged().isEmpty()) {
                    toRefactorList.add(refactorFiles);
                }
            } catch (RuntimeException ex) {
                log.warn("Skipping invalid Cinneide candidate {} due to transformation error: {}", candidate.getClassName(), ex.getMessage());
            }
        }

        if (toRefactorList.isEmpty()) {
            log.info("No candidates successfully refactored for Cinneide");
            return;
        }
        log.info("Candidates for Cinneide {}", candidates.stream().map(RefactoringCandidate::getClassName).toList());
        project.addAllRefactorFiles(toRefactorList);
        log.info("Candidates Refactored with success");
    }
}
