package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.exceptions.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstMethodHelper.doesMethodCallsMatch;

/**
 * Handler for generic node-level AST operations.
 * <p>
 * Provides methods for searching, filtering, and extracting nodes
 * from the AST regardless of their specific type.
 */
public final class AstNodeHandler {

    private AstNodeHandler() {
    }

    /**
     * Gets the first simple name found in the node's children.
     *
     * @param node the node to search
     * @return Optional containing the simple name if found
     * @throws NullNodeException if node is null
     */
    public static Optional<SimpleName> getSimpleName(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(SimpleName.class::isInstance)
                .map(SimpleName.class::cast)
                .findFirst();
    }

    /**
     * Checks if a node tree contains an instance of the specified class.
     *
     * @param node the node to search
     * @param clazz the class to search for
     * @return true if the class is found in the node tree
     * @throws ClassExpectedException if clazz is null
     */
    public static boolean nodeHasClazz(Node node, Class<?> clazz) {
        var nonNullClazz = Optional.ofNullable(clazz)
                .orElseThrow(ClassExpectedException::new);

        if (nonNullClazz.isInstance(node)) {
            return true;
        }

        if (node == null || node.getChildNodes() == null || node.getChildNodes().isEmpty()) {
            return false;
        }

        return node.getChildNodes().stream().anyMatch(n -> nodeHasClazz(n, nonNullClazz));
    }

    /**
     * Gets all nodes of a specific type from the node tree.
     *
     * @param node the root node
     * @param type the class type to search for
     * @param <T>  the node type
     * @return collection of nodes of the specified type
     * @throws NullNodeException     if node is null
     * @throws ClassExpectedException if type is null
     */
    public static <T extends Node> Collection<T> getNodeByType(Node node, Class<T> type) {
        if (node == null) {
            throw new NullNodeException();
        }
        if (type == null) {
            throw new ClassExpectedException();
        }

        var nodes = new ArrayList<T>();

        if (type.isInstance(node)) {
            nodes.add(type.cast(node));
        } else if (node.getChildNodes().isEmpty()) {
            return nodes;
        }

        nodes.addAll(node.getChildNodes().stream()
                .map(n -> getNodeByType(n, type))
                .flatMap(Collection::stream)
                .toList());

        return nodes;
    }

    /**
     * Gets all if statements from a method, including nested ones.
     *
     * @param method the method to search
     * @return collection of all if statements
     * @throws NullMethodException if method is null
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
     * Recursively gets all nested if statements within an if statement.
     *
     * @param statement the if statement
     * @return list of nested if statements
     * @throws NullIfStmtException if statement is null
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
     * Gets the first literal expression from a node's children.
     *
     * @param node the node to search
     * @return Optional containing the literal expression if found
     * @throws NullNodeException if node is null
     */
    public static Optional<LiteralExpr> getLiteralExpr(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(LiteralExpr.class::isInstance)
                .map(LiteralExpr.class::cast)
                .findFirst();
    }

    /**
     * Gets the first variable declarator from a list of nodes.
     *
     * @param nodes the nodes to search
     * @return the first variable declarator found
     * @throws VariableDeclarationExpectedException if no declarator found
     */
    public static VariableDeclarator getVariableDeclarator(List<Node> nodes) {
        return Optional.ofNullable(nodes)
                .stream()
                .flatMap(List::stream)
                .filter(VariableDeclarator.class::isInstance)
                .map(VariableDeclarator.class::cast)
                .findFirst()
                .orElseThrow(VariableDeclarationExpectedException::new);
    }

    /**
     * Gets the class or interface declaration from a compilation unit.
     *
     * @param cUnit the compilation unit
     * @return Optional containing the class or interface declaration
     * @throws NoClassOrInterfaceException if cUnit is null
     */
    public static Optional<ClassOrInterfaceDeclaration> getClassOrInterfaceDeclaration(CompilationUnit cUnit) {
        return Optional.ofNullable(cUnit)
                .map(Node::getChildNodes)
                .orElseThrow(NoClassOrInterfaceException::new)
                .stream()
                .filter(ClassOrInterfaceDeclaration.class::isInstance)
                .map(ClassOrInterfaceDeclaration.class::cast)
                .findFirst();
    }

