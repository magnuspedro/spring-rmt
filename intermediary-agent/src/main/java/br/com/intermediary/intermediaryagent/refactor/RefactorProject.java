package br.com.intermediary.intermediaryagent.refactor;


import br.com.magnus.config.starter.projects.Project;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.server.ResponseStatusException;

public interface RefactorProject {
    Project process(Project project);

    ProjectResults retrieve(String id);

    @Retryable(retryFor = ResponseStatusException.class, maxAttemptsExpression = "${retry.max-attempts}", backoff = @Backoff(delayExpression = "${retry.delay}", multiplierExpression = "${retry.multiplier}"))
    ProjectResults retrieveRetryable(String id);
}
