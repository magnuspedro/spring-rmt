package br.com.detection.detectionagent.refactor.methods.zeiferisVE.executors;

import br.com.detection.detectionagent.refactor.dataExtractions.ast.AstHandler;
import br.com.detection.detectionagent.refactor.methods.zeiferisVE.FragmentsSplitter;
import br.com.detection.detectionagent.refactor.methods.zeiferisVE.ZafeirisEtAl2016Candidate;
import br.com.magnus.config.starter.file.JavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.expr.AssignExpr.Operator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ZafeirisEtAl2016Executor {

    public void refactor(ZafeirisEtAl2016Candidate candidate, List<JavaFile> javaFiles) {
        Assert.notNull(candidate, "Candidate cannot be null");
        Assert.notNull(javaFiles, "JavaFiles cannot be null");

        var parent = this.getParent(javaFiles, candidate);

        final var newOverriddenMethod = extractMethodOnOverriddenMethod(candidate, parent);
        final var newDoOverriddenCall = replaceSuperCallByDoOverridden(candidate, newOverriddenMethod);
        extractMethodOnBeforeAndAfterFragments(candidate, parent, newDoOverriddenCall);
        pullUpOverridenMethod(candidate, parent);
        applyFinalAdjustments(candidate, parent);
    }

    private CompilationUnit getParent(List<JavaFile> javaFile, ZafeirisEtAl2016Candidate candidate) {
        var cus = javaFile.stream()
                .map(JavaFile::getCompilationUnit)
                .toList();

        return AstHandler.getParent(candidate.getCompilationUnit(), cus).orElseThrow(IllegalStateException::new);
    }

    private void applyFinalAdjustments(ZafeirisEtAl2016Candidate candidate,
                                       CompilationUnit parentCU) {

        final MethodDeclaration overriddenMethodDclr = AstHandler.getMethods(parentCU)
                .stream()
                .filter(m -> AstHandler.methodsParamsMatch(m, candidate.getOverriddenMethod()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

        overriddenMethodDclr.setFinal(true);
    }

    private void pullUpOverridenMethod(ZafeirisEtAl2016Candidate candidate,
                                       CompilationUnit parentCU) {

        final var overriddenMethodDclr = AstHandler.getMethods(parentCU)
                .stream()
                .filter(m -> AstHandler.methodsParamsMatch(m, candidate.getOverriddenMethod()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

        final var overridingMethodDclr = AstHandler.getMethods(candidate.getCompilationUnit())
                .stream()
                .filter(m -> AstHandler.methodsParamsMatch(m, candidate.getOverridingMethod()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

        overriddenMethodDclr.setBody(new BlockStmt());

        overridingMethodDclr.getBody()
                .ifPresent(b -> b.getStatements()
                        .forEach(overriddenMethodDclr.getBody()
                                .orElseThrow(IllegalArgumentException::new)::addStatement));

        final var childClass = AstHandler.getClassOrInterfaceDeclaration(candidate.getCompilationUnit())
                .orElseThrow(IllegalArgumentException::new);

        childClass.remove(overridingMethodDclr);
    }

    private void extractMethodOnBeforeAndAfterFragments(ZafeirisEtAl2016Candidate candidate, CompilationUnit parentCU, MethodCallExpr newDoOverriddenCall) {

        final var childMethodDclr = AstHandler.getMethods(candidate.getCompilationUnit())
                .stream()
                .filter(m -> m.getNameAsString().equals(candidate.getOverriddenMethod().getNameAsString()))
                .filter(m -> AstHandler.methodsParamsMatch(m, candidate.getOverriddenMethod())).findFirst()
                .orElseThrow(IllegalArgumentException::new);

        final var fragmentsSplitter = FragmentsSplitter.splitByMethodAndMethodCall(childMethodDclr, newDoOverriddenCall);

        final var beforeFragmentReturnValue = this.applyExtractMethodOnBeforeFragment(parentCU, candidate.getCompilationUnit(),
                childMethodDclr, fragmentsSplitter);
        final var afterFragmentMethod = this.applyExtractMethodOnAfterFragment(parentCU, candidate.getCompilationUnit(),
                childMethodDclr, fragmentsSplitter, Optional.of(beforeFragmentReturnValue)
                        .filter(VariableDeclarationExpr.class::isInstance)
                        .map(VariableDeclarationExpr.class::cast));

        applyBeforeAndAfterFragmentsInSourceMethod(fragmentsSplitter, childMethodDclr, beforeFragmentReturnValue,
                newDoOverriddenCall, afterFragmentMethod);
    }

    private void applyBeforeAndAfterFragmentsInSourceMethod(FragmentsSplitter fragmentsSplitter,
                                                            MethodDeclaration childMethodDclr, Node beforeFragmentReturnValue, MethodCallExpr newDoOverridenCall,
                                                            MethodDeclaration afterFragmentMethod) {

        VariableDeclarationExpr superCallThroughAssignment = null;
        final Optional<FragmentsSplitter.SuperReturnVar> superReturnVar = fragmentsSplitter.getSuperReturnVariable();
        if (superReturnVar.isPresent()) {
            superCallThroughAssignment = new VariableDeclarationExpr(
                    new VariableDeclarator(superReturnVar.get().getType(), "superReturnVar", newDoOverridenCall));
        }

        final MethodCallExpr afterFragmentMethodCallExpr = new MethodCallExpr(afterFragmentMethod.getNameAsString());
        childMethodDclr.getParameters().forEach(p -> afterFragmentMethodCallExpr.addArgument(p.getName().asString()));
        if (beforeFragmentReturnValue instanceof VariableDeclarationExpr) {
            afterFragmentMethodCallExpr.addArgument(
                    ((VariableDeclarationExpr) beforeFragmentReturnValue).getVariable(0).getNameAsString());
        }
        if (superReturnVar.isPresent()) {
            afterFragmentMethodCallExpr.addArgument(superCallThroughAssignment.getVariable(0).getNameAsString());
        }
        final ReturnStmt returnStmt = new ReturnStmt(afterFragmentMethodCallExpr);

        final BlockStmt block = new BlockStmt();
        block.addStatement(Optional.of(beforeFragmentReturnValue).filter(Expression.class::isInstance)
                .map(Expression.class::cast).orElseThrow(IllegalStateException::new));
        block.addStatement(Optional.ofNullable((Expression) superCallThroughAssignment).orElse(newDoOverridenCall));
        block.addStatement(returnStmt);

        childMethodDclr.setBody(block);
    }

    private Node applyExtractMethodOnBeforeFragment(CompilationUnit parentCU, CompilationUnit childCU,
                                                    MethodDeclaration childMethodDclr, FragmentsSplitter fragmentsSplitter) {

        final var variables = fragmentsSplitter.getVariablesOnBeforeFragmentsMethodClass();
        if (variables.size() > 1) {
            throw new IllegalStateException();
        }

        final var childClassDclr = AstHandler.getClassOrInterfaceDeclaration(childCU)
                .orElseThrow(IllegalArgumentException::new);
        final var block = new BlockStmt();
        final var beforeMethodName = String.format("before%s%s",
                childMethodDclr.getName().asString().substring(0, 1).toUpperCase(),
                childMethodDclr.getName().asString().substring(1));
        final var newMethod = childClassDclr.addMethod(beforeMethodName, Modifier.PROTECTED);

        fragmentsSplitter.getBeforeStatements().forEach(block::addStatement);
        newMethod.setBody(block);
        childMethodDclr.getParameters().forEach(newMethod::addParameter);
        childMethodDclr.getThrownExceptions().forEach(newMethod::addThrownException);

        final var methodCallExpr = new MethodCallExpr(beforeMethodName);
        newMethod.getParameters().stream()
                .map(Parameter::getName)
                .map(NameExpr::new)
                .forEach(methodCallExpr.getArguments()::add);

        if (variables.size() == 1) {
            final var varDclrExpr = variables.stream().findFirst().get();
            final var varDclr = varDclrExpr.getVariables().getFirst();
            final var returnStmt = new ReturnStmt(new NameExpr(varDclr.getName().asString()));
            final var thisMethodCallDclrExpr = new VariableDeclarationExpr(new VariableDeclarator(varDclr.getType(), varDclr.getName(), methodCallExpr));

            block.addStatement(returnStmt);
            newMethod.setType(varDclr.getType());
            createHookMethod(parentCU, newMethod, fragmentsSplitter);

            return thisMethodCallDclrExpr;
        }

        createHookMethod(parentCU, newMethod, fragmentsSplitter);

        return methodCallExpr;
    }

    private void createHookMethod(CompilationUnit parentCU, MethodDeclaration newMethodDclr,
                                  FragmentsSplitter fragmentsSplitter) {

        if (AstHandler.getMethodByName(parentCU, newMethodDclr.getNameAsString()) != null) {
            return;
        }

        final var childClassDclr = AstHandler.getClassOrInterfaceDeclaration(parentCU).orElseThrow(IllegalArgumentException::new);
        final var hookMethod = childClassDclr.addMethod(newMethodDclr.getNameAsString(), Modifier.PROTECTED);

        newMethodDclr.getParameters().forEach(p -> hookMethod.getParameters().add(new Parameter(p.getType(), p.getName())));
        newMethodDclr.getThrownExceptions().forEach(hookMethod::addThrownException);
        Optional.ofNullable(newMethodDclr.getType()).ifPresent(hookMethod::setType);

        hookMethod.setBody(new BlockStmt());
        final var body = hookMethod.getBody().orElseThrow(() -> new IllegalArgumentException("Body is null"));

        fragmentsSplitter.getVariablesOnBeforeFragmentsMethodClass()
                .stream()
                .findFirst()
                .ifPresent(body::addStatement);

        newMethodDclr.getBody()
                .filter(b -> !b.getStatements().isEmpty())
                .filter(b -> b.getStatement(b.getStatements().size() - 1) != null)
                .map(b -> b.getStatement(b.getStatements().size() - 1))
                .filter(ReturnStmt.class::isInstance)
                .map(ReturnStmt.class::cast)
                .ifPresent(stmt -> hookMethod.getBody().get().addStatement(stmt));
    }

    private MethodDeclaration applyExtractMethodOnAfterFragment(CompilationUnit parentCU, CompilationUnit childCU,
                                                                MethodDeclaration childMethodDclr, FragmentsSplitter fragmentsSplitter,
                                                                Optional<VariableDeclarationExpr> beforeFragmentReturnValue) {

        final var childClassDclr = AstHandler.getClassOrInterfaceDeclaration(childCU).orElseThrow(IllegalArgumentException::new);
        final var afterMethodName = String.format("after%s%s",
                childMethodDclr.getName().asString().substring(0, 1).toUpperCase(),
                childMethodDclr.getName().asString().substring(1));
        final var newMethod = childClassDclr.addMethod(afterMethodName, Modifier.PROTECTED);
        final var block = new BlockStmt();

        fragmentsSplitter.getAfterStatements().forEach(block::addStatement);
        newMethod.setBody(block);
        newMethod.setType(childMethodDclr.getType());
        childMethodDclr.getTypeParameters().forEach(typeParam -> newMethod.getTypeParameters().add(typeParam));
        childMethodDclr.getParameters().forEach(newMethod::addParameter);
        childMethodDclr.getThrownExceptions().forEach(newMethod::addThrownException);
        beforeFragmentReturnValue.ifPresent((f -> {
            var variable = f.getVariables().getFirst();
            newMethod.addParameter(variable.getType(), variable.getNameAsString());
        }));
        fragmentsSplitter.getSuperReturnVariable()
                .ifPresent(returnVar -> newMethod.addParameter(returnVar.getType(), returnVar.getName().asString()));

        createHookMethod(parentCU, newMethod, fragmentsSplitter);

        return newMethod;
    }

    private MethodCallExpr replaceSuperCallByDoOverridden(ZafeirisEtAl2016Candidate candidate, MethodDeclaration newOverriddenMethod) {

        final var superExpr = AstHandler.getSuperCalls(candidate.getCompilationUnit()).getFirst();
        final var superMethodCall = (MethodCallExpr) superExpr.getParentNode().orElseThrow(IllegalArgumentException::new);
        final var node = AstHandler.getExpressionStatement(superExpr).orElseThrow(IllegalArgumentException::new);
        final var newMethodCall = new MethodCallExpr(newOverriddenMethod.getNameAsString());

        superMethodCall.getArguments().forEach(newMethodCall::addArgument);

        if (node.getChildNodes().getFirst() instanceof final VariableDeclarationExpr oldVariableDeclaration) {

            final var oldVariableDeclarator = (VariableDeclarator) oldVariableDeclaration.getChildNodes().getFirst();
            final var variableDeclarator = new VariableDeclarator(oldVariableDeclarator.getType(), oldVariableDeclarator.getName(), newMethodCall);
            final var newVariableDeclaration = new VariableDeclarationExpr(variableDeclarator);

            node.setExpression(newVariableDeclaration);

        } else if (node.getChildNodes().getFirst() instanceof MethodCallExpr) {
            node.setExpression(newMethodCall);
        } else if (node.getChildNodes().getFirst() instanceof final AssignExpr oldAssignment) {
            node.setExpression(new AssignExpr(oldAssignment.getTarget(), newMethodCall, Operator.ASSIGN));
        } else {
            throw new UnsupportedOperationException();
        }

        return newMethodCall;
    }

    private MethodDeclaration extractMethodOnOverriddenMethod(ZafeirisEtAl2016Candidate candidate, CompilationUnit parentCu) {

        final var parentClass = AstHandler.getClassOrInterfaceDeclaration(parentCu)
                .orElseThrow(IllegalArgumentException::new);
        final var parentMethod = AstHandler.getMethods(parentClass).stream()
                .filter(m -> AstHandler.methodsParamsMatch(m, candidate.getOverriddenMethod()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        final var newMethodName = String.format("do%s%s",
                parentMethod.getName().asString().substring(0, 1).toUpperCase(),
                parentMethod.getName().asString().substring(1));

        final var doMethod = AstHandler.getMethodByName(parentCu, newMethodName);
        if (doMethod != null) {
            return doMethod;
        }

        final var newMethod = parentClass.addMethod(newMethodName, Modifier.PRIVATE);
        newMethod.setBody(parentMethod.getBody().orElseThrow(IllegalArgumentException::new));
        newMethod.setType(parentMethod.getType());
        parentMethod.getTypeParameters().forEach(typeParam -> newMethod.getTypeParameters().add(typeParam));
        parentMethod.getParameters().forEach(newMethod::addParameter);
        parentMethod.getThrownExceptions().forEach(newMethod::addThrownException);

        final var methodCallExpr = new MethodCallExpr(newMethodName);
        parentMethod.getParameters().forEach(p -> methodCallExpr.addArgument(p.getName().asString()));
        final var returnStmt = new ReturnStmt(methodCallExpr);
        parentMethod.setBody(new BlockStmt(NodeList.nodeList(returnStmt)));

        return newMethod;
    }
}
