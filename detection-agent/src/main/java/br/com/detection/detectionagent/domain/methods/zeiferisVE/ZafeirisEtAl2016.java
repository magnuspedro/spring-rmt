package br.com.detection.detectionagent.domain.methods.zeiferisVE;

import br.com.detection.detectionagent.domain.methods.DetectionMethod;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.executors.ZafeirisEtAl2016Executor;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.verifiers.ZafeirisEtAl2016Verifier;
import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.ExtractionMethodFactory;
import br.com.detection.detectionagent.methods.dataExtractions.forks.AbstractSyntaxTreeDependent;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.patterns.DesignPattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
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
        try {
            extractionMethodFactory.build(this).parseAll(javaFiles);
            return zafeirisEtAl2016Verifier.retrieveCandidatesFrom(javaFiles).stream()
                    .map(RefactoringCandidate.class::cast)
                    .toList();
        } catch (MalformedURLException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
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
