package br.com.intermediary.intermediaryagent.files;

import br.com.intermediary.intermediaryagent.files.collections.FileRepositoryCollections;
import br.com.messages.files.FileEntity;

public interface FileRepository<T extends FileEntity> extends ReadOnlyFileRepository<T> {

	void put(FileRepositoryCollections collection, T project);

	void remove(FileRepositoryCollections collection, T project);

}
