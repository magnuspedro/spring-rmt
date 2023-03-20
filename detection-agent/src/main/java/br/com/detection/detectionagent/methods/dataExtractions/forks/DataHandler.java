package br.com.detection.detectionagent.methods.dataExtractions.forks;

import java.nio.file.Path;
import java.util.Collection;

public interface DataHandler {

	Collection<Object> getParsedFiles();

	Collection<Path> getFiles();

	Object parseFile(Path file);

	Path getFile(Object parsedEntity);

	Object getParsedFileByName(String name);

}
