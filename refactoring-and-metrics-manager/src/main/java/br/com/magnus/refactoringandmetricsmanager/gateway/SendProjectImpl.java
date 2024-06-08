package br.com.magnus.refactoringandmetricsmanager.gateway;

import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.refactoringandmetricsmanager.configuration.SqsProperties;
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
    public void send(Project project) {
        log.info("Sending message {} to Queue {}", project.getId(), sqsProperties.detectPattern());

        sqsTemplate.sendAsync(to -> to.queue(sqsProperties.detectPattern())
                .payload(project.getId()));
    }
}
