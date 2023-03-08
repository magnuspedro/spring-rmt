package br.com.metrics.metricsagent.project;

import br.com.messages.projects.Project;
import br.com.metrics.metricsagent.domain.files.FileRepositoryCollections;

import java.io.Serializable;
import java.util.Optional;

public interface ProjectsReadonlyRepository extends Serializable {

	Optional<Project> get(FileRepositoryCollections collection, String id);

}
