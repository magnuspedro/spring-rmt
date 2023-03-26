package br.com.detection.detectionagent.methods.dataExtractions;

import br.com.detection.detectionagent.file.JavaFile;

import java.util.List;

public interface ExtractionMethod {
	List<Object> parseAll(List<JavaFile> files);
	
	Object parseSingle(JavaFile file);

	Boolean supports(Object object);
}
