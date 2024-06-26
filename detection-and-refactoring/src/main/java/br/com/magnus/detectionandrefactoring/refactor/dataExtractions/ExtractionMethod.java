package br.com.magnus.detectionandrefactoring.refactor.dataExtractions;

import br.com.magnus.config.starter.file.JavaFile;

import java.util.List;

public interface ExtractionMethod {
	List<Object> parseAll(List<JavaFile> files);
	
	Object parseSingle(JavaFile file);

	Boolean supports(Object object);
}
