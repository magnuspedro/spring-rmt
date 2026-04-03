package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.List;
import java.util.Set;

public class DelegationMiniTransformation implements MiniTransformation {

    private final String contextClassName;
    private final Set<String> moveMethodNames;
    private final String delegationClassName;

    public DelegationMiniTransformation(String contextClassName, Set<String> moveMethodNames, String delegationClassName) {
        this.contextClassName = contextClassName;
        this.moveMethodNames = moveMethodNames;
        this.delegationClassName = delegationClassName;
    }

    @Override
    public void apply(CinneideContext context) {
        var contextClass = context.classDeclaration(contextClassName);
        var contextCu = context.compilationUnit(contextClassName);
        var delegationCu = new CompilationUnit();
        contextCu.getPackageDeclaration().ifPresent(pkg -> delegationCu.setPackageDeclaration(pkg.getNameAsString()));
        var delegationClass = delegationCu.addClass(delegationClassName);

        if (contextClass.getFieldByName("delegation").isEmpty()) {
            var field = new VariableDeclarator(new ClassOrInterfaceType(delegationClassName), "delegation", new ObjectCreationExpr().setType(delegationClassName));
            contextClass.addMember(new com.github.javaparser.ast.body.FieldDeclaration().addVariable(field).setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL));
        }

        var contextMethods = List.copyOf(AstHandler.getMethods(contextClass));
        for (var method : contextMethods) {
            if (!moveMethodNames.contains(method.getNameAsString())) {
                continue;
            }
            var moved = method.clone();
            moved.setModifiers(Modifier.Keyword.PUBLIC);
            delegationClass.addMember(moved);
            method.setBody(delegateBody(method));
        }

        var path = context.classFile(contextClassName).getPath();
        context.addClass(delegationClassName, path, delegationCu);
    }

    private BlockStmt delegateBody(MethodDeclaration method) {
        var call = new MethodCallExpr(new NameExpr("delegation"), method.getNameAsString());
        method.getParameters().stream()
                .map(Parameter::getNameAsString)
                .map(NameExpr::new)
                .forEach(call::addArgument);
        if (method.getType().isVoidType()) {
            return new BlockStmt().addStatement(new ExpressionStmt(call));
        }
        return new BlockStmt().addStatement(new ReturnStmt(call));
    }
}
