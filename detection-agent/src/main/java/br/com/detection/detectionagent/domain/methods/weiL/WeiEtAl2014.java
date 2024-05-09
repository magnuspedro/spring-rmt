package br.com.detection.detectionagent.domain.methods.weiL;

import br.com.detection.detectionagent.domain.methods.DetectionMethod;
import br.com.detection.detectionagent.domain.methods.RefactoringCandidatesVerifier;
import br.com.detection.detectionagent.domain.methods.weiL.executors.WeiEtAl2014Executor;
import br.com.detection.detectionagent.methods.dataExtractions.ExtractionMethodFactory;
import br.com.detection.detectionagent.methods.dataExtractions.forks.AbstractSyntaxTreeDependent;
import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.patterns.DesignPattern;
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
    private final List<WeiEtAl2014Executor> executors;

    @Override
    public Collection<RefactoringCandidate> extractCandidates(List<JavaFile> javaFiles) {
        this.extractionMethodFactory.build(this).parseAll(javaFiles);
        return this.refactoringCandidatesVerifier.stream()
                .map(f -> f.retrieveCandidatesFrom(javaFiles))
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public void refactor(List<JavaFile> javaFiles, RefactoringCandidate candidate) {
        this.getExecutors()
                .filter(e -> e.isApplicable(candidate))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .refactor(candidate, javaFiles);
    }

    private Stream<WeiEtAl2014Executor> getExecutors() {
        return executors.stream();
    }

    @Override
    public Set<DesignPattern> getDesignPatterns() {
        return designPatterns;
    }

    @Override
    public boolean supports(RefactoringCandidate refactoringCandidate) {
        return refactoringCandidate instanceof WeiEtAl2014Candidate;
    }

}
