package br.com.detection.detectionagent.refactor.methods;

import br.com.detection.detectionagent.repository.ProjectRepository;
import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.file.extractor.FileExtractor;
import br.com.magnus.config.starter.members.candidates.BasicRefactoringCandidate;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
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

    private final FileExtractor fileExtractor;

    private final List<DetectionMethod> detectionMethod;

    @Override
    public Project extractCandidates(String projectId) {
        var project = (Project) projectsRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        var javaFiles = this.fileExtractor.extract(project);
        var candidates = detectionMethod.stream()
                .map(f -> f.extractCandidates(javaFiles))
                .flatMap(Collection::stream)
                .toList();

        if (candidates.isEmpty()) {
            log.info("No candidates found");
            return project;
        }

        log.info("Candidates {}", candidates.stream().map(RefactoringCandidate::getClassName).toList());
        this.refactor(javaFiles, candidates);
        project.setRefactoringCandidates(BasicRefactoringCandidate.from(candidates));
        project.setRefactoredContent(javaFiles);
        log.info("Candidates Refactored with success");

        return project;
    }

    @Override
    public void refactor(List<JavaFile> javaFiles, Collection<RefactoringCandidate> candidates) {
        candidates.forEach(candidate -> detectionMethod
                .stream()
                .filter(m -> m.supports(candidate))
                .forEach(f -> f.refactor(javaFiles, candidate)));
    }
}
