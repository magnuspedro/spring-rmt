package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.transformations;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns.PartialAbstractionMiniTransformation;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.Set;
import java.util.stream.Collectors;

public class SingletonPatternTransformation implements PatternTransformation {

    private final String concreteSingletonClass;
    private final String newAbstractSingletonClass;

    public SingletonPatternTransformation(String concreteSingletonClass, String newAbstractSingletonClass) {
        this.concreteSingletonClass = concreteSingletonClass;
        this.newAbstractSingletonClass = newAbstractSingletonClass;
    }

    @Override
    public void apply(CinneideContext context) {
        var concrete = context.classDeclaration(concreteSingletonClass);
        var allMethodNames = AstHandler.getMethods(concrete).stream()
                .map(MethodDeclaration::getNameAsString)
                .collect(Collectors.toSet());
        new PartialAbstractionMiniTransformation(concreteSingletonClass, newAbstractSingletonClass, allMethodNames).apply(context);

        var abstractSingleton = context.classDeclaration(newAbstractSingletonClass);
        if (abstractSingleton.getFieldByName("instance").isEmpty()) {
            abstractSingleton.addFieldWithInitializer(new ClassOrInterfaceType(newAbstractSingletonClass), "instance",
                    new ObjectCreationExpr().setType(concreteSingletonClass), Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
        }

        var singletonMethod = abstractSingleton.getMethodsByName("getInstance").stream().findFirst().orElseGet(() -> {
            var method = abstractSingleton.addMethod("getInstance", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
            method.setType(newAbstractSingletonClass);
            return method;
        });
        singletonMethod.setBody(new BlockStmt().addStatement(new ReturnStmt(new NameExpr("instance"))));

        context.files().forEach(file -> {
            if (file.getFileNameWithoutExtension().equals(newAbstractSingletonClass)) {
                return;
            }
            file.getCompilationUnit().findAll(ObjectCreationExpr.class).stream()
                    .filter(expr -> expr.getType().getNameAsString().equals(concreteSingletonClass))
                    .forEach(expr -> expr.replace(new MethodCallExpr(new NameExpr(newAbstractSingletonClass), "getInstance")));
        });

        abstractSingleton.getConstructors().forEach(constructor -> {
            constructor.setModifiers(Modifier.Keyword.PROTECTED);
        });
    }
}
