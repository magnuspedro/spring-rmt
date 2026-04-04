package br.com.magnus.detectionandrefactoring.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * AWS SQS consumer for processing refactoring candidates.
 * <p>
 * Listens for messages on the configured SQS queue and triggers
 * refactoring processing for each received project ID.
 * Active only when {@code rqueue.enabled} is false.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "rqueue.enabled", havingValue = "false")
public class RefactorCandidateConsumer {

    /**
     * Processor for refactoring candidates.
     */
    private final ProcessRefactorCandidate processRefactorCandidate;

    /**
     * Listens for SQS messages and processes refactoring candidates.
     *
     * @param id the project ID to process
     */
    @SqsListener("${queue.detect-pattern}")
    public void listener(String id) {
        processRefactorCandidate.process(id);
    }
}
