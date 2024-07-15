package br.com.magnus.projectsyncbff.refactor;


import br.com.magnus.config.starter.projects.Project;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public interface RefactorProject {
    void process(Project project);

    ProjectResults retrieve(String id);

    @Retryable(retryFor = ResponseStatusException.class, maxAttemptsExpression = "${retry.max-attempts}", backoff = @Backoff(delayExpression = "${retry.delay}", multiplierExpression = "${retry.multiplier}"))
    ProjectResults retrieveRetryable(String id);

    String downloadProject(String projectId, List<String> candidatesIds);
}
