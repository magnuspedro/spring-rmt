package br.com.magnus.detectionandrefactoring.consumer;

import br.com.magnus.config.starter.message.Message;
import com.github.sonus21.rqueue.annotation.RqueueListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Redis RQueue consumer for processing refactoring candidates.
 * <p>
 * Listens for messages on the configured Redis queue and triggers
 * refactoring processing for each received message.
 * Active only when {@code rqueue.enabled} is true.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "rqueue.enabled", havingValue = "true")
public class RefactorCandidateRedisConsumer {

    /**
     * Processor for refactoring candidates.
     */
    private final ProcessRefactorCandidate processRefactorCandidate;

    /**
     * Listens for RQueue messages and processes refactoring candidates.
     *
     * @param message the message containing the project ID
     */
    @RqueueListener(value = "${queue.detect-pattern}")
    public void listener(Message message) {
        processRefactorCandidate.process(message.id());
    }
}
