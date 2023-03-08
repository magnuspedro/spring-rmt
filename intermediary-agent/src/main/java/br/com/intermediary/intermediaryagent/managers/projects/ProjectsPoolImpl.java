package br.com.intermediary.intermediaryagent.managers.projects;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import br.com.messages.projects.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectsPoolImpl implements ProjectsPool {

	private static final long serialVersionUID = 1L;
	
	private final Set<Project> projects = new HashSet<>();

	@Override
	public void register(Project project) {
		this.projects.add(project);
	}

	@Override
	public Collection<Project> getAll() {
		return projects;
	}

	@Override
	public Optional<Project> getById(String id) {
		return this.getAll().stream().filter(p -> p.getId().equals(id)).findFirst();
	}

	@Override
	public boolean isRegistered(String projectId) {
		return this.getAll().stream().anyMatch(p -> p.getId().equals(projectId));
	}

}
