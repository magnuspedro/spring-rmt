package br.com.messages.projects;

import java.util.Optional;

import br.com.messages.files.FileRepository;
import br.com.messages.files.FileRepositoryCollections;

public interface ProjectsRepository extends ProjectsReadonlyRepository, FileRepository<Project> {

	Optional<Project> get(FileRepositoryCollections collection, String id);

	Optional<Project> getWithoutContent(FileRepositoryCollections collection, String id);

	void put(FileRepositoryCollections collection, Project project);

	void remove(FileRepositoryCollections collection, Project project);
}
