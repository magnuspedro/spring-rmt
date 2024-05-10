package br.com.detection.detectionagent.consumer;

import br.com.detection.detectionagent.gateway.SendProject;
import br.com.detection.detectionagent.refactor.methods.DetectionMethodsManager;
import br.com.detection.detectionagent.repository.ProjectUpdater;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefactorCandidateConsumer {
    private final DetectionMethodsManager detectionMethodsManager;
    private final ProjectUpdater projectUpdater;
    private final SendProject sendProject;

    @SqsListener("${sqs.detect-pattern}")
    public void listener(String id) {
        Assert.notNull(id, "Id cannot be null");
        log.info("Message received id: {}", id);
        var project = detectionMethodsManager.extractCandidates(id);
        projectUpdater.saveProject(project);
        send(project);
    }

    private void send(Project project) {
        if (project.getStatus().contains(ProjectStatus.NO_CANDIDATES)) {
            return;
        }
        sendProject.send(project.getId());
    }
}