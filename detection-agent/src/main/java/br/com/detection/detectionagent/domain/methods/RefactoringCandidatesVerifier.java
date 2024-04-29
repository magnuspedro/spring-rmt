package br.com.detection.detectionagent.domain.methods;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;

import java.util.List;

public interface RefactoringCandidatesVerifier {

	List<RefactoringCandidate> retrieveCandidatesFrom(List<JavaFile> javaFiles);

}
