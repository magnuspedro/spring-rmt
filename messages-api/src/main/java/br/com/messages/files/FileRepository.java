package br.com.messages.files;

public interface FileRepository<T extends FileEntity> extends ReadOnlyFileRepository<T> {

	void put(FileRepositoryCollections collection, T project);

	void remove(FileRepositoryCollections collection, T project);

}
