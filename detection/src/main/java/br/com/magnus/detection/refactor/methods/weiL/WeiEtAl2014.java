package br.com.magnus.detection.refactor.methods.weiL;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.detection.refactor.dataExtractions.ExtractionMethodFactory;
import br.com.magnus.detection.refactor.dataExtractions.ast.AbstractSyntaxTreeExtraction;
import br.com.magnus.detection.refactor.methods.weiL.executors.WeiEtAl2014Executor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class WeiEtAl2014 implements AbstractSyntaxTreeExtraction {

    private final List<RefactoringCandidatesVerifier> refactoringCandidatesVerifier;
    private final ExtractionMethodFactory extractionMethodFactory;
    @Getter
    private final Set<DesignPattern> designPatterns = Set.of(DesignPattern.STRATEGY, DesignPattern.FACTORY_METHOD);
    private final List<WeiEtAl2014Executor> executors;

    public List<RefactoringCandidate> extractCandidates(List<JavaFile> javaFiles) {
        this.extractionMethodFactory.build(this).parseAll(javaFiles);
        return this.refactoringCandidatesVerifier.stream()
                .map(f -> f.retrieveCandidatesFrom(javaFiles))
                .flatMap(List::stream)
                .toList();
    }

    public void refactor(RefactorFiles refactorFiles) {
        this.getExecutors()
                .filter(e -> e.isApplicable(refactorFiles.candidate()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .refactor(refactorFiles);
    }

    private Stream<WeiEtAl2014Executor> getExecutors() {
        return executors.stream();
    }
}
