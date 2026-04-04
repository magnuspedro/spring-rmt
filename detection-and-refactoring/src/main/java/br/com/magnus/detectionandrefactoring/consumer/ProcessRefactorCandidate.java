package br.com.magnus.detectionandrefactoring.consumer;

import br.com.magnus.config.starter.file.extractor.FileExtractor;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.detectionandrefactoring.configuration.RefactoringProperties;
import br.com.magnus.detectionandrefactoring.gateway.SendProject;
import br.com.magnus.detectionandrefactoring.refactor.methods.DetectionMethodsManager;
import br.com.magnus.detectionandrefactoring.repository.ProjectRepository;
import br.com.magnus.detectionandrefactoring.repository.ProjectUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.List;

/**
 * Consumer for processing refactoring candidates.
 * <p>
 * Executes detection methods in parallel using bounded concurrency,
 * aggregates results, and triggers downstream processing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessRefactorCandidate {

    /**
     * All detection method managers to execute.
     */
    private final List<DetectionMethodsManager> detectionMethodsManager;

    /**
     * Updates project data after processing.
     */
    private final ProjectUpdater projectUpdater;

    /**
     * Sends project to downstream processing.
     */
    private final SendProject sendProject;

    /**
     * Repository for project access.
     */
    private final ProjectRepository projectsRepository;

    /**
     * Extracts file content from storage.
     */
    private final FileExtractor fileExtractor;

    /**
     * Executor for parallel processing.
     */
    @Qualifier("detectionMethodsManagerExecutor")
    private final java.util.concurrent.Executor detectionMethodsManagerExecutor;

    /**
     * Refactoring configuration properties.
     */
    private final RefactoringProperties refactoringProperties;

    /**
     * Processes a project by running all detection methods.
     *
     * @param id the project identifier
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if processing fails
     */
    public void process(String id) {
        Assert.notNull(id, "Id cannot be null");
        var startedAt = System.currentTimeMillis();
        log.info("[AUDIT] detection event=start projectId={} timestamp={}", id, Instant.now());
        var project = retrieveProject(id);
        try {
            project.addAllRefactorFiles(executeDetectionMethods(project));
            projectUpdater.saveProject(project);
            sendProject(project);
            log.info("[AUDIT] detection event=complete projectId={} timestamp={} durationMs={}",
                    id, Instant.now(), System.currentTimeMillis() - startedAt);
        } catch (RuntimeException exception) {
            log.warn("[AUDIT] detection event=fail projectId={} timestamp={} durationMs={}",
                    id, Instant.now(), System.currentTimeMillis() - startedAt, exception);
            throw exception;
        }
    }

    /**
     * Executes all detection methods in parallel.
     *
     * @param project the project to analyze
     * @return list of refactored file groups from all methods
     */
    private List<RefactorFiles> executeDetectionMethods(Project project) {
        return DetectionMethodsManager.executeInParallel(
                        detectionMethodsManager,
                        detectionMethodsManagerExecutor,
                        refactoringProperties.getParallelism("detection-methods-manager"),
                        this::refactorMethod)
                .stream()
                .flatMap(List::stream)
                .toList();
    }

    /**
     * Refactors a single detection method.
     *
     * @param method the detection method to execute
     * @return list of refactored files
     */
    private List<RefactorFiles> refactorMethod(DetectionMethodsManager method) {
        return method.refactor(project);
    }

    /**
     * Sends the project for further processing if candidates exist.
     *
     * @param project the project to send
     */
    private void sendProject(Project project) {
        if (project.getStatus().contains(ProjectStatus.NO_CANDIDATES)) {
            return;
        }
        sendProject.send(project.getId());
    }

    /**
     * Retrieves and enriches a project with file content.
     *
     * @param id the project identifier
     * @return enriched project with file content
     * @throws IllegalArgumentException if project not found
     */
    private Project retrieveProject(String id) {
        var baseProject = projectsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        return Project.builder()
                .baseProject(baseProject)
                .originalContent(this.fileExtractor.extract(baseProject))
                .build();
    }
}