    /**
     * Gets the block statement from a node's children.
     *
     * @param node the node to search
     * @return Optional containing the block statement if found
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
     * Gets all declared fields from a node's children.
     *
     * @param node the node to search
     * @return collection of field declarations
     * @throws NullNodeException if node is null
     */
    public static Collection<FieldDeclaration> getDeclaredFields(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(FieldDeclaration.class::isInstance)
                .map(FieldDeclaration.class::cast)
                .toList();
    }

    /**
     * Gets the first object creation expression from a node's children.
     *
     * @param node the node to search
     * @return Optional containing the object creation expression if found
     * @throws NullNodeException if node is null
     */
    public static Optional<ObjectCreationExpr> getObjectCreationExpr(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(ObjectCreationExpr.class::isInstance)
                .map(ObjectCreationExpr.class::cast)
                .findFirst();
    }

    /**
     * Gets the return statement from an if statement's then block.
     *
     * @param ifStmt the if statement
     * @return Optional containing the return statement if found
     * @throws NullIfStmtException if ifStmt is null
     */
    public static Optional<ReturnStmt> getReturnStmt(IfStmt ifStmt) {
        if (ifStmt == null) {
            throw new NullIfStmtException();
        }

        if (ifStmt.hasThenBlock()) {
            return ifStmt.getThenStmt()
                    .getChildNodes()
                    .stream()
                    .filter(ReturnStmt.class::isInstance)
                    .map(ReturnStmt.class::cast)
                    .findFirst();
        }
        return Optional.empty();
    }

    /**
     * Gets the first name expression from a node's children.
     *
     * @param node the node to search
     * @return Optional containing the name expression if found
     * @throws NullNodeException if node is null
     */
    public static Optional<NameExpr> getNameExpr(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(NameExpr.class::isInstance)
                .map(NameExpr.class::cast)
                .findFirst();
    }

    /**
     * Gets the expression statement for a node.
     * <p>
     * Traverses up the parent chain to find the enclosing expression statement.
     *
     * @param node the node
     * @return Optional containing the expression statement
     */
    public static Optional<ExpressionStmt> getExpressionStatement(Node node) {
        if (node == null || node instanceof BlockStmt || node instanceof ClassOrInterfaceDeclaration) {
            return Optional.empty();
        }
        if (node instanceof ExpressionStmt) {
            return Optional.of((ExpressionStmt) node);
        }
        return getExpressionStatement(node.getParentNode().orElse(null));
    }

    /**
     * Gets all method call expressions from a node tree.
     *
     * @param node the root node
     * @return list of method call expressions
     */
    public static List<MethodCallExpr> getMethodCallExpr(Node node) {
        var methodCalls = new ArrayList<MethodCallExpr>();

        if (node == null) {
            return methodCalls;
        } else if (node instanceof MethodCallExpr) {
            methodCalls.add((MethodCallExpr) node);
        } else if (node.getChildNodes() == null || node.getChildNodes().isEmpty()) {
            return methodCalls;
        }

        methodCalls.addAll(node.getChildNodes().stream()
                .map(AstNodeHandler::getMethodCallExpr)
                .flatMap(Collection::stream)
                .toList());

        return methodCalls;
    }

    /**
     * Checks if a node contains a matching method call.
     *
     * @param node       the node to search
     * @param methodCall the method call to match against
     * @return true if a matching method call is found
     */
    public static boolean doesNodeContainMatchingMethodCall(Node node, MethodCallExpr methodCall) {
        var methodCalls = getMethodCallExpr(node);
        return methodCalls.stream().anyMatch(m -> AstMethodHelper.doesMethodCallsMatch(m, methodCall));
    }

