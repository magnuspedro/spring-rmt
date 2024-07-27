package br.com.magnus.projectsyncbff.gateway;

import br.com.magnus.config.starter.message.Message;
import br.com.magnus.projectsyncbff.configuration.QueueProperties;
import br.com.magnus.projectsyncbff.configuration.RedisQueueProperties;
import com.github.sonus21.rqueue.core.RqueueMessageEnqueuer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "rqueue.enabled", havingValue = "true")
public class SendProjectRedis implements SendProject {

    private final RqueueMessageEnqueuer rqueueMessageEnqueuer;
    private final RedisQueueProperties queueProperties;

    @Override
    public void send(String id) {
        log.info("Sending message {} to Queue {}", id, queueProperties.getDetectionMethod());
        rqueueMessageEnqueuer.enqueue(queueProperties.getDetectionMethod(), new Message(id));
    }
}
