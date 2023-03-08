package br.com.intermediary.intermediaryagent.files.projects;

import java.util.Optional;

import br.com.intermediary.intermediaryagent.files.FileRepository;
import br.com.intermediary.intermediaryagent.files.collections.FileRepositoryCollections;
import br.com.messages.projects.Project;

public interface ProjectsRepository extends FileRepository<Project> {

	Optional<Project> getWithoutContent(FileRepositoryCollections collection, String id);

}
