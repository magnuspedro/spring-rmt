package br.com.intermediary.intermediaryagent.refactor;


import br.com.messages.projects.Project;

public interface RefactorProject {
    Project process(Project project);
}
