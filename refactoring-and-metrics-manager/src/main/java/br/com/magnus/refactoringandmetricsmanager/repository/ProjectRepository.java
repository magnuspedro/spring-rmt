package br.com.magnus.refactoringandmetricsmanager.repository;


import br.com.magnus.config.starter.projects.BaseProject;
import org.springframework.data.repository.CrudRepository;

public interface ProjectRepository extends CrudRepository<BaseProject, String> {
}
