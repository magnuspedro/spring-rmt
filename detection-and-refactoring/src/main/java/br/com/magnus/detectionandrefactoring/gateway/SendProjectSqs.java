package br.com.magnus.detectionandrefactoring.gateway;

import br.com.magnus.detectionandrefactoring.configuration.QueueProperties;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "rqueue.enabled", havingValue = "false")
public class SendProjectSqs implements SendProject {
    private final SqsTemplate sqsTemplate;

    private final QueueProperties sqsProperties;

    @Override
    public void send(String id) {
        log.info("Sending message {} to Queue {}", id, sqsProperties.measurePattern());

        sqsTemplate.sendAsync(to -> to.queue(sqsProperties.measurePattern()).payload(id));
    }
}
