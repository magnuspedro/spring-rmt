package br.com.magnus.refactoringandmetricsmanager.repository;


import br.com.magnus.config.starter.projects.BaseProject;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.CrudRepository;

@EnableRedisRepositories
public interface ProjectRepository extends CrudRepository<BaseProject, String> {
}
