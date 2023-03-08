package br.com.detection.detectionagent.domain.methods;

import br.com.detection.detectionagent.domain.methods.details.Author;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.patterns.DesignPattern;

import java.util.List;
import java.util.Set;

public interface DetectionMethod {

	Set<DesignPattern> getDesignPatterns();

	String getTitle();

	int getYear();

	List<Author> getAuthors();

	Reference toReference();

}
