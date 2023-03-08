package br.com.detection.detectionagent.methods;

import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;

import java.util.Collection;

public interface DetectionMethodsManager {

	/**
	 * Extract the refactoring candidates.
	 * @param projectId
	 * @return return the a Map of Collections of refactoring candidates.
	 */
	Collection<RefactoringCandidate> extractCandidates(String projectId);

	String refactor(String projectId, Collection<RefactoringCandidadeDTO> eligiblePatterns);
	
	Collection<Reference> getReferences();
	

}
