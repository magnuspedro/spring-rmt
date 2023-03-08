package br.com.detection.detectionagent.domain.methods;

import br.com.detection.detectionagent.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;

import java.util.List;

public interface RefactoringCandidatesVerifier {

	public List<RefactoringCandidate> retrieveCandidatesFrom(Reference reference, DataHandler dataHandler);

}
