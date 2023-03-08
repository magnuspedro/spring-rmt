package br.com.detection.detectionagent.project;

import br.com.detection.detectionagent.domain.files.FileRepositoryCollections;
import br.com.messages.projects.Project;

import java.io.Serializable;
import java.util.Optional;

public interface ProjectsReadonlyRepository extends Serializable {

	Optional<Project> get(FileRepositoryCollections collection, String id);

}
