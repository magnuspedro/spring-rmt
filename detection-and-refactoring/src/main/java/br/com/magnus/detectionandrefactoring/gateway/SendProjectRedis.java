package br.com.magnus.detectionandrefactoring.gateway;

import br.com.magnus.config.starter.message.Message;
import br.com.magnus.detectionandrefactoring.configuration.RedisQueueProperties;
import com.github.sonus21.rqueue.core.RqueueMessageEnqueuer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Redis RQueue implementation of {@link SendProject}.
 * <p>
 * Uses Redis lists as a queue for project identifiers.
 * Active only when {@code rqueue.enabled} is true.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "rqueue.enabled", havingValue = "true")
public class SendProjectRedis implements SendProject {

    /**
     * RQueue message enqueuer for sending messages.
     */
    private final RqueueMessageEnqueuer rqueueMessageEnqueuer;

    /**
     * Queue configuration properties.
     */
    private final RedisQueueProperties queueProperties;

    @Override
    public void send(String id) {
        log.info("Sending message {} to Queue {}", id, queueProperties.getMeasurePattern());
        rqueueMessageEnqueuer.enqueue(queueProperties.getMeasurePattern(), new Message(id));
    }
}
