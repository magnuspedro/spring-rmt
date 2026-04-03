package br.com.magnus.detectionandrefactoring.refactor.methods;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.detectionandrefactoring.configuration.RefactoringProperties;
import br.com.magnus.detectionandrefactoring.refactor.methods.weiL.WeiEtAl2014;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetectionMethodsManagerWei implements DetectionMethodsManager {

    private final WeiEtAl2014 weiEtAl2014;
    private final RefactoringProperties refactoringProperties;

    @Override
    public List<RefactorFiles> refactor(Project project) {
        var candidates = weiEtAl2014.extractCandidates(project.getOriginalContent());

        if (hasNoCandidates(candidates)) {
            log.info("No candidates found for Wei");
            return List.of();
        }

        log.info("Candidates for Wei {}", candidates.stream().map(RefactoringCandidate::getClassName).toList());
        var refactoredFiles = this.refactor(project.getOriginalContent(), candidates);
        log.info("Candidates Refactored with success");
        return refactoredFiles;
    }

    private List<RefactorFiles> refactor(List<JavaFile> javaFiles, List<RefactoringCandidate> candidates) {
        return executeInParallel(candidates, refactoringProperties.parallelism(), candidate -> {
                    var files = javaFiles.stream().map(JavaFile::clone).collect(Collectors.toCollection(ArrayList::new));
                    var refactorFiles = RefactorFiles.builder()
                            .files(files)
                            .candidates(List.of(candidate))
                            .build();
                    weiEtAl2014.refactor(refactorFiles);
                    return refactorFiles;
                })
                .stream()
                .toList();
    }
}
