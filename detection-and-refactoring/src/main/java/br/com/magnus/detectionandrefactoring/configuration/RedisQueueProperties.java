package br.com.magnus.detectionandrefactoring.configuration;

import com.github.sonus21.rqueue.core.RqueueEndpointManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisQueueProperties {

    private final RqueueEndpointManager rqueueEndpointManager;
    private final QueueProperties queueProperties;
    private boolean isRegistered = false;

    public String getMeasurePattern() {
        if (!isRegistered && !rqueueEndpointManager.isQueueRegistered(queueProperties.measurePattern())) {
            log.info("Registering Queue {}", queueProperties.measurePattern());
            rqueueEndpointManager.registerQueue(queueProperties.measurePattern());
            isRegistered = true;
        }
        return queueProperties.measurePattern();
    }
}
