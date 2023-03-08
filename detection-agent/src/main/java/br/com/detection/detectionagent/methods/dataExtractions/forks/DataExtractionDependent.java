package br.com.detection.detectionagent.methods.dataExtractions.forks;

import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;

import java.util.Collection;

public interface DataExtractionDependent {

	Collection<RefactoringCandidate> extractCandidates(DataHandler dataHandler);

	void refactor(DataHandler dataHandler, RefactoringCandidate candidates);

	Reference toReference();

}
