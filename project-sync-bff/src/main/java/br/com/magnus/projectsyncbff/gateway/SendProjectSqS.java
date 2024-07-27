package br.com.magnus.projectsyncbff.gateway;

import br.com.magnus.projectsyncbff.configuration.QueueProperties;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "rqueue.enabled", havingValue = "false")
public class SendProjectSqS implements SendProject {
    private final SqsTemplate sqsTemplate;

    private final QueueProperties queueProperties;

    @Override
    public void send(String id) {
        log.info("Sending message {} to Queue {}", id, queueProperties.detectPattern());

        sqsTemplate.sendAsync(to -> to.queue(queueProperties.detectPattern()).payload(id));
    }
}
