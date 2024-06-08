package br.com.magnus.refactoringandmetricsmanager.gateway;


import br.com.magnus.config.starter.projects.Project;

public interface SendProject {
    void send(Project project);
}
