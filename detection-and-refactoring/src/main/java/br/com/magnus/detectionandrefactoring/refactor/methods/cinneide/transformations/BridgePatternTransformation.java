package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.transformations;

import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns.WrapperMiniTransformation;

import java.util.Set;

public class BridgePatternTransformation implements PatternTransformation {

    private final Set<String> clientClasses;
    private final String interfaceName;
    private final String bridgeClassName;

    public BridgePatternTransformation(Set<String> clientClasses, String interfaceName, String bridgeClassName) {
        this.clientClasses = clientClasses;
        this.interfaceName = interfaceName;
        this.bridgeClassName = bridgeClassName;
    }

    @Override
    public void apply(CinneideContext context) {
        new WrapperMiniTransformation(clientClasses, interfaceName, bridgeClassName).apply(context);
    }
}
