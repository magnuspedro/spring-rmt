package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.transformations;

import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns.AbstractAccessMiniTransformation;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns.AbstractionMiniTransformation;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns.DelegationMiniTransformation;

import java.util.Set;

public class StrategyPatternTransformation implements PatternTransformation {

    private final String contextClass;
    private final Set<String> strategyMethods;
    private final String strategyClass;

    public StrategyPatternTransformation(String contextClass, Set<String> strategyMethods, String strategyClass) {
        this.contextClass = contextClass;
        this.strategyMethods = strategyMethods;
        this.strategyClass = strategyClass;
    }

    @Override
    public void apply(CinneideContext context) {
        var strategyInterface = strategyClass + "Interface";
        new DelegationMiniTransformation(contextClass, strategyMethods, strategyClass).apply(context);
        new AbstractionMiniTransformation(strategyClass, strategyInterface).apply(context);
        new AbstractAccessMiniTransformation(contextClass, strategyClass, strategyInterface, Set.of()).apply(context);
    }
}
