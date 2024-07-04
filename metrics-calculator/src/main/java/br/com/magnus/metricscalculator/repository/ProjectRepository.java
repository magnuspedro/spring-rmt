package br.com.magnus.metricscalculator.repository;

import br.com.magnus.config.starter.projects.Project;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.CrudRepository;

@EnableRedisRepositories
public interface ProjectRepository extends CrudRepository<Project, String> {
}
