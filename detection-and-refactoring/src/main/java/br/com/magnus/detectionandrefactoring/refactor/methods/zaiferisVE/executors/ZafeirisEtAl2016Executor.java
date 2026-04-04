package br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.executors;

import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.exceptions.VariableNotFoundException;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.FragmentsSplitter;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.ZafeirisEtAl2016Candidate;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.expr.AssignExpr.Operator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * Executor for Zafeiris et al. 2016 extract method refactoring.
 * <p>
 * Performs extract method refactoring by moving method bodies to super classes
 * and extracting before/after fragments into separate hook methods.
 */
@Component
@RequiredArgsConstructor
public class ZafeirisEtAl2016Executor {

    /**
     * Refactors code by extracting method to super class.
     *
     * @param refactorFiles the files to refactor
     * @throws IllegalArgumentException if refactorFiles, candidates, or files are null/empty
     */
    public void refactor(RefactorFiles refactorFiles) {
        Assert.notNull(refactorFiles, "RefactorFiles cannot be null");
        Assert.notEmpty(refactorFiles.candidates(), "Candidate cannot be null");
        Assert.notEmpty(refactorFiles.files(), "JavaFiles cannot be null");

        refactorFiles.candidates().forEach(refactoringCandidate -> {
            var candidate = (ZafeirisEtAl2016Candidate) refactoringCandidate;
            var parent = findParentCompilationUnit(refactorFiles, candidate)
                    .orElseThrow(() -> new IllegalStateException("Parent not found"));

            var newOverriddenMethod = extractMethodOnOverriddenMethod(candidate, parent);
            var newDoOverriddenCall = replaceSuperCallWithDoOverridden(candidate, newOverriddenMethod);
            extractBeforeAndAfterFragments(candidate, parent, newDoOverriddenCall);
            pullUpMethodBody(candidate, parent);
            applyFinalAdjustments(candidate, parent);
            updateRefactorFiles(refactorFiles, candidate);
        });
    }

    /**
     * Finds the parent class compilation unit.
     *
     * @param refactorFiles the files containing parent class
     * @param candidate     the candidate with child class
     * @return Optional containing parent compilation unit
     */
    private Optional<CompilationUnit> findParentCompilationUnit(RefactorFiles refactorFiles, ZafeirisEtAl2016Candidate candidate) {
        for (var file : refactorFiles.files()) {
            var parent = AstHandler.getParent(candidate.getCompilationUnit(), file.getCompilationUnit());
            if (parent.isPresent()) {
                refactorFiles.addFileChanged(file.getFullName());
                return Optional.of(file.getCompilationUnit());
            }
        }
        return Optional.empty();
    }

    /**
     * Updates refactor files with changed compilation unit.
     *
     * @param refactorFiles the refactor files container
     * @param candidate     the candidate with updated compilation unit
     */
    private void updateRefactorFiles(RefactorFiles refactorFiles, ZafeirisEtAl2016Candidate candidate) {
        refactorFiles.addFileChanged(candidate.getFile().getFullName());
        refactorFiles.files().stream()
                .filter(f -> f.getFullName().equals(candidate.getFile().getFullName()))
                .findFirst()
                .ifPresent(f -> f.setParsed(candidate.getCompilationUnit()));
    }

    /**
     * Extracts the method body from the overridden method to a new private method.
     *
     * @param candidate the refactoring candidate
     * @param parentCu  the parent compilation unit
     * @return the newly extracted method
     */
    private MethodDeclaration extractMethodOnOverriddenMethod(ZafeirisEtAl2016Candidate candidate, CompilationUnit parentCu) {
        var parentClass = AstHandler.getClassOrInterfaceDeclaration(parentCu)
                .orElseThrow(() -> new IllegalArgumentException("Parent class not found"));
        var parentMethod = findMethodInClass(parentClass, candidate.getOverriddenMethod());
        var newMethodName = buildDoMethodName(parentMethod);

        var existingMethod = AstHandler.getMethodByName(parentCu, newMethodName);
        if (existingMethod != null) {
            return existingMethod;
        }

        var newMethod = createDoMethod(parentClass, parentMethod, newMethodName);
        replaceParentMethodBodyWithCall(parentMethod, newMethodName);

        return newMethod;
    }

