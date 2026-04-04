package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns;

import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.Set;

/**
 * Abstract base for access modifier transformations.
 * <p>
 * Handles common logic for changing visibility of members and replacing
 * concrete class references with interface references.
 */
public class AbstractAccessMiniTransformation implements MiniTransformation {

    /**
     * The context class name.
     */
    private final String contextClassName;

    /**
     * The concrete class name to replace.
     */
    private final String concreteClassName;

    /**
     * The interface name to use as replacement.
     */
    private final String interfaceName;

    /**
     * Method names to skip during transformation.
     */
    private final Set<String> skipMethodNames;

    /**
     * Creates a new access mini transformation.
     *
     * @param contextClassName  the context class name
     * @param concreteClassName  the concrete class name
     * @param interfaceName      the interface name
     * @param skipMethodNames    method names to skip
     */
    public AbstractAccessMiniTransformation(String contextClassName, String concreteClassName,
                                           String interfaceName, Set<String> skipMethodNames) {
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
                .forEach(this::replaceTypeIfConcrete);
    }

    /**
     * Replaces type if it matches the concrete class.
     *
     * @param fieldDeclaration the field declaration
     */
    private void replaceTypeIfConcrete(FieldDeclaration fieldDeclaration) {
        fieldDeclaration.getVariables().forEach(this::replaceTypeIfConcrete);
    }

    /**
     * Replaces type if it matches the concrete class.
     *
     * @param parameter the parameter
     */
    private void replaceTypeIfConcrete(Parameter parameter) {
        if (parameter.getType().isClassOrInterfaceType()
                && parameter.getType().asClassOrInterfaceType().getNameAsString().equals(concreteClassName)) {
            parameter.setType(new ClassOrInterfaceType(interfaceName));
        }
    }

    /**
     * Replaces type if it matches the concrete class.
     *
     * @param methodDeclaration the method declaration
     */
    private void replaceTypeIfConcrete(MethodDeclaration methodDeclaration) {
        if (methodDeclaration.getType().isClassOrInterfaceType()
                && methodDeclaration.getType().asClassOrInterfaceType().getNameAsString().equals(concreteClassName)) {
            methodDeclaration.setType(new ClassOrInterfaceType(interfaceName));
        }
        methodDeclaration.findAll(Parameter.class).forEach(this::replaceTypeIfConcrete);
    }

    /**
     * Replaces type if it matches the concrete class.
     *
     * @param variableDeclarator the variable declarator
     */
    private void replaceTypeIfConcrete(VariableDeclarator variableDeclarator) {
        if (variableDeclarator.getType().isClassOrInterfaceType()
                && variableDeclarator.getType().asClassOrInterfaceType().getNameAsString().equals(concreteClassName)) {
            variableDeclarator.setType(new ClassOrInterfaceType(interfaceName));
        }
    }
}
