package br.com.magnus.detectionandrefactoring.refactor.methods;

import br.com.magnus.config.starter.file.JavaFile;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class DetectionMethodsManagerZaiferis implements DetectionMethodsManager {

    private final ZafeirisEtAl2016 zafeirisEtAl2016;
    private final RefactoringProperties refactoringProperties;
    @Qualifier("zafeirisRefactoringExecutor")
    private final Executor zafeirisRefactoringExecutor;

    @Override
    public List<RefactorFiles> refactor(Project project) {
        var candidates = zafeirisEtAl2016.extractCandidates(project.getOriginalContent());

        if (hasNoCandidates(candidates)) {
            log.info("No candidates found for zafeiris");
            return List.of();
        }
        var candidatesGroup = groupCandidates(candidates);
        log.info("Candidates for zafeiris {}", candidatesGroup.keySet());

        var refactoredFiles = this.refactor(project.getOriginalContent(), candidatesGroup);
        log.info("Candidates Refactored with success");
        return refactoredFiles;
    }

    private List<RefactorFiles> refactor(List<JavaFile> javaFiles, HashMap<String, List<RefactoringCandidate>> candidates) {
        return DetectionMethodsManager.executeInParallel(
                        new ArrayList<>(candidates.entrySet()),
                        zafeirisRefactoringExecutor,
                        refactoringProperties.getParallelism("zafeiris"),
                        entry -> {
                    var files = javaFiles.stream().map(JavaFile::clone).collect(Collectors.toCollection(ArrayList::new));
                    var refactorFiles = RefactorFiles.builder()
                            .files(files)
                            .candidates(entry.getValue())
                            .build();
                    zafeirisEtAl2016.refactor(refactorFiles);
                    return refactorFiles;
                })
                .stream()
                .toList();
    }

    private HashMap<String, List<RefactoringCandidate>> groupCandidates(List<RefactoringCandidate> refactoringCandidates) {
        var candidatesGroup = new HashMap<String, List<RefactoringCandidate>>();
        for (var refactoringCandidate : refactoringCandidates) {
            if (refactoringCandidate instanceof ZafeirisEtAl2016Candidate candidate) {
                var parentType = candidate.getParentType();
                if (candidatesGroup.containsKey(parentType)) {
                    var group = candidatesGroup.get(parentType);
                    group.add(candidate);
                } else {
                    candidatesGroup.put(parentType, new ArrayList<>(List.of(candidate)));
                }
            }
        }
        return candidatesGroup;
    }

}