    /**
     * Finds a method in a class by parameter matching.
     *
     * @param classDecl the class to search
     * @param method     the method to find
     * @return the found method
     */
    private MethodDeclaration findMethodInClass(Node classDecl, MethodDeclaration method) {
        return AstHandler.getMethods(classDecl).stream()
                .filter(m -> AstHandler.methodsParamsMatch(m, method))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Method not found"));
    }

    /**
     * Builds the "do" method name from original method name.
     *
     * @param method the original method
     * @return the "do" method name
     */
    private String buildDoMethodName(MethodDeclaration method) {
        return String.format("do%s%s",
                method.getNameAsString().substring(0, 1).toUpperCase(),
                method.getNameAsString().substring(1));
    }

    /**
     * Creates a new "do" method in the parent class.
     *
     * @param parentClass    the parent class
     * @param parentMethod   the original method
     * @param newMethodName  the name for the new method
     * @return the newly created method
     */
    private MethodDeclaration createDoMethod(Node parentClass, MethodDeclaration parentMethod, String newMethodName) {
        var newMethod = parentClass.getClass().isAssignableFrom(Class.class)
                ? ((com.github.javaparser.ast.body.ClassOrInterfaceDeclaration) parentClass).addMethod(newMethodName, Modifier.privateModifier().getKeyword())
                : null;

        if (newMethod != null) {
            newMethod.setBody(parentMethod.getBody().orElseThrow(() -> new IllegalArgumentException("Method body is null")));
            newMethod.setType(parentMethod.getType());
            parentMethod.getTypeParameters().forEach(typeParam -> newMethod.getTypeParameters().add(typeParam));
            parentMethod.getParameters().forEach(newMethod::addParameter);
            parentMethod.getThrownExceptions().forEach(newMethod::addThrownException);
        }
        return newMethod;
    }

    /**
     * Replaces parent method body with call to "do" method.
     *
     * @param parentMethod  the parent method
     * @param newMethodName the "do" method name
     */
    private void replaceParentMethodBodyWithCall(MethodDeclaration parentMethod, String newMethodName) {
        var methodCallExpr = new MethodCallExpr(newMethodName);
        parentMethod.getParameters().forEach(p -> methodCallExpr.addArgument(p.getName().asString()));
        var returnStmt = new ReturnStmt(methodCallExpr);
        parentMethod.setBody(new BlockStmt(NodeList.nodeList(returnStmt)));
    }

    /**
     * Replaces the super call with a call to the new "do" method.
     *
     * @param candidate             the refactoring candidate
     * @param newOverriddenMethod    the new "do" method
     * @return the new method call expression
     */
    private MethodCallExpr replaceSuperCallWithDoOverridden(ZafeirisEtAl2016Candidate candidate, MethodDeclaration newOverriddenMethod) {
        var superExpr = AstHandler.getSuperCalls(candidate.getCompilationUnit()).getFirst();
        var superMethodCall = (MethodCallExpr) superExpr.getParentNode()
                .orElseThrow(() -> new IllegalArgumentException("Super call not found"));
        var node = AstHandler.getExpressionStatement(superExpr)
                .orElseThrow(() -> new IllegalArgumentException("Expression statement not found"));
        var newMethodCall = new MethodCallExpr(newOverriddenMethod.getNameAsString());

        superMethodCall.getArguments().forEach(newMethodCall::addArgument);

        if (node.getChildNodes().getFirst() instanceof VariableDeclarationExpr oldVariableDeclaration) {
            var variableDeclaration = replaceVariableDeclaration(newMethodCall, oldVariableDeclaration);
            node.setExpression(variableDeclaration);
        } else if (node.getChildNodes().getFirst() instanceof MethodCallExpr) {
            node.setExpression(newMethodCall);
        } else if (node.getChildNodes().getFirst() instanceof AssignExpr oldAssignment) {
            node.setExpression(new AssignExpr(oldAssignment.getTarget(), newMethodCall, Operator.ASSIGN));
        } else {
            throw new UnsupportedOperationException("Unsupported super call pattern");
        }

        return newMethodCall;
    }

