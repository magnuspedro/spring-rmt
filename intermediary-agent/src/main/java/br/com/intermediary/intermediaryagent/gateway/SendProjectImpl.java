package br.com.intermediary.intermediaryagent.gateway;

import br.com.intermediary.intermediaryagent.configuration.SqsProperties;
import br.com.messages.projects.Project;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendProjectImpl implements SendProject {
    private final QueueMessagingTemplate queueMessagingTemplate;

    private final SqsProperties sqsProperties;

    @Override
    public void send(Project project) {
        log.info("Sending message {} to Queue {}", project.getId(), sqsProperties.detectPattern());

        queueMessagingTemplate.convertAndSend(sqsProperties.detectPattern(), project.getId());
    }
}
