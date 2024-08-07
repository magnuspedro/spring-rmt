package br.com.magnus.detectionandrefactoring.refactor.methods.cinneid.helpers;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.Set;


public class Helpers {
    public static Optional<CompilationUnit> abstractClass(ClassOrInterfaceDeclaration clazz, String newName) {
        if (clazz == null || clazz.isInterface() || clazz.getMethods().isEmpty()) {
            return Optional.empty();
        }

        var publicMethods = clazz.getMethods().stream()
                .filter(MethodDeclaration::isPublic)
                .toList();
        if (publicMethods.isEmpty()) {
            return Optional.empty();
        }

        var cu = new CompilationUnit();
        var newClass = cu.addClass(newName)
                .setInterface(true)
                .setName(newName);
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

        return Optional.of(cu);
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

    public static Optional<MethodCallExpr> replaceObjCreationWithMethInvocation(ObjectCreationExpr e, String newName) {
        if (e == null) {
            return Optional.empty();
        }

        return Optional.of(new MethodCallExpr()
                .setName(newName)
                .setArguments(e.getArguments())
        );
    }

    public static void replaceClassWithInterface(VariableDeclarator variable, ClassOrInterfaceDeclaration inf) {
        Assert.isTrue(inf.isInterface(), "The class must be an interface");

        variable.setType(inf.getNameAsString());
    }

    public static void addClass(ClassOrInterfaceDeclaration clazz, ClassOrInterfaceDeclaration superClazz, Set<ClassOrInterfaceDeclaration> subClasses) {
        Assert.notNull(clazz, "The class must not be null");
        Optional.ofNullable(superClazz).ifPresent(sup -> sup.addExtendedType(clazz.getNameAsString()));
        Optional.ofNullable(subClasses).ifPresent(subs -> subs.forEach(sub -> sub.addExtendedType(clazz.getNameAsString())));
    }

    public static MethodDeclaration abstractMethod(MethodDeclaration method) {
        Assert.notNull(method, "The method must not be null");
        return method.clone().addModifier(Modifier.Keyword.ABSTRACT);
    }

    public static void pullUpMethod(ClassOrInterfaceDeclaration superClazz, MethodDeclaration method) {
        Assert.notNull(superClazz, "The super class must not be null");
        Assert.notNull(method, "The method must not be null");

        method.remove();
        superClazz.getMembers().add(method);
    }
}
