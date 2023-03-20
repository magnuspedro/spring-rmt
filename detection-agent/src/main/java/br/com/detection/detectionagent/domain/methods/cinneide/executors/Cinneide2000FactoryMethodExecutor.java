package br.com.detection.detectionagent.domain.methods.cinneide.executors;

import br.com.detection.detectionagent.domain.methods.cinneide.Cinneide2000FactoryMethodCandidate;
import br.com.detection.detectionagent.domain.methods.cinneide.minitransformations.PartialAbstraction;
import br.com.detection.detectionagent.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.patterns.DesignPattern;
import com.github.javaparser.ast.CompilationUnit;

import java.util.Collection;

public class Cinneide2000FactoryMethodExecutor implements Cinneide2000Executor{

	private final PartialAbstraction pa = new PartialAbstraction();
	
	@Override
	public boolean isApplicable(RefactoringCandidate candidate) {
		return candidate instanceof Cinneide2000FactoryMethodCandidate
				&& DesignPattern.FACTORY_METHOD.equals(candidate.getEligiblePattern());
	}

	@Override
	public void refactor(RefactoringCandidate candidate, DataHandler dataHandler) {
		final Cinneide2000FactoryMethodCandidate cinneidCandidate = (Cinneide2000FactoryMethodCandidate) candidate;

		final Collection<CompilationUnit> allClasses = pa.getParsedClasses(dataHandler);
		final CompilationUnit baseCu = pa.updateBaseCompilationUnit(allClasses, cinneidCandidate);		
	}

}
