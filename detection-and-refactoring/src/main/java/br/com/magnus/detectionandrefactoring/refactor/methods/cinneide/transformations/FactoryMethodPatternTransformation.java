package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.transformations;

import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns.*;

import java.util.Set;

public class FactoryMethodPatternTransformation implements PatternTransformation {

    private final String creatorClass;
    private final String productClass;
    private final String productInterface;
    private final String abstractCreator;
    private final String createProductMethod;

    public FactoryMethodPatternTransformation(String creatorClass, String productClass, String productInterface, String abstractCreator, String createProductMethod) {
        this.creatorClass = creatorClass;
        this.productClass = productClass;
        this.productInterface = productInterface;
        this.abstractCreator = abstractCreator;
        this.createProductMethod = createProductMethod;
    }

    @Override
    public void apply(CinneideContext context) {
        new AbstractionMiniTransformation(productClass, productInterface).apply(context);
        new EncapsulateConstructionMiniTransformation(creatorClass, productClass, createProductMethod).apply(context);
        new AbstractAccessMiniTransformation(creatorClass, productClass, productInterface, Set.of(createProductMethod)).apply(context);
        new PartialAbstractionMiniTransformation(creatorClass, abstractCreator, Set.of(createProductMethod)).apply(context);
    }
}
