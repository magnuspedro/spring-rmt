package br.com.detection.detectionagent.domain.methods.weiL;

import br.com.detection.detectionagent.domain.methods.DetectionMethod;
import br.com.detection.detectionagent.domain.methods.details.Author;
import br.com.detection.detectionagent.domain.methods.weiL.executors.WeiEtAl2014Executor;
import br.com.detection.detectionagent.domain.methods.weiL.executors.WeiEtAl2014StrategyExecutor;
import br.com.detection.detectionagent.domain.methods.weiL.verifiers.WeiEtAl2014FactoryVerifier;
import br.com.detection.detectionagent.domain.methods.weiL.verifiers.WeiEtAl2014StrategyVerifier;
import br.com.detection.detectionagent.domain.methods.weiL.verifiers.WeiEtAl2014Verifier;
import br.com.detection.detectionagent.methods.dataExtractions.forks.AbstractSyntaxTreeDependent;
import br.com.detection.detectionagent.methods.dataExtractions.forks.DataHandler;
import br.com.detection.detectionagent.domain.methods.weiL.executors.WeiEtAl2014FactoryExecutor;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.patterns.DesignPattern;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class WeiEtAl2014 implements DetectionMethod, AbstractSyntaxTreeDependent {

    private final String title;

    private final int year;

    private final Set<DesignPattern> designPatterns = new HashSet<>();

    private final List<Author> authors = new ArrayList<>();

    public WeiEtAl2014() {
        this.title = "Automated pattern­directed refactoring for complex conditional statements";
        this.year = 2014;

        this.authors.add(new Author("Liu Wei"));
        this.authors.add(new Author("Hu Zhi-gang"));
        this.authors.add(new Author("Liu Hong-tao"));
        this.authors.add(new Author("Yang Liu"));

        this.designPatterns.add(DesignPattern.TEMPLATE_METHOD);
    }

    @Override
    public Collection<RefactoringCandidate> extractCandidates(DataHandler dataHandler) {
        return this.getVerifiers()
                .flatMap(v -> v.retrieveCandidatesFrom(this.toReference(), dataHandler).stream())
                .collect(Collectors.toList());
    }

    private Stream<WeiEtAl2014Verifier> getVerifiers() {
        return Stream.of(new WeiEtAl2014FactoryVerifier(), new WeiEtAl2014StrategyVerifier());
    }

    @Override
    public void refactor(DataHandler dataHandler, RefactoringCandidate candidate) {
        this.getExecutors().filter(e -> e.isApplicable(candidate)).findFirst()
                .orElseThrow(IllegalArgumentException::new).refactor(candidate, dataHandler);
    }

    private Stream<WeiEtAl2014Executor> getExecutors() {
        return Stream.of(new WeiEtAl2014FactoryExecutor(), new WeiEtAl2014StrategyExecutor());
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getYear() {
        return year;
    }

    @Override
    public Set<DesignPattern> getDesignPatterns() {
        return designPatterns;
    }

    @Override
    public List<Author> getAuthors() {
        return authors;
    }

    @Override
    public Reference toReference() {
        return new Reference(title, year, authors.stream().map(Author::getName).collect(Collectors.toList()));
    }

}
