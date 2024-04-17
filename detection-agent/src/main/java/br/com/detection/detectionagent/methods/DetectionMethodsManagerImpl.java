package br.com.detection.detectionagent.methods;

import br.com.detection.detectionagent.domain.methods.DetectionMethod;
import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.refactor.ExtractFiles;
import br.com.detection.detectionagent.repository.ProjectRepository;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetectionMethodsManagerImpl implements DetectionMethodsManager {

    private final ProjectRepository projectsRepository;

    private final ExtractFiles extractFiles;

    private final List<DetectionMethod> detectionMethod;

    @Override
    public List<RefactoringCandidate> extractCandidates(String projectId) {

        var project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        var javaFiles = this.extractFiles.extract(project);
        var candidates = detectionMethod.stream()
                .map(f -> f.extractCandidates(javaFiles))
                .flatMap(Collection::stream)
                .toList();

        log.info("Candidates {}", candidates.stream().map(RefactoringCandidate::getClassName).toList());

        this.refactor(projectId, javaFiles, candidates);

        return candidates;
    }

    @Override
    public String refactor(String id, List<JavaFile> javaFiles, Collection<RefactoringCandidate> candidates) {
        candidates.forEach(candidate -> detectionMethod
                .forEach(f -> f.refactor(javaFiles, candidate)));
        return id;
    }
}