    /**
     * Replaces variable declaration with new method call.
     *
     * @param newMethodCall          the new method call
     * @param oldVariableDeclaration the old variable declaration
     * @return the new variable declaration
     */
    private VariableDeclarationExpr replaceVariableDeclaration(MethodCallExpr newMethodCall, VariableDeclarationExpr oldVariableDeclaration) {
        var oldVariableDeclarator = AstHandler.getVariableDeclarator(oldVariableDeclaration.getChildNodes());
        var variableDeclarator = new VariableDeclarator(oldVariableDeclarator.getType(), oldVariableDeclarator.getName(), newMethodCall);
        return new VariableDeclarationExpr(variableDeclarator);
    }

    /**
     * Extracts before and after fragments into separate methods.
     *
     * @param candidate            the refactoring candidate
     * @param parentCU             the parent compilation unit
     * @param newDoOverriddenCall  the call to the "do" method
     */
    private void extractBeforeAndAfterFragments(ZafeirisEtAl2016Candidate candidate, CompilationUnit parentCU, MethodCallExpr newDoOverriddenCall) {
        var childMethodDecl = findOverriddenMethodInChild(candidate);
        var fragmentsSplitter = FragmentsSplitter.splitByMethodAndMethodCall(childMethodDecl, newDoOverriddenCall);

        var beforeFragmentReturnValue = applyExtractMethodOnBeforeFragment(parentCU, candidate.getCompilationUnit(),
                childMethodDecl, fragmentsSplitter);
        var afterFragmentMethod = applyExtractMethodOnAfterFragment(parentCU, candidate.getCompilationUnit(),
                childMethodDecl, fragmentsSplitter, getBeforeFragmentReturnValue(beforeFragmentReturnValue));

        replaceSourceMethodWithFragments(fragmentsSplitter, childMethodDecl, beforeFragmentReturnValue,
                newDoOverriddenCall, afterFragmentMethod);
    }

