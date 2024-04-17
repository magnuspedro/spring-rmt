package br.com.detection.detectionagent.domain.methods.zeiferisVE;

import br.com.detection.detectionagent.domain.methods.DetectionMethod;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.executors.ZafeirisEtAl2016Executor;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.verifiers.ZafeirisEtAl2016Verifier;
import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.ExtractionMethodFactory;
import br.com.detection.detectionagent.methods.dataExtractions.forks.AbstractSyntaxTreeDependent;
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
        var extractMethod = extractionMethodFactory.build(this);
        new ZafeirisEtAl2016Executor().refactor((ZafeirisEtAl2016Candidate) candidate, javaFiles, extractMethod);
    }

    ;

    @Override
    public Set<DesignPattern> getDesignPatterns() {
        return designPatterns;
    }

}
