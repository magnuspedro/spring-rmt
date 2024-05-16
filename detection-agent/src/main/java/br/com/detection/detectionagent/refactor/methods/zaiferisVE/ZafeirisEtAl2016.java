package br.com.detection.detectionagent.refactor.methods.zaiferisVE;

import br.com.detection.detectionagent.refactor.dataExtractions.ExtractionMethodFactory;
import br.com.detection.detectionagent.refactor.dataExtractions.ast.AbstractSyntaxTreeExtraction;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.detection.detectionagent.refactor.methods.zaiferisVE.executors.ZafeirisEtAl2016Executor;
import br.com.detection.detectionagent.refactor.methods.zaiferisVE.verifiers.ZafeirisEtAl2016Verifier;
import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.patterns.DesignPattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ZafeirisEtAl2016 implements AbstractSyntaxTreeExtraction {

    private final ZafeirisEtAl2016Verifier zafeirisEtAl2016Verifier;

    private final ExtractionMethodFactory extractionMethodFactory;

    private final ZafeirisEtAl2016Executor zafeirisEtAl2016Executor;

    @Getter
    private final Set<DesignPattern> designPatterns = Set.of(DesignPattern.TEMPLATE_METHOD);

    public List<RefactoringCandidate> extractCandidates(List<JavaFile> javaFiles) {
        extractionMethodFactory.build(this).parseAll(javaFiles);
        return zafeirisEtAl2016Verifier.retrieveCandidatesFrom(javaFiles).stream()
                .map(RefactoringCandidate.class::cast)
                .toList();
    }

    public void refactor(RefactorFiles refactorFiles) {
        this.zafeirisEtAl2016Executor.refactor(refactorFiles);
    }
}
