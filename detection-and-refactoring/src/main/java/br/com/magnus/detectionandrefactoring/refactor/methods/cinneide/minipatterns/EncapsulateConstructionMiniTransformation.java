package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.UnknownType;

import java.util.ArrayList;
import java.util.List;

/**
 * Mini transformation for encapsulating object creation.
 * <p>
 * Replaces direct object creation expressions with factory method calls.
 * Creates factory methods if they don't already exist.
 * This is the core of the Factory Method refactoring pattern.
 */
public class EncapsulateConstructionMiniTransformation implements MiniTransformation {

    /**
     * The name of the creator class.
     */
    private final String creatorClassName;

    /**
     * The name of the product class being created.
     */
    private final String productClassName;

    /**
     * The name of the factory method to create/use.
     */
    private final String createMethodName;

    /**
     * Creates a new encapsulate construction mini transformation.
     *
     * @param creatorClassName  the creator class name
     * @param productClassName   the product class name
     * @param createMethodName  the factory method name
     */
    public EncapsulateConstructionMiniTransformation(String creatorClassName, String productClassName, String createMethodName) {
        this.creatorClassName = creatorClassName;
        this.productClassName = productClassName;
        this.createMethodName = createMethodName;
    }

    /**
     * Applies the encapsulate construction transformation.
     * <p>
     * Replaces direct object creation with factory method calls.
     *
     * @param context the refactoring context
     */
    @Override
    public void apply(CinneideContext context) {
        var creatorClass = context.classDeclaration(creatorClassName);
        var creations = new ArrayList<ObjectCreationExpr>();
        AstHandler.getMethods(creatorClass).forEach(method -> method.findAll(ObjectCreationExpr.class).stream()
                .filter(o -> o.getType().getNameAsString().equals(productClassName))
                .forEach(creations::add));

        for (var creation : creations) {
            ensureCreatorMethod(creatorClass, creation);
            var replacement = new MethodCallExpr(createMethodName);
            creation.getArguments().forEach(argument -> replacement.addArgument(argument.clone()));
            creation.replace(replacement);
        }
    }

    /**
     * Ensures a factory method exists for the given object creation.
     * <p>
     * Creates the method if it doesn't already exist.
     *
     * @param creatorClass the creator class
     * @param creation     the object creation expression
     */
    private void ensureCreatorMethod(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration creatorClass, ObjectCreationExpr creation) {
        var argTypes = creation.getArguments().stream().map(this::inferType).toList();
        var exists = AstHandler.getMethods(creatorClass).stream()
                .filter(method -> method.getNameAsString().equals(createMethodName))
                .anyMatch(method -> sameParamTypes(method.getParameters(), argTypes));

        if (exists) {
            return;
        }

        var method = creatorClass.addMethod(createMethodName, Modifier.Keyword.PUBLIC);
        method.setType(new ClassOrInterfaceType(productClassName));
        for (var i = 0; i < argTypes.size(); i++) {
            method.addParameter(new Parameter(argTypes.get(i), "arg" + i));
        }
        var objectCreation = new ObjectCreationExpr();
        objectCreation.setType(productClassName);
        method.getParameters().forEach(parameter -> objectCreation.addArgument(new NameExpr(parameter.getNameAsString())));
        method.setBody(new BlockStmt().addStatement(new ReturnStmt(objectCreation)));
    }

    /**
     * Checks if two parameter type lists are equivalent.
     *
     * @param parameters the method parameters
     * @param argTypes   the argument types
     * @return true if types match
     */
    private boolean sameParamTypes(List<Parameter> parameters, List<Type> argTypes) {
        if (parameters.size() != argTypes.size()) {
            return false;
        }
        for (var i = 0; i < parameters.size(); i++) {
            if (!parameters.get(i).getType().equals(argTypes.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Infers the type of an expression.
     * <p>
     * Handles literals, names, and method calls.
     *
     * @param expression the expression to infer type from
     * @return the inferred type
     */
    private Type inferType(Expression expression) {
        if (expression.isIntegerLiteralExpr()) return PrimitiveType.intType();
        if (expression.isLongLiteralExpr()) return PrimitiveType.longType();
        if (expression.isBooleanLiteralExpr()) return PrimitiveType.booleanType();
        if (expression.isDoubleLiteralExpr()) return PrimitiveType.doubleType();
        if (expression.isCharLiteralExpr()) return PrimitiveType.charType();
        if (expression.isStringLiteralExpr()) return new ClassOrInterfaceType("String");
        if (expression.isNameExpr()) return new ClassOrInterfaceType("Object");
        if (expression.isMethodCallExpr()) return new ClassOrInterfaceType("Object");
        return new UnknownType();
    }
}
