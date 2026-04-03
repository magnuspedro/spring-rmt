package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.transformations.AbstractFactoryPatternTransformation;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.transformations.BridgePatternTransformation;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.transformations.FactoryMethodPatternTransformation;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.transformations.SingletonPatternTransformation;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.transformations.StrategyPatternTransformation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class CinneideRefactoringTool {

    public List<JavaFile> applyFactoryMethod(List<JavaFile> files, String creatorClass, String productClass, String productInterface, String abstractCreator, String createMethodName) {
        var context = new CinneideContext(files);
        new FactoryMethodPatternTransformation(creatorClass, productClass, productInterface, abstractCreator, createMethodName).apply(context);
        return context.files();
    }

    public List<JavaFile> applySingleton(List<JavaFile> files, String concreteSingletonClass, String newAbstractSingletonClass) {
        var context = new CinneideContext(files);
        new SingletonPatternTransformation(concreteSingletonClass, newAbstractSingletonClass).apply(context);
        return context.files();
    }

    public List<JavaFile> applyAbstractFactory(List<JavaFile> files, Set<String> productClasses, String newFactoryName, String newAbstractFactoryName) {
        var context = new CinneideContext(files);
        new AbstractFactoryPatternTransformation(productClasses, newFactoryName, newAbstractFactoryName).apply(context);
        return context.files();
    }

    public List<JavaFile> applyStrategy(List<JavaFile> files, String contextClass, Set<String> strategyMethods, String strategyClass) {
        var context = new CinneideContext(files);
        new StrategyPatternTransformation(contextClass, strategyMethods, strategyClass).apply(context);
        return context.files();
    }

    public List<JavaFile> applyBridge(List<JavaFile> files, Set<String> clientClasses, String interfaceName, String bridgeClassName) {
        var context = new CinneideContext(files);
        new BridgePatternTransformation(clientClasses, interfaceName, bridgeClassName).apply(context);
        return context.files();
    }
}
