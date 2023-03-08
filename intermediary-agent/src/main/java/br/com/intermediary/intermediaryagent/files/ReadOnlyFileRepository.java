package br.com.intermediary.intermediaryagent.files;

import java.util.Optional;

import br.com.intermediary.intermediaryagent.files.collections.FileRepositoryCollections;
import br.com.messages.files.FileEntity;

public interface ReadOnlyFileRepository<T extends FileEntity> {

	Optional<T> get(FileRepositoryCollections collection, String id);

}
