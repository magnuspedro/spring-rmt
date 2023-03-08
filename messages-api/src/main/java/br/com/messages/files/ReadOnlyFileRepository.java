package br.com.messages.files;

import java.util.Optional;

public interface ReadOnlyFileRepository<T extends FileEntity> {

	Optional<T> get(FileRepositoryCollections collection, String id);

}
