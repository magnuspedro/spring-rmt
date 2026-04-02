package br.com.magnus.projectsyncbff.configuration;

import com.github.sonus21.rqueue.core.RqueueEndpointManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "rqueue.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class RedisQueueProperties {

    private final RqueueEndpointManager rqueueEndpointManager;
    private final QueueProperties queueProperties;
    private boolean isRegistered = false;

    public String getDetectionMethod() {
        if (!isRegistered && !rqueueEndpointManager.isQueueRegistered(queueProperties.detectPattern())) {
            log.info("Registering Queue {}", queueProperties.detectPattern());
            rqueueEndpointManager.registerQueue(queueProperties.detectPattern());
            isRegistered = true;
        }
        return queueProperties.detectPattern();
    }
}
