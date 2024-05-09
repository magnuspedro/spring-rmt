package br.com.detection.detectionagent.domain.methods.zeiferisVE;

import br.com.detection.detectionagent.domain.methods.DetectionMethod;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.executors.ZafeirisEtAl2016Executor;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.verifiers.ZafeirisEtAl2016Verifier;
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

@Component
@RequiredArgsConstructor
public class ZafeirisEtAl2016 implements DetectionMethod, AbstractSyntaxTreeDependent {

    private final ZafeirisEtAl2016Verifier zafeirisEtAl2016Verifier;

    private final ExtractionMethodFactory extractionMethodFactory;

    private final ZafeirisEtAl2016Executor zafeirisEtAl2016Executor;

    private final Set<DesignPattern> designPatterns = Set.of(DesignPattern.TEMPLATE_METHOD);

    @Override
    public Collection<RefactoringCandidate> extractCandidates(List<JavaFile> javaFiles) {
        extractionMethodFactory.build(this).parseAll(javaFiles);
        return zafeirisEtAl2016Verifier.retrieveCandidatesFrom(javaFiles).stream()
                .map(RefactoringCandidate.class::cast)
                .toList();
    }

    @Override
    public void refactor(List<JavaFile> javaFiles, RefactoringCandidate candidate) {
        this.zafeirisEtAl2016Executor.refactor((ZafeirisEtAl2016Candidate) candidate, javaFiles);
    }

    @Override
    public Set<DesignPattern> getDesignPatterns() {
        return designPatterns;
    }

    @Override
    public boolean supports(RefactoringCandidate refactoringCandidate) {
        return refactoringCandidate instanceof ZafeirisEtAl2016Candidate;
    }
}