    /**
     * Finds the overridden method in the child class.
     *
     * @param candidate the refactoring candidate
     * @return the found method declaration
     */
    private MethodDeclaration findOverriddenMethodInChild(ZafeirisEtAl2016Candidate candidate) {
        return AstHandler.getMethods(candidate.getCompilationUnit()).stream()
                .filter(m -> m.getNameAsString().equals(candidate.getOverriddenMethod().getNameAsString()))
                .filter(m -> AstHandler.methodsParamsMatch(m, candidate.getOverriddenMethod()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Overridden method not found"));
    }

    /**
     * Extracts the before fragment into a separate method.
     *
     * @param parentCU            the parent compilation unit
     * @param childCU             the child compilation unit
     * @param childMethodDecl     the child method
     * @param fragmentsSplitter   the fragment splitter
     * @return the return value or method call expression
     */
    private Node applyExtractMethodOnBeforeFragment(CompilationUnit parentCU, CompilationUnit childCU,
                                                      MethodDeclaration childMethodDecl, FragmentsSplitter fragmentsSplitter) {
        var variables = fragmentsSplitter.getVariablesOnBeforeFragmentsMethodClass();
        if (variables.size() > 1) {
            throw new IllegalStateException("Too many variables in before fragment");
        }

        var childClassDecl = AstHandler.getClassOrInterfaceDeclaration(childCU)
                .orElseThrow(() -> new IllegalArgumentException("Child class not found"));
        var beforeMethodName = buildBeforeMethodName(childMethodDecl);
        var newMethod = createProtectedMethod(childClassDecl, beforeMethodName, childMethodDecl);

        fragmentsSplitter.getBeforeStatements().forEach(newMethod.getBody()::addStatement);

        if (variables.size() == 1) {
            return createMethodWithReturn(parentCU, newMethod, fragmentsSplitter, variables, childMethodDecl);
        }
        createHookMethod(parentCU, newMethod, fragmentsSplitter);
        return new MethodCallExpr(beforeMethodName);
    }

    /**
     * Builds the "before" method name.
     *
     * @param method the original method
     * @return the "before" method name
     */
    private String buildBeforeMethodName(MethodDeclaration method) {
        return String.format("before%s%s",
                method.getNameAsString().substring(0, 1).toUpperCase(),
                method.getNameAsString().substring(1));
    }

    /**
     * Creates a protected method with parameters and exceptions from source.
     *
     * @param classDecl  the class to add method to
     * @param methodName the method name
     * @param sourceMethod the source method to copy parameters/exceptions from
     * @return the new method
     */
    private MethodDeclaration createProtectedMethod(Node classDecl, String methodName, MethodDeclaration sourceMethod) {
        var newMethod = ((com.github.javaparser.ast.body.ClassOrInterfaceDeclaration) classDecl)
                .addMethod(methodName, Modifier.protectedModifier().getKeyword());

        sourceMethod.getParameters().forEach(newMethod::addParameter);
        sourceMethod.getThrownExceptions().forEach(newMethod::addThrownException);
        newMethod.setBody(new BlockStmt());

        return newMethod;
    }

    /**
     * Creates a method that returns a value.
     *
     * @param parentCU            the parent compilation unit
     * @param newMethod           the new method
     * @param fragmentsSplitter   the fragment splitter
     * @param variables           the variables
     * @param childMethodDecl     the child method
     * @return the variable declaration expression
     */
    private Node createMethodWithReturn(CompilationUnit parentCU, MethodDeclaration newMethod,
                                         FragmentsSplitter fragmentsSplitter, List<VariableDeclarationExpr> variables,
                                         MethodDeclaration childMethodDecl) {
        var varDclrExpr = variables.getFirst();
        var varDclr = varDclrExpr.getVariables().getFirst()
                .orElseThrow(() -> new VariableNotFoundException("Variable not found"));

        var methodCallExpr = new MethodCallExpr(newMethod.getNameAsString());
        newMethod.getParameters().stream()
                .map(Parameter::getName)
                .map(NameExpr::new)
                .forEach(methodCallExpr.getArguments()::add);

        var returnStmt = new ReturnStmt(new NameExpr(varDclr.getNameAsString()));
        var varDclrExprCall = new VariableDeclarationExpr(new VariableDeclarator(varDclr.getType(), varDclr.getName(), methodCallExpr));

        newMethod.getBody().get().addStatement(returnStmt);
        newMethod.setType(varDclr.getType());
        createHookMethod(parentCU, newMethod, fragmentsSplitter);

        return varDclrExprCall;
    }

    /**
     * Creates a hook method in the parent class.
     *
     * @param parentCU           the parent compilation unit
     * @param newMethodDecl      the new method declaration
     * @param fragmentsSplitter  the fragment splitter
     */
    private void createHookMethod(CompilationUnit parentCU, MethodDeclaration newMethodDecl,
                                  FragmentsSplitter fragmentsSplitter) {
        if (AstHandler.getMethodByName(parentCU, newMethodDecl.getNameAsString()) != null) {
            return;
        }

        var parentClassDecl = AstHandler.getClassOrInterfaceDeclaration(parentCU)
                .orElseThrow(() -> new IllegalArgumentException("Parent class not found"));
        var hookMethod = ((com.github.javaparser.ast.body.ClassOrInterfaceDeclaration) parentClassDecl)
                .addMethod(newMethodDecl.getNameAsString(), Modifier.protectedModifier().getKeyword());

        newMethodDecl.getParameters().forEach(p -> hookMethod.getParameters().add(new Parameter(p.getType(), p.getName())));
        newMethodDecl.getThrownExceptions().forEach(hookMethod::addThrownException);
        Optional.ofNullable(newMethodDecl.getType()).ifPresent(hookMethod::setType);

        hookMethod.setBody(new BlockStmt());
        var body = hookMethod.getBody().orElseThrow(() -> new IllegalArgumentException("Body is null"));

        fragmentsSplitter.getVariablesOnBeforeFragmentsMethodClass().stream()
                .findFirst()
                .ifPresent(body::addStatement);

        newMethodDecl.getBody()
                .filter(b -> !b.getStatements().isEmpty())
                .map(b -> b.getStatements().get(b.getStatements().size() - 1))
                .filter(ReturnStmt.class::isInstance)
                .map(ReturnStmt.class::cast)
                .ifPresent(body.getStatements()::add);
    }

    /**
     * Extracts the after fragment into a separate method.
     *
     * @param parentCU                  the parent compilation unit
     * @param childCU                   the child compilation unit
     * @param childMethodDecl           the child method
     * @param fragmentsSplitter         the fragment splitter
     * @param beforeFragmentReturnValue  the before fragment return value
     * @return the new after method
     */
    private MethodDeclaration applyExtractMethodOnAfterFragment(CompilationUnit parentCU, CompilationUnit childCU,
                                                                MethodDeclaration childMethodDecl, FragmentsSplitter fragmentsSplitter,
                                                                Optional<VariableDeclarationExpr> beforeFragmentReturnValue) {
        var childClassDecl = AstHandler.getClassOrInterfaceDeclaration(childCU)
                .orElseThrow(() -> new IllegalArgumentException("Child class not found"));
        var afterMethodName = buildAfterMethodName(childMethodDecl);
        var newMethod = ((com.github.javaparser.ast.body.ClassOrInterfaceDeclaration) childClassDecl)
                .addMethod(afterMethodName, Modifier.protectedModifier().getKeyword());

        newMethod.setType(childMethodDecl.getType());
        childMethodDecl.getTypeParameters().forEach(typeParam -> newMethod.getTypeParameters().add(typeParam));
        childMethodDecl.getParameters().forEach(newMethod::addParameter);
        childMethodDecl.getThrownExceptions().forEach(newMethod::addThrownException);

        fragmentsSplitter.getAfterStatements().forEach(newMethod.getBody().get()::addStatement);

        beforeFragmentReturnValue.ifPresent(f -> {
            var variable = f.getVariables().getFirst()
                    .orElseThrow(() -> new VariableNotFoundException("Variable not found"));
            newMethod.addParameter(variable.getType(), variable.getNameAsString());
        });

        fragmentsSplitter.getSuperReturnVariable()
                .ifPresent(returnVar -> newMethod.addParameter(returnVar.getType(), returnVar.name().asString()));

        createHookMethod(parentCU, newMethod, fragmentsSplitter);

        return newMethod;
    }

    /**
     * Builds the "after" method name.
     *
     * @param method the original method
     * @return the "after" method name
     */
    private String buildAfterMethodName(MethodDeclaration method) {
        return String.format("after%s%s",
                method.getNameAsString().substring(0, 1).toUpperCase(),
                method.getNameAsString().substring(1));
    }

    /**
     * Gets the before fragment return value as Optional.
     *
     * @param returnValue the return value node
     * @return Optional containing variable declaration
     */
    private Optional<VariableDeclarationExpr> getBeforeFragmentReturnValue(Node returnValue) {
        if (returnValue instanceof VariableDeclarationExpr varDecl) {
            return Optional.of(varDecl);
        }
        return Optional.empty();
    }

    /**
     * Replaces the source method body with calls to before/after methods.
     *
     * @param fragmentsSplitter        the fragment splitter
     * @param childMethodDecl           the child method
     * @param beforeFragmentReturnValue the before fragment return value
     * @param newDoOverriddenCall       the call to "do" method
     * @param afterFragmentMethod       the after method
     */
    private void replaceSourceMethodWithFragments(FragmentsSplitter fragmentsSplitter,
                                                    MethodDeclaration childMethodDecl, Node beforeFragmentReturnValue,
                                                    MethodCallExpr newDoOverriddenCall, MethodDeclaration afterFragmentMethod) {

        var superCallAssignment = buildSuperCallAssignment(fragmentsSplitter, newDoOverriddenCall);
        var afterFragmentCallExpr = buildAfterFragmentMethodCall(afterFragmentMethod, childMethodDecl,
                beforeFragmentReturnValue, superCallAssignment);

        var returnStmt = new ReturnStmt(afterFragmentCallExpr);
        var block = new BlockStmt();

        Optional.of(beforeFragmentReturnValue)
                .filter(Expression.class::isInstance)
                .map(Expression.class::cast)
                .ifPresentOrElse(block.getStatements()::add,
                        () -> block.getStatements().add(Optional.ofNullable((Expression) superCallAssignment)
                                .orElse(newDoOverriddenCall)));

        block.getStatements().add(returnStmt);
        childMethodDecl.setBody(block);
    }

    /**
     * Builds the super call assignment expression.
     *
     * @param fragmentsSplitter  the fragment splitter
     * @param newDoOverriddenCall the call to "do" method
     * @return the variable declaration expression or null
     */
    private VariableDeclarationExpr buildSuperCallAssignment(FragmentsSplitter fragmentsSplitter,
                                                               MethodCallExpr newDoOverriddenCall) {
        return fragmentsSplitter.getSuperReturnVariable()
                .map(returnVar -> new VariableDeclarationExpr(
                        new VariableDeclarator(returnVar.type(), "superReturnVar", newDoOverriddenCall)))
                .orElse(null);
    }

    /**
     * Builds the after fragment method call expression.
     *
     * @param afterFragmentMethod         the after method
     * @param childMethodDecl             the child method
     * @param beforeFragmentReturnValue   the before fragment return value
     * @param superCallAssignment         the super call assignment
     * @return the method call expression
     */
    private MethodCallExpr buildAfterFragmentMethodCall(MethodDeclaration afterFragmentMethod,
                                                           MethodDeclaration childMethodDecl, Node beforeFragmentReturnValue,
                                                           VariableDeclarationExpr superCallAssignment) {
        var afterFragmentCallExpr = new MethodCallExpr(afterFragmentMethod.getNameAsString());

        childMethodDecl.getParameters().forEach(p -> afterFragmentCallExpr.addArgument(p.getName().asString()));

        if (beforeFragmentReturnValue instanceof VariableDeclarationExpr varDecl) {
            afterFragmentCallExpr.addArgument(varDecl.getVariable(0).getNameAsString());
        }
        if (superCallAssignment != null) {
            afterFragmentCallExpr.addArgument(superCallAssignment.getVariable(0).getNameAsString());
        }

        return afterFragmentCallExpr;
    }

    /**
     * Pulls up the overridden method body to the parent class.
     *
     * @param candidate the refactoring candidate
     * @param parentCU  the parent compilation unit
     */
    private void pullUpMethodBody(ZafeirisEtAl2016Candidate candidate, CompilationUnit parentCU) {
        var overriddenMethodDecl = findMethodInCompilationUnit(parentCU, candidate.getOverriddenMethod());
        var overridingMethodDecl = findMethodInCompilationUnit(candidate.getCompilationUnit(), candidate.getOverridingMethod());

        overriddenMethodDecl.setBody(new BlockStmt());

        overridingMethodDecl.getBody()
                .ifPresent(b -> b.getStatements().forEach(
                        overriddenMethodDecl.getBody().orElseThrow().getStatements()::add));

        var childClass = AstHandler.getClassOrInterfaceDeclaration(candidate.getCompilationUnit())
                .orElseThrow(() -> new IllegalArgumentException("Child class not found"));

        childClass.remove(overridingMethodDecl);
    }

    /**
     * Finds a method in a compilation unit by parameter matching.
     *
     * @param cu    the compilation unit
     * @param method the method to find
     * @return the found method
     */
    private MethodDeclaration findMethodInCompilationUnit(CompilationUnit cu, MethodDeclaration method) {
        return AstHandler.getMethods(cu).stream()
                .filter(m -> AstHandler.methodsParamsMatch(m, method))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Method not found"));
    }

    /**
     * Applies final adjustments to the refactored code.
     *
     * @param candidate the refactoring candidate
     * @param parentCU  the parent compilation unit
     */
    private void applyFinalAdjustments(ZafeirisEtAl2016Candidate candidate, CompilationUnit parentCU) {
        var overriddenMethodDecl = findMethodInCompilationUnit(parentCU, candidate.getOverriddenMethod());
        overriddenMethodDecl.setFinal(true);
    }
}