    /**
     * Finds a method by name in a class or interface declaration.
     *
     * @param clazz  the class to search
     * @param method the method name
     * @return the method declaration, or null if not found
     */
    public static MethodDeclaration getMethodByName(ClassOrInterfaceDeclaration clazz, String method) {
        return AstMethodHandler.getMethods(clazz).stream()
                .filter(m -> m.getNameAsString().equals(method))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds a method by name in a compilation unit.
     *
     * @param cUnit  the compilation unit
     * @param method the method name
     * @return the method declaration, or null if not found
     */
    public static MethodDeclaration getMethodByName(CompilationUnit cUnit, String method) {
        return AstMethodHandler.getMethods(cUnit).stream()
                .filter(m -> m.getNameAsString().equals(method))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all object creation expressions from a node tree, excluding if statements.
     *
     * @param node the root node
     * @return list of object creation expressions
     * @throws NullNodeException if node is null
     */
    public static List<ObjectCreationExpr> getObjectCreationExprList(Node node) {
        if (node == null) {
            throw new NullNodeException();
        }

        var objectCreationExprs = new ArrayList<ObjectCreationExpr>();
        if (node instanceof ObjectCreationExpr) {
            objectCreationExprs.add((ObjectCreationExpr) node);
        }

        for (var child : node.getChildNodes()) {
            if (!(child instanceof IfStmt)) {
                objectCreationExprs.addAll(getObjectCreationExprList(child));
            }
        }

        return objectCreationExprs;
    }

    /**
     * Checks if a variable is used in a method call.
     *
     * @param var         the variable declaration
     * @param methodCall  the method call to check
     * @return true if the variable is present in the method call
     * @throws MethodCallExpectedException if methodCall is null
     */
    public static boolean variableIsPresentInMethodCall(VariableDeclarationExpr var, MethodCallExpr methodCall) {
        var simpleNameList = Optional.ofNullable(methodCall)
                .map(MethodCallExpr::getChildNodes)
                .orElseThrow(MethodCallExpectedException::new)
                .stream()
                .filter(NameExpr.class::isInstance)
                .map(NameExpr.class::cast)
                .map(NameExpr::getName)
                .toList();

        for (var paramName : simpleNameList) {
            if (AstVariableHandler.getVariableName(var).equals(paramName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the parent compilation unit by type name.
     *
     * @param cUnit      the child compilation unit
     * @param allClasses all available compilation units
     * @return Optional containing the parent compilation unit
     */
    public static Optional<CompilationUnit> getParent(CompilationUnit cUnit, Collection<CompilationUnit> allClasses) {
        var parentDef = AstTypeHandler.getParentType(cUnit);

        if (parentDef.isPresent()) {
            var typeName = getSimpleName(parentDef.get())
                    .orElseThrow(SimpleNameException::new);

            for (var parent : allClasses) {
                var declaration = getClassOrInterfaceDeclaration(parent);
                var isClassNameEqualsTypeName = declaration.map(dcl -> getSimpleName(dcl)
                                .orElseThrow(SimpleNameException::new))
                        .filter(typeName::equals)
                        .isPresent();

                if (isClassNameEqualsTypeName) {
                    return Optional.of(parent);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if a specific compilation unit is the parent of another.
     *
     * @param cUnit  the child compilation unit
     * @param parent the potential parent compilation unit
     * @return Optional containing the parent if it matches
     */
    public static Optional<CompilationUnit> getParent(CompilationUnit cUnit, CompilationUnit parent) {
        var parentDef = AstTypeHandler.getParentType(cUnit);

        if (parentDef.isPresent()) {
            var typeName = getSimpleName(parentDef.get())
                    .orElseThrow(SimpleNameException::new);

            var declaration = getClassOrInterfaceDeclaration(parent);
            var isClassNameEqualsTypeName = declaration.map(dcl -> getSimpleName(dcl)
                            .orElseThrow(SimpleNameException::new))
                    .filter(typeName::equals)
                    .isPresent();

            if (isClassNameEqualsTypeName) {
                return Optional.of(parent);
            }
        }
        return Optional.empty();
    }
}
