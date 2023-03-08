package br.com.metrics.metricsagent.project;

import br.com.messages.projects.Project;
import br.com.metrics.metricsagent.domain.files.FileRepositoryCollections;

public interface ProjectsRepository extends ProjectsReadonlyRepository {

	void put(FileRepositoryCollections collection, Project project);

	void remove(FileRepositoryCollections collection, Project project);

}
