package br.com.metrics.metricsagent.repository;

import br.com.magnus.config.starter.projects.Project;
import org.springframework.data.repository.CrudRepository;

public interface ProjectRepository extends CrudRepository<Project, String> {
}