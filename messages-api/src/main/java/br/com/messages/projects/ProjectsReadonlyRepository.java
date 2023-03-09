package br.com.messages.projects;

import br.com.messages.files.FileRepositoryCollections;

import java.io.Serializable;
import java.util.Optional;

public interface ProjectsReadonlyRepository extends Serializable {

    Optional<Project> get(FileRepositoryCollections collection, String id);

}
