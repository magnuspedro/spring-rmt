package br.com.magnus.detectionandrefactoring.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "rqueue.enabled", havingValue = "false")
public class RefactorCandidateConsumer {

    private final ProcessRefactorCandidate processRefactorCandidate;

    @SqsListener("${queue.detect-pattern}")
    public void listener(String  id) {
        processRefactorCandidate.process(id);
    }
}