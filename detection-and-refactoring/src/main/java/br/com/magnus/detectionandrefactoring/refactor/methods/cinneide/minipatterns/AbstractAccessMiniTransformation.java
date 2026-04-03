package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns;

import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.Set;

public class AbstractAccessMiniTransformation implements MiniTransformation {

    private final String contextClassName;
    private final String concreteClassName;
    private final String interfaceName;
    private final Set<String> skipMethodNames;

    public AbstractAccessMiniTransformation(String contextClassName, String concreteClassName, String interfaceName, Set<String> skipMethodNames) {
        this.contextClassName = contextClassName;
        this.concreteClassName = concreteClassName;
        this.interfaceName = interfaceName;
        this.skipMethodNames = skipMethodNames;
    }

    @Override
    public void apply(CinneideContext context) {
        var contextClass = context.classDeclaration(contextClassName);
        contextClass.findAll(FieldDeclaration.class).forEach(this::replaceTypeIfConcrete);
        contextClass.findAll(VariableDeclarator.class).forEach(this::replaceTypeIfConcrete);

        contextClass.findAll(MethodDeclaration.class).stream()
                .filter(method -> !skipMethodNames.contains(method.getNameAsString()))
                .forEach(method -> {
                    if (method.getType().isClassOrInterfaceType()
                            && method.getType().asClassOrInterfaceType().getNameAsString().equals(concreteClassName)) {
                        method.setType(new ClassOrInterfaceType(interfaceName));
                    }
                    method.findAll(Parameter.class).forEach(this::replaceTypeIfConcrete);
                });
    }

    private void replaceTypeIfConcrete(FieldDeclaration fieldDeclaration) {
        fieldDeclaration.getVariables().forEach(this::replaceTypeIfConcrete);
    }

    private void replaceTypeIfConcrete(Parameter parameter) {
        if (parameter.getType().isClassOrInterfaceType()
                && parameter.getType().asClassOrInterfaceType().getNameAsString().equals(concreteClassName)) {
            parameter.setType(new ClassOrInterfaceType(interfaceName));
        }
    }

    private void replaceTypeIfConcrete(VariableDeclarator variableDeclarator) {
        if (variableDeclarator.getType().isClassOrInterfaceType()
                && variableDeclarator.getType().asClassOrInterfaceType().getNameAsString().equals(concreteClassName)) {
            variableDeclarator.setType(new ClassOrInterfaceType(interfaceName));
        }
    }
}
