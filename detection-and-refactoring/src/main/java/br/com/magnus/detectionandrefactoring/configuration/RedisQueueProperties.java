package br.com.magnus.detectionandrefactoring.configuration;

import com.github.sonus21.rqueue.core.RqueueEndpointManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Manages Redis queue registration and configuration.
 * <p>
 * Ensures the measurement queue is registered with RQueue before use.
 * Only active when the {@code rqueue.enabled} property is true.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "rqueue.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class RedisQueueProperties {

    /**
     * RQueue endpoint manager for queue registration.
     */
    private final RqueueEndpointManager rqueueEndpointManager;

    /**
     * Queue configuration properties.
     */
    private final QueueProperties queueProperties;

    /**
     * Tracks whether the queue has been registered.
     */
    private boolean isRegistered = false;

    /**
     * Gets the measurement queue pattern, registering the queue if needed.
     *
     * @return the measurement queue pattern
     */
    public String getMeasurePattern() {
        if (!isRegistered && !rqueueEndpointManager.isQueueRegistered(queueProperties.measurePattern())) {
            log.info("Registering Queue {}", queueProperties.measurePattern());
            rqueueEndpointManager.registerQueue(queueProperties.measurePattern());
            isRegistered = true;
        }
        return queueProperties.measurePattern();
    }
}
