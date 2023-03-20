package br.com.detection.detectionagent.methods.dataExtractions;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public interface DataExtractionApproach {
	Collection<Object> parseAll(Path... files);
	
	Optional<Object> parseSingle(Path file);
	
}
