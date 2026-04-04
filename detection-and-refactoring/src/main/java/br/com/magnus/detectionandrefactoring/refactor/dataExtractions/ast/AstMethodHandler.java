package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.exceptions.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstMethodHelper.methodsParamsMatch;

/**
 * Handler for method-related AST operations.
 */
public final class AstMethodHandler {

    private AstMethodHandler() {
    }

    /**
     * Gets all methods from a compilation unit.
     *
     * @param cUnit the compilation unit
     * @return list of method declarations
     */
    public static List<MethodDeclaration> getMethods(CompilationUnit cUnit) {
        return Optional.ofNullable(cUnit)
                .map(Node::getChildNodes)
                .orElseThrow(NullCompilationUnitException::new)
                .stream()
                .filter(n -> n instanceof ClassOrInterfaceDeclaration)
                .flatMap(n -> n.getChildNodes().stream())
                .filter(cn -> cn instanceof MethodDeclaration)
                .map(MethodDeclaration.class::cast)
                .toList();
    }

    /**
     * Gets all methods from a class or interface declaration.
     *
     * @param classOrInterfaceDeclaration the class or interface
     * @return list of method declarations
     */
    public static List<MethodDeclaration> getMethods(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        return Optional.ofNullable(classOrInterfaceDeclaration)
                .map(Node::getChildNodes)
                .orElseThrow(NoClassOrInterfaceDeclarationException::new)
                .stream()
                .filter(n -> n instanceof MethodDeclaration)
                .map(MethodDeclaration.class::cast)
                .toList();
    }

    /**
     * Gets a method by name from a class.
     *
     * @param clazz the class declaration
     * @param method the method name
     * @return the found method or null
     */
    public static MethodDeclaration getMethodByName(ClassOrInterfaceDeclaration clazz, String method) {
        return getMethods(clazz).stream()
                .filter(m -> m.getNameAsString().equals(method))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets a method by name from a compilation unit.
     *
     * @param cUnit the compilation unit
     * @param method the method name
     * @return the found method or null
     */
    public static MethodDeclaration getMethodByName(CompilationUnit cUnit, String method) {
        return getMethods(cUnit).stream()
                .filter(m -> m.getNameAsString().equals(method))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves the overridden method from the parent class.
     *
     * @param parent            the parent compilation unit
     * @param overridingMethod the overriding method
     * @return the overridden method or null
     */
    public static MethodDeclaration retrieveOverriddenMethod(CompilationUnit parent, MethodDeclaration overridingMethod) {
        var childMethodName = AstNodeHandler.getSimpleName(overridingMethod)
                .orElseThrow(SimpleNameException::new)
                .asString();

        return getMethods(parent).stream()
                .filter(parentMethod -> childMethodName.equals(
                        AstNodeHandler.getSimpleName(parentMethod)
                                .orElseThrow(SimpleNameException::new))
                        && AstMethodHelper.methodsParamsMatch(overridingMethod, parentMethod))
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if two methods have matching parameters.
     *
     * @param m1 first method
     * @param m2 second method
     * @return true if parameters match
     */
    public static boolean methodsParamsMatch(MethodDeclaration m1, MethodDeclaration m2) {
        if (m1 == null || m2 == null) {
            throw new NullMethodException();
        }

        var params1 = m1.getParameters();
        var params2 = m2.getParameters();

        if (params1.size() != params2.size()) {
            return false;
        }

        for (var i = 0; i < params1.size(); i++) {
            if (i >= params2.size() || !params1.get(i).getType().equals(params2.get(i).getType())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets all if statements from a method.
     *
     * @param method the method
     * @return collection of if statements
     */
    public static Collection<IfStmt> getIfStatements(MethodDeclaration method) {
        if (method == null) {
            throw new NullMethodException();
        }

        if (method.getBody().isEmpty()) {
            return List.of();
        }

        var ifStmts = method.getBody()
                .get()
                .getStatements()
                .stream()
                .filter(IfStmt.class::isInstance)
                .map(IfStmt.class::cast)
                .toList();

        var statements = new ArrayList<IfStmt>();
        ifStmts.forEach(i -> {
            statements.add(i);
            statements.addAll(getInnerIfStatements(i));
        });

        return statements;
    }

    /**
     * Gets inner if statements from a statement.
     *
     * @param statement the statement
     * @return list of inner if statements
     */
    private static Collection<IfStmt> getInnerIfStatements(IfStmt statement) {
        if (statement == null) {
            throw new NullIfStmtException();
        }

        var inner = statement.getChildNodes()
                .stream()
                .filter(IfStmt.class::isInstance)
                .map(IfStmt.class::cast)
                .toList();

        var statements = new ArrayList<IfStmt>(inner);
        for (var singleInner : inner) {
            statements.addAll(getInnerIfStatements(singleInner));
        }
        return statements;
    }

    /**
     * Gets the block statement from a node.
     *
     * @param node the node
     * @return optional block statement
     */
    public static Optional<BlockStmt> getBlockStatement(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .flatMap(it -> it.stream()
                        .filter(BlockStmt.class::isInstance)
                        .map(BlockStmt.class::cast)
                        .findFirst());
    }

    /**
     * Gets the return type of a method.
     *
     * @param method the method
     * @return optional class or interface type
     */
    public static Optional<com.github.javaparser.ast.type.ClassOrInterfaceType> getMethodReturnClassType(MethodDeclaration method) {
        return Optional.ofNullable(method)
                .map(MethodDeclaration::getType)
                .filter(com.github.javaparser.ast.type.ClassOrInterfaceType.class::isInstance)
                .map(com.github.javaparser.ast.type.ClassOrInterfaceType.class::cast);
    }
}
