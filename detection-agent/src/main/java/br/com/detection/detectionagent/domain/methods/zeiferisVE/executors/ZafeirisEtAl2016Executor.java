package br.com.detection.detectionagent.domain.methods.zeiferisVE.executors;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.FragmentsSplitter;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.ZafeirisEtAl2016Canditate;
import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.ExtractionMethod;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.expr.AssignExpr.Operator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ZafeirisEtAl2016Executor {

    private final AstHandler astHandler = new AstHandler();

    private Collection<CompilationUnit> getParsedClasses(List<JavaFile> javaFiles, ExtractionMethod extractionMethod) {
        return extractionMethod.parseAll(javaFiles)
                .stream()
                .map(CompilationUnit.class::cast)
                .toList();

    }

    public void refactor(ZafeirisEtAl2016Canditate candidate, List<JavaFile> javaFiles, ExtractionMethod extractMethod) {

        var cus = this.getParsedClasses(javaFiles, extractMethod);
        var childCU = candidate.getCompilationUnit();
        var parentCU = this.updateParent(cus, childCU);

        final MethodDeclaration newOverridenMethod = extractMethodOnOverriddenMethod(javaFiles, candidate, parentCU);

        cus = this.getParsedClasses(javaFiles, extractMethod);
        childCU = this.updateChild(cus, candidate);
        parentCU = this.updateParent(cus, childCU);

        final MethodCallExpr newDoOverridenCall = replaceSuperCallByDoOverriden(javaFiles, childCU, parentCU,
                newOverridenMethod);

        cus = this.getParsedClasses(javaFiles, extractMethod);
        childCU = this.updateChild(cus, candidate);
        parentCU = this.updateParent(cus, childCU);

        extractMethodOnBeforeAndAfterFragments(javaFiles, candidate, childCU, parentCU, newDoOverridenCall);

        cus = this.getParsedClasses(javaFiles, extractMethod);
        childCU = this.updateChild(cus, candidate);
        parentCU = this.updateParent(cus, childCU);

        pullUpOverridenMethod(javaFiles, candidate, parentCU, childCU);

        cus = this.getParsedClasses(javaFiles, extractMethod);
        childCU = this.updateChild(cus, candidate);
        parentCU = this.updateParent(cus, childCU);

        applyFinalAdjustments(javaFiles, candidate, parentCU, childCU);
    }

    private CompilationUnit updateChild(Collection<CompilationUnit> allClasses, ZafeirisEtAl2016Canditate candidate) {
        return allClasses.stream().filter(c ->
                this.astHandler.doesCompilationUnitsMatch(c, Optional.of(candidate.getClassDeclaration()),
                        Optional.of(candidate.getPackageDeclaration()))
        ).findFirst().get();
    }

    private CompilationUnit updateParent(Collection<CompilationUnit> allClasses, CompilationUnit childCU) {
        return this.astHandler.getParent(childCU, allClasses).orElseThrow(IllegalStateException::new);
    }

    private void applyFinalAdjustments(List<JavaFile> javaFiles, ZafeirisEtAl2016Canditate candidate,
                                       CompilationUnit parentCU, CompilationUnit childCU) {

        final MethodDeclaration overridenMethodDclr = this.astHandler.getMethods(parentCU).stream()
                .filter(m -> this.astHandler.methodsParamsMatch(m, candidate.getOverridenMethod())).findFirst()
                .orElseThrow(IllegalArgumentException::new);

        overridenMethodDclr.setFinal(true);

//        writeCanges(parentCU, javaFiles.getFile(parentCU));

    }

    private void pullUpOverridenMethod(List<JavaFile> javaFiles, ZafeirisEtAl2016Canditate candidate,
                                       CompilationUnit parentCU, CompilationUnit childCU) {

        final MethodDeclaration overridenMethodDclr = this.astHandler.getMethods(parentCU).stream()
                .filter(m -> this.astHandler.methodsParamsMatch(m, candidate.getOverridenMethod())).findFirst()
                .orElseThrow(IllegalArgumentException::new);

        final MethodDeclaration overridingMethodDclr = this.astHandler.getMethods(childCU).stream()
                .filter(m -> this.astHandler.methodsParamsMatch(m, candidate.getOverridingMethod())).findFirst()
                .orElseThrow(IllegalArgumentException::new);

        overridenMethodDclr.setBody(new BlockStmt());

        overridingMethodDclr.getBody()
                .ifPresent(b -> b.getStatements().forEach(overridenMethodDclr.getBody().get()::addStatement));

        final ClassOrInterfaceDeclaration childClass = this.astHandler.getClassOrInterfaceDeclaration(childCU).get();

        childClass.remove(overridingMethodDclr);

//        writeCanges(parentCU, javaFiles.getFile(parentCU));
//        writeCanges(childCU, javaFiles.getFile(childCU));
    }

    private void extractMethodOnBeforeAndAfterFragments(List<JavaFile> javaFiles, ZafeirisEtAl2016Canditate candidate,
                                                        CompilationUnit childCU, CompilationUnit parentCU, MethodCallExpr newDoOverridenCall) {

        final MethodDeclaration childMethodDclr = this.astHandler.getMethods(childCU).stream()
                .filter(m -> m.getNameAsString().equals(candidate.getOverridenMethod().getNameAsString()))
                .filter(m -> this.astHandler.methodsParamsMatch(m, candidate.getOverridenMethod())).findFirst()
                .orElseThrow(IllegalArgumentException::new);

        final FragmentsSplitter fragmentsSplitter = new FragmentsSplitter(childMethodDclr, newDoOverridenCall);

        final Node beforeFragmentReturnValue = this.applyExtractMethodOnBeforeFragment(parentCU, childCU,
                childMethodDclr, fragmentsSplitter);
        final MethodDeclaration afterFragmentMethod = this.applyExtractMethodOnAfterFragment(parentCU, childCU,
                childMethodDclr, fragmentsSplitter, Optional.of(beforeFragmentReturnValue)
                        .filter(VariableDeclarationExpr.class::isInstance).map(VariableDeclarationExpr.class::cast));

        applyBeforeAndAfterFragmentsInSourceMethod(fragmentsSplitter, childMethodDclr, beforeFragmentReturnValue,
                newDoOverridenCall, afterFragmentMethod);

//        writeCanges(parentCU, javaFiles.getFile(parentCU));
//        writeCanges(childCU, javaFiles.getFile(childCU));
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

        final ClassOrInterfaceDeclaration childClassDclr = this.astHandler.getClassOrInterfaceDeclaration(childCU)
                .orElseThrow(IllegalArgumentException::new);

        final String beforeMethodName = String.format("before%s%s",
                childMethodDclr.getName().asString().substring(0, 1).toUpperCase(),
                childMethodDclr.getName().asString().substring(1));

        childClassDclr.addMethod(beforeMethodName, Modifier.PROTECTED);

        final Collection<MethodDeclaration> methods = this.astHandler.getMethods(childCU);

        final MethodDeclaration newMethodDclr = methods.stream().filter(
                        m -> this.astHandler.getSimpleName(m).filter(sn -> sn.asString().equals(beforeMethodName)).isPresent())
                .findFirst().orElseThrow(IllegalArgumentException::new);

        final BlockStmt block = new BlockStmt();

        fragmentsSplitter.getBeforeFragment().stream().filter(Statement.class::isInstance).map(Statement.class::cast)
                .forEach(block::addStatement);

        newMethodDclr.setBody(block);
        childMethodDclr.getParameters().forEach(newMethodDclr::addParameter);
        childMethodDclr.getThrownExceptions().forEach(newMethodDclr::addThrownException);

        final MethodCallExpr methodCallExpr = new MethodCallExpr(beforeMethodName);
        newMethodDclr.getParameters().stream().map(Parameter::getName).map(NameExpr::new)
                .forEach(methodCallExpr.getArguments()::add);

        final Collection<VariableDeclarationExpr> variables = fragmentsSplitter
                .getVariablesOnBeforeFragmentsMethodCalss();

        if (variables.size() == 1) {

            final VariableDeclarationExpr varDclrExpr = variables.stream().findFirst().get();

            final VariableDeclarator varDclr = varDclrExpr.getVariables().get(0);

            final ReturnStmt returnStmt = new ReturnStmt(new NameExpr(varDclr.getName().asString()));

            block.addStatement(returnStmt);

            newMethodDclr.setType(varDclr.getType());

            final VariableDeclarationExpr thisMethodCallDclrExpr = new VariableDeclarationExpr(
                    new VariableDeclarator(varDclr.getType(), varDclr.getName(), methodCallExpr));

            createHookMethod(parentCU, newMethodDclr, fragmentsSplitter);

            return thisMethodCallDclrExpr;
        } else if (variables.size() > 1) {
            throw new IllegalStateException();
        }

        createHookMethod(parentCU, newMethodDclr, fragmentsSplitter);

        return methodCallExpr;
    }

    private void createHookMethod(CompilationUnit parentCU, MethodDeclaration newMethodDclr,
                                  FragmentsSplitter fragmentsSplitter) {

        final ClassOrInterfaceDeclaration childClassDclr = this.astHandler.getClassOrInterfaceDeclaration(parentCU)
                .orElseThrow(IllegalArgumentException::new);

        childClassDclr.addMethod(newMethodDclr.getNameAsString(), Modifier.PROTECTED);

        final Collection<MethodDeclaration> methods = this.astHandler.getMethods(parentCU);

        final MethodDeclaration hookMethodDclr = methods.stream()
                .filter(m -> this.astHandler.getSimpleName(m)
                        .filter(sn -> sn.asString().equals(newMethodDclr.getNameAsString())).isPresent())
                .findFirst().orElseThrow(IllegalArgumentException::new);

        newMethodDclr.getParameters()
                .forEach(p -> hookMethodDclr.getParameters().add(new Parameter(p.getType(), p.getName())));
        newMethodDclr.getThrownExceptions().forEach(hookMethodDclr::addThrownException);

        Optional.ofNullable(newMethodDclr.getType()).ifPresent(hookMethodDclr::setType);

        final Optional<ReturnStmt> returnStmt = newMethodDclr.getBody()
                .filter(b -> !b.getStatements().isEmpty())
                .filter(b -> b.getStatement(b.getStatements().size() - 1) != null)
                .map(b -> b.getStatement(b.getStatements().size() - 1)).filter(ReturnStmt.class::isInstance)
                .map(ReturnStmt.class::cast);

        hookMethodDclr.setBody(new BlockStmt());

        final Collection<VariableDeclarationExpr> variableDeclarationExpressions = fragmentsSplitter
                .getVariablesOnBeforeFragmentsMethodCalss();

        variableDeclarationExpressions.stream().findFirst().ifPresent(hookMethodDclr.getBody().get()::addStatement);

        returnStmt.ifPresent(stmt -> hookMethodDclr.getBody().get().addStatement(stmt));
    }

    private MethodDeclaration applyExtractMethodOnAfterFragment(CompilationUnit parentCU, CompilationUnit childCU,
                                                                MethodDeclaration childMethodDclr, FragmentsSplitter fragmentsSplitter,
                                                                Optional<VariableDeclarationExpr> beforeFragmentReturnValue) {

        final ClassOrInterfaceDeclaration childClassDclr = this.astHandler.getClassOrInterfaceDeclaration(childCU)
                .orElseThrow(IllegalArgumentException::new);

        final Optional<FragmentsSplitter.SuperReturnVar> superReturnVar = fragmentsSplitter.getSuperReturnVariable();

        final String afterMethodName = String.format("after%s%s",
                childMethodDclr.getName().asString().substring(0, 1).toUpperCase(),
                childMethodDclr.getName().asString().substring(1));

        childClassDclr.addMethod(afterMethodName, Modifier.PROTECTED);

        final Collection<MethodDeclaration> methods = this.astHandler.getMethods(childCU);

        final MethodDeclaration newMethodDclr = methods.stream().filter(
                        m -> this.astHandler.getSimpleName(m).filter(sn -> sn.asString().equals(afterMethodName)).isPresent())
                .findFirst().orElseThrow(IllegalArgumentException::new);

        final BlockStmt block = new BlockStmt();

        fragmentsSplitter.getAfterFragment().stream().filter(Statement.class::isInstance).map(Statement.class::cast)
                .forEach(block::addStatement);

        newMethodDclr.setBody(block);
        newMethodDclr.setType(childMethodDclr.getType());
        childMethodDclr.getTypeParameters().forEach(tparam -> newMethodDclr.getTypeParameters().add(tparam));
        childMethodDclr.getParameters().forEach(newMethodDclr::addParameter);
        childMethodDclr.getThrownExceptions().forEach(newMethodDclr::addThrownException);

        if (beforeFragmentReturnValue.isPresent()) {
            final VariableDeclarator varDclr = beforeFragmentReturnValue.get().getVariables().get(0);

            newMethodDclr.addParameter(varDclr.getType(), varDclr.getNameAsString());
        }

        superReturnVar.ifPresent(returnVar -> newMethodDclr.addParameter(returnVar.getType(), returnVar.getName().asString()));

        createHookMethod(parentCU, newMethodDclr, fragmentsSplitter);

        return newMethodDclr;
    }

    private MethodCallExpr replaceSuperCallByDoOverriden(List<JavaFile> javaFiles, CompilationUnit childCU,
                                                         CompilationUnit parentCu, MethodDeclaration newOverridenMethod) {

        final SuperExpr superExpr = this.astHandler.getSuperCalls(childCU).stream().findFirst().get();

        final MethodCallExpr superMethodCall = (MethodCallExpr) superExpr.getParentNode().get();

        final ExpressionStmt node = this.astHandler.getExpressionStatement(superExpr).get();

        final MethodCallExpr newMethodCall = new MethodCallExpr(newOverridenMethod.getNameAsString());

        superMethodCall.getArguments().forEach(newMethodCall::addArgument);

        if (node.getChildNodes().get(0) instanceof final VariableDeclarationExpr oldVariableDeclaration) {

            final VariableDeclarator oldVariableDeclarator = (VariableDeclarator) oldVariableDeclaration.getChildNodes()
                    .get(0);

            final VariableDeclarator varibleDeclarator = new VariableDeclarator(oldVariableDeclarator.getType(),
                    oldVariableDeclarator.getName(), newMethodCall);

            final VariableDeclarationExpr newVariableDeclaration = new VariableDeclarationExpr(varibleDeclarator);

            node.setExpression(newVariableDeclaration);

        } else if (node.getChildNodes().get(0) instanceof MethodCallExpr) {
            node.setExpression(newMethodCall);
        } else if (node.getChildNodes().get(0) instanceof final AssignExpr oldAssignment) {
            node.setExpression(new AssignExpr(oldAssignment.getTarget(), newMethodCall, Operator.ASSIGN));
        } else {
            throw new UnsupportedOperationException();
        }

//        writeCanges(childCU, javaFiles.getFile(childCU));

        return newMethodCall;
    }

    private MethodDeclaration extractMethodOnOverriddenMethod(List<JavaFile> javaFiles,
                                                              ZafeirisEtAl2016Canditate candidate, CompilationUnit parentCu) {

        final ClassOrInterfaceDeclaration parentClassDclr = this.astHandler.getClassOrInterfaceDeclaration(parentCu)
                .orElseThrow(IllegalArgumentException::new);

        final MethodDeclaration oldMethodDclr = this.astHandler.getMethods(parentCu).stream()
                .filter(m -> this.astHandler.methodsParamsMatch(m, candidate.getOverridenMethod())).findFirst()
                .orElseThrow(IllegalArgumentException::new);

        final String newMethodName = String.format("do%s%s",
                oldMethodDclr.getName().asString().substring(0, 1).toUpperCase(),
                oldMethodDclr.getName().asString().substring(1));

        parentClassDclr.addMethod(newMethodName, Modifier.PRIVATE);

        final Collection<MethodDeclaration> methods = this.astHandler.getMethods(parentCu);

        final MethodDeclaration newMethodDclr = methods.stream()
                .filter(m -> this.astHandler.getSimpleName(m)
                        .filter(sn -> sn.asString().equals(newMethodName))
                        .isPresent())
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

        newMethodDclr.setBody(oldMethodDclr.getBody().orElseThrow(IllegalArgumentException::new));
        newMethodDclr.setType(oldMethodDclr.getType());
        oldMethodDclr.getTypeParameters().forEach(tparam -> newMethodDclr.getTypeParameters().add(tparam));
        oldMethodDclr.getParameters().forEach(newMethodDclr::addParameter);
        oldMethodDclr.getThrownExceptions().forEach(newMethodDclr::addThrownException);

        final MethodCallExpr methodCallExpr = new MethodCallExpr(newMethodName);
        oldMethodDclr.getParameters().forEach(p -> methodCallExpr.addArgument(p.getName().asString()));
        final ReturnStmt returnStmt = new ReturnStmt(methodCallExpr);

        oldMethodDclr.setBody(new BlockStmt());
        oldMethodDclr.getBody().get().addStatement(returnStmt);

//        writeCanges(parentCu, javaFiles.getFile(parentCu));

        return newMethodDclr;
    }

    private void writeCanges(CompilationUnit cUnit, Path file) {
        try (FileWriter fileWriter = new FileWriter(file.toFile())) {
            fileWriter.write(cUnit.toString());
            fileWriter.flush();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

}
