package br.com.detection.detectionagent.refactor.methods;

import br.com.detection.detectionagent.refactor.methods.weiL.WeiEtAl2014;
import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
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

    @Override
    public void refactor(Project project) {
        var candidates = weiEtAl2014.extractCandidates(project.getOriginalContent());

        if (hasNoCandidates(candidates)) {
            log.info("No candidates found for Wei");
            return;
        }

        log.info("Candidates for Wei {}", candidates.stream().map(RefactoringCandidate::getClassName).toList());
        var refactoredFiles = this.refactor(project.getOriginalContent(), candidates);
        project.addAllRefactorFiles(refactoredFiles);
        log.info("Candidates Refactored with success");
    }

    private List<RefactorFiles> refactor(List<JavaFile> javaFiles, List<RefactoringCandidate> candidates) {
        final var toRefactorList = new ArrayList<RefactorFiles>();
        for (var candidate : candidates) {
            var files = javaFiles.stream().map(JavaFile::clone).collect(Collectors.toCollection(ArrayList::new));
            var refactorFiles = RefactorFiles.builder()
                    .files(files)
                    .candidates(List.of(candidate))
                    .build();
            toRefactorList.add(refactorFiles);
            weiEtAl2014.refactor(refactorFiles);
        }
        return toRefactorList;
    }
}
