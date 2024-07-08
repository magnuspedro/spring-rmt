package br.com.magnus.projectsyncbff.gateway;


import br.com.magnus.config.starter.projects.Project;

public interface SendProject {
    void send(Project project);
}
