package br.com.intermediary.intermediaryagent.refactor;


import br.com.magnus.config.starter.projects.Project;

public interface RefactorProject {
    Project process(Project project);

    ProjectResults retrieve(String id);

    ProjectResults retrieveRetry(String id, int retry);
}
