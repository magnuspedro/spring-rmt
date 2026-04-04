package br.com.magnus.detectionandrefactoring.repository;

import br.com.magnus.config.starter.projects.BaseProject;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for accessing project data from Redis.
 * <p>
 * Provides CRUD operations for project persistence using Redis as the backend.
 */
@EnableRedisRepositories
public interface ProjectRepository extends CrudRepository<BaseProject, String> {
}
