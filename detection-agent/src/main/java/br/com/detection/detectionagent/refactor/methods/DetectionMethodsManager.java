package br.com.detection.detectionagent.refactor.methods;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;

import java.util.Collection;
import java.util.List;

public interface DetectionMethodsManager {

    Project extractCandidates(String projectId);

    void refactor(List<JavaFile> javaFiles, Collection<RefactoringCandidate> candidates);

}
