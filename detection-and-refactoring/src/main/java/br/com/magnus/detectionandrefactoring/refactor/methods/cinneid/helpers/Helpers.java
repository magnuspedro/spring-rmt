package br.com.magnus.detectionandrefactoring.refactor.methods.cinneid.helpers;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

import java.util.Optional;


public class Helpers {
    public static Optional<ClassOrInterfaceDeclaration> abstractClass(ClassOrInterfaceDeclaration clazz, String newName) {
        if (clazz == null || clazz.isInterface() || clazz.getMethods().isEmpty()) {
            return Optional.empty();
        }

        var publicMethods = clazz.getMethods().stream()
                .filter(MethodDeclaration::isPublic)
                .toList();
        if (publicMethods.isEmpty()) {
            return Optional.empty();
        }

        var newClass = new ClassOrInterfaceDeclaration();
        newClass.setInterface(true);
        newClass.setName(newName);
        for (MethodDeclaration m : publicMethods) {
            var modifiers = m.getModifiers().stream()
                    .filter(modifier -> !modifier.equals(Modifier.publicModifier()))
                    .map(Modifier::getKeyword)
                    .toArray(Modifier.Keyword[]::new);

            var newMethod = newClass.addMethod(m.getNameAsString(), modifiers)
                    .setType(m.getType());
            if (!newMethod.isStatic()) {
                newMethod.setBody(null);
            }
        }

        return Optional.of(newClass);
    }

    public static Optional<MethodDeclaration> makeAbstract(ConstructorDeclaration constructor, String newName) {
        if (constructor == null) {
            return Optional.empty();
        }
        var obj = new ObjectCreationExpr().setType(constructor.getNameAsString());
        constructor.getParameters().forEach(p -> obj.addArgument(p.getNameAsString()));
        var body = new BlockStmt().setStatements(NodeList.nodeList(new ReturnStmt().setExpression(obj)));

        return Optional.of(new MethodDeclaration()
                .setName(newName)
                .setBody(body)
                .setParameters(constructor.getParameters())
                .setModifiers(constructor.getModifiers())
                .setType(constructor.getNameAsString()));
    }

    public static Optional<MethodDeclaration> replaceObjCreationWithMethInvocation(ObjectCreationExpr e, String newName){

        return Optional.empty();
    }
}
