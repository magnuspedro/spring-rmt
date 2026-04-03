package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide;

import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.patterns.DesignPattern;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class CinneideCandidate implements RefactoringCandidate {

    @Builder.Default
    private final String id = UUID.randomUUID().toString();

    @Builder.Default
    private final Reference reference = Reference.builder()
            .title("Automated Application of Design Patterns: A Refactoring Approach")
            .year(2000)
            .authors(List.of("Mel Ó Cinnéide"))
            .build();

    private final String pkg;
    private final String className;
    private final DesignPattern eligiblePattern;

    private final String creatorClass;
    private final String productClass;
    private final String productInterface;
    private final String abstractCreator;
    private final String createMethodName;

    private final String concreteSingletonClass;
    private final String newAbstractSingletonClass;

    private final Set<String> productClasses;
    private final String newFactoryName;
    private final String newAbstractFactoryName;

    private final String contextClass;
    private final Set<String> strategyMethods;
    private final String strategyClass;

    private final Set<String> clientClasses;
    private final String interfaceName;
    private final String bridgeClassName;
}
