package br.com.detection.detectionagent.methods;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;

import java.util.Collection;
import java.util.List;

public interface DetectionMethodsManager {

	List<RefactoringCandidate> extractCandidates(String projectId);

	String refactor(String id, List<JavaFile> javaFiles, Collection<RefactoringCandidate> candidates);

}
