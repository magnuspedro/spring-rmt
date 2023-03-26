package br.com.detection.detectionagent.domain.methods;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.ExtractionMethod;
import br.com.messages.members.candidates.RefactoringCandidate;

import java.util.List;

public interface RefactoringCandidatesVerifier {

	public List<RefactoringCandidate> retrieveCandidatesFrom(List<JavaFile> javaFiles, ExtractionMethod extractionMethod);

}
