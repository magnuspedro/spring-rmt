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

public class EncapsulateConstructionMiniTransformation implements MiniTransformation {

    private final String creatorClassName;
    private final String productClassName;
    private final String createMethodName;

    public EncapsulateConstructionMiniTransformation(String creatorClassName, String productClassName, String createMethodName) {
        this.creatorClassName = creatorClassName;
        this.productClassName = productClassName;
        this.createMethodName = createMethodName;
    }

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

    private boolean sameParamTypes(List<Parameter> parameters, List<Type> argTypes) {
        if (parameters.size() != argTypes.size()) {
            return false;
        }
        for (int i = 0; i < parameters.size(); i++) {
            if (!parameters.get(i).getType().equals(argTypes.get(i))) {
                return false;
            }
        }
        return true;
    }

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
