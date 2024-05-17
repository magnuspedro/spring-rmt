package br.com.intermediary.intermediaryagent.refactor;


import br.com.magnus.config.starter.projects.Project;
import org.springframework.http.ResponseEntity;

public interface RefactorProject {
    Project process(Project project);

    ResponseEntity<ProjectResults> retrieve(String id);
}
