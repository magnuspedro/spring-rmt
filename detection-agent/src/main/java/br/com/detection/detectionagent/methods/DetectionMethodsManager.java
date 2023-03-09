package br.com.detection.detectionagent.methods;

import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;

import java.util.Collection;
import java.util.List;

public interface DetectionMethodsManager {

	List<RefactoringCandidate> extractCandidates(String projectId);

	String refactor(String projectId, Collection<RefactoringCandidadeDTO> eligiblePatterns);
	
	Collection<Reference> getReferences();
	

}
