package br.com.detection.detectionagent.domain.methods.weiL;

import br.com.detection.detectionagent.domain.methods.DetectionMethod;
import br.com.detection.detectionagent.domain.methods.RefactoringCandidatesVerifier;
import br.com.detection.detectionagent.domain.methods.weiL.executors.WeiEtAl2014Executor;
import br.com.detection.detectionagent.domain.methods.weiL.executors.WeiEtAl2014FactoryExecutor;
import br.com.detection.detectionagent.domain.methods.weiL.executors.WeiEtAl2014StrategyExecutor;
import br.com.detection.detectionagent.domain.methods.weiL.verifiers.WeiEtAl2014FactoryVerifier;
import br.com.detection.detectionagent.domain.methods.weiL.verifiers.WeiEtAl2014StrategyVerifier;
import br.com.detection.detectionagent.domain.methods.weiL.verifiers.WeiEtAl2014Verifier;
import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.ExtractionMethodFactory;
import br.com.detection.detectionagent.methods.dataExtractions.forks.AbstractSyntaxTreeDependent;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.patterns.DesignPattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class WeiEtAl2014 implements DetectionMethod, AbstractSyntaxTreeDependent {

    private final List<RefactoringCandidatesVerifier> refactoringCandidatesVerifier;

    private final ExtractionMethodFactory extractionMethodFactory;
    private final Set<DesignPattern> designPatterns = Set.of(DesignPattern.TEMPLATE_METHOD);

    @Override
    public Collection<RefactoringCandidate> extractCandidates(List<JavaFile> javaFiles) {
        var extractionMethod = this.extractionMethodFactory.build(this);
        return this.refactoringCandidatesVerifier.stream()
                .map(f -> f.retrieveCandidatesFrom(javaFiles, extractionMethod))
                .flatMap(List::stream)
                .toList();
    }

    private Stream<WeiEtAl2014Verifier> getVerifiers() {
        return Stream.of(new WeiEtAl2014FactoryVerifier(), new WeiEtAl2014StrategyVerifier());
    }

    @Override
    public void refactor(List<JavaFile> javaFiles, RefactoringCandidate candidate) {
        var extractionMethod = this.extractionMethodFactory.build(this);
        this.getExecutors().filter(e -> e.isApplicable(candidate)).findFirst()
                .orElseThrow(IllegalArgumentException::new).refactor(candidate, javaFiles, extractionMethod);
    }

    private Stream<WeiEtAl2014Executor> getExecutors() {
        return Stream.of(new WeiEtAl2014FactoryExecutor(), new WeiEtAl2014StrategyExecutor());
    }

    @Override
    public Set<DesignPattern> getDesignPatterns() {
        return designPatterns;
    }

}
