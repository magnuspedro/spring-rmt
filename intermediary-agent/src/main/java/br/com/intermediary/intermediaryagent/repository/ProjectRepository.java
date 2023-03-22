package br.com.intermediary.intermediaryagent.repository;

import br.com.messages.projects.Project;
import org.springframework.data.repository.CrudRepository;

public interface ProjectRepository extends CrudRepository<Project, String> {
}
