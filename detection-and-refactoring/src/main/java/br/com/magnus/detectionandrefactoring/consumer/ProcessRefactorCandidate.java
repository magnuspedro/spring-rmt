package br.com.magnus.detectionandrefactoring.consumer;

import br.com.magnus.config.starter.file.extractor.FileExtractor;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.detectionandrefactoring.gateway.SendProject;
import br.com.magnus.detectionandrefactoring.refactor.methods.DetectionMethodsManager;
import br.com.magnus.detectionandrefactoring.repository.ProjectRepository;
import br.com.magnus.detectionandrefactoring.repository.ProjectUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessRefactorCandidate {

    private final List<DetectionMethodsManager> detectionMethodsManager;
    private final ProjectUpdater projectUpdater;
    private final SendProject sendProject;
    private final ProjectRepository projectsRepository;
    private final FileExtractor fileExtractor;

    public void process(String id) {
        Assert.notNull(id, "Id cannot be null");
        var startedAt = System.currentTimeMillis();
        log.info("[AUDIT] detection event=start projectId={} timestamp={}", id, java.time.Instant.now());
        var project = retrieveProject(id);
        try {
            project.addAllRefactorFiles(detectionMethodsManager.stream()
                    .map(method -> method.refactor(project))
                    .flatMap(List::stream)
                    .toList());
            projectUpdater.saveProject(project);
            send(project);
            log.info("[AUDIT] detection event=complete projectId={} timestamp={} durationMs={}",
                    id, java.time.Instant.now(), System.currentTimeMillis() - startedAt);
        } catch (RuntimeException exception) {
            log.warn("[AUDIT] detection event=fail projectId={} timestamp={} durationMs={}",
                    id, java.time.Instant.now(), System.currentTimeMillis() - startedAt, exception);
            throw exception;
        }
    }

    private void send(Project project) {
        if (project.getStatus().contains(ProjectStatus.NO_CANDIDATES)) {
            return;
        }
        sendProject.send(project.getId());
    }

    private Project retrieveProject(String id) {
        var baseProject = projectsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        return Project.builder()
                .baseProject(baseProject)
                .originalContent(this.fileExtractor.extract(baseProject))
                .build();
    }
}
