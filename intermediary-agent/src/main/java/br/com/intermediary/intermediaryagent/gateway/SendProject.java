package br.com.intermediary.intermediaryagent.gateway;


import br.com.messages.projects.Project;

public interface SendProject {
    void send(Project project);
}