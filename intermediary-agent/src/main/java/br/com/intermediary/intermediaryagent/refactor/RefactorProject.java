package br.com.intermediary.intermediaryagent.refactor;


import br.com.messages.projects.Project;

import java.io.InputStream;

public interface RefactorProject {
    Project process(Project project);

}
