package br.com.magnus.detectionandrefactoring.gateway;

import br.com.magnus.detectionandrefactoring.configuration.QueueProperties;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * AWS SQS implementation of {@link SendProject}.
 * <p>
 * Uses Amazon SQS for reliable message delivery.
 * Active only when {@code rqueue.enabled} is false and {@code aws.sqs.enabled} is true.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "rqueue.enabled", havingValue = "false")
@ConditionalOnProperty(name = "aws.sqs.enabled", havingValue = "true", matchIfMissing = true)
public class SendProjectSqs implements SendProject {

    /**
     * SQS template for sending messages.
     */
    private final SqsTemplate sqsTemplate;

    /**
     * Queue configuration properties.
     */
    private final QueueProperties sqsProperties;

    @Override
    public void send(String id) {
        log.info("Sending message {} to Queue {}", id, sqsProperties.measurePattern());
        sqsTemplate.sendAsync(to -> to.queue(sqsProperties.measurePattern()).payload(id));
    }
}
