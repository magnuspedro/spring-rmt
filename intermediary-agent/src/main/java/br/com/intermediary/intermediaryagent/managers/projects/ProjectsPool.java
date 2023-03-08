package br.com.intermediary.intermediaryagent.managers.projects;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;


import br.com.messages.projects.Project;

public interface ProjectsPool extends Serializable {

	void register(Project project);

	Collection<Project> getAll();

	Optional<Project> getById(String id);

	boolean isRegistered(String projectId);

}
