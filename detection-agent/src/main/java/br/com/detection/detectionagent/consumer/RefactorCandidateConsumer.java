package br.com.detection.detectionagent.consumer;

import br.com.detection.detectionagent.refactor.methods.DetectionMethodsManager;
import br.com.detection.detectionagent.repository.ProjectUpdater;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefactorCandidateConsumer {
    private final DetectionMethodsManager detectionMethodsManager;
    private final ProjectUpdater projectUpdater;

    @SqsListener("${sqs.detect-pattern}")
    public void listener(String id) {
        log.info("Message received id: {}", id);
        var project = detectionMethodsManager.extractCandidates(id);

        projectUpdater.updateStatus(project);

    }
}