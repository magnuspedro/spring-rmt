package br.com.detection.detectionagent.refactor.methods;

import br.com.detection.detectionagent.refactor.methods.zaiferisVE.ZafeirisEtAl2016;
import br.com.detection.detectionagent.refactor.methods.zaiferisVE.ZafeirisEtAl2016Candidate;
import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class DetectionMethodsManagerZaiferis implements DetectionMethodsManager {

    private final ZafeirisEtAl2016 zafeirisEtAl2016;

    @Override
    public void refactor(Project project) {
        var candidates = zafeirisEtAl2016.extractCandidates(project.getOriginalContent());

        if (hasNoCandidates(candidates)) {
            log.info("No candidates found for zafeiris");
            return;
        }
        var candidatesGroup = groupCandidates(candidates);
        log.info("Candidates for zafeiris {}", candidatesGroup.keySet());

        var refactoredFiles = this.refactor(project.getOriginalContent(), candidatesGroup);
        project.setRefactorFiles(refactoredFiles);
        log.info("Candidates Refactored with success");
    }

    private List<RefactorFiles> refactor(List<JavaFile> javaFiles, HashMap<String, List<RefactoringCandidate>> candidates) {
        final var toRefactorList = new ArrayList<RefactorFiles>();
        for (var parent : candidates.keySet()) {
            var files = javaFiles.stream().map(JavaFile::clone).collect(Collectors.toCollection(ArrayList::new));
            var refactorFiles = RefactorFiles.builder()
                    .files(files)
                    .candidates(candidates.get(parent))
                    .build();
            toRefactorList.add(refactorFiles);
            zafeirisEtAl2016.refactor(refactorFiles);
        }
        return toRefactorList;
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
