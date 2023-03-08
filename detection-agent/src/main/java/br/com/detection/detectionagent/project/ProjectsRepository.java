package br.com.detection.detectionagent.project;

import br.com.detection.detectionagent.domain.files.FileRepositoryCollections;
import br.com.messages.projects.Project;

public interface ProjectsRepository extends ProjectsReadonlyRepository {
	
	void put(FileRepositoryCollections collection, Project project);

	void remove(FileRepositoryCollections collection, Project project);

}
