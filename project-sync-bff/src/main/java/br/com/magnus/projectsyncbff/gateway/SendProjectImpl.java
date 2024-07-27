package br.com.magnus.projectsyncbff.gateway;

import br.com.magnus.projectsyncbff.configuration.SqsProperties;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendProjectImpl implements SendProject {
    private final SqsTemplate sqsTemplate;

    private final SqsProperties sqsProperties;

    @Override
    public void send(String id) {
        log.info("Sending message {} to Queue {}", id, sqsProperties.detectPattern());

        sqsTemplate.sendAsync(to -> to.queue(sqsProperties.detectPattern())
                .payload(id));
    }
}
