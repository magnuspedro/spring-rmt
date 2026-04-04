package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.exceptions.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithCondition;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Facade for AST operations.
 * <p>
 * This class provides a unified interface to all AST handlers, delegating
 * to specialized handler classes for specific node types and operations.
 * <p>
 * Methods are organized by responsibility:
 * <ul>
 *   <li>Method operations → {@link AstMethodHandler}</li>
 *   <li>Variable operations → {@link AstVariableHandler}</li>
 *   <li>Super call operations → {@link AstSuperCallHandler}</li>
 *   <li>Statement operations → {@link AstStatementHandler}</li>
 *   <li>Type operations → {@link AstTypeHandler}</li>
 *   <li>Node operations → {@link AstNodeHandler}</li>
 *   <li>Static utilities → {@link AstMethodHelper}</li>
 * </ul>
 *
 * @deprecated Use the specific handler classes directly for better type safety
 *             and discoverability. This facade is maintained for backward compatibility.
 */
@Deprecated(since = "1.0", forRemoval = false)
public final class AstHandler {

    private AstHandler() {
    }

    // ===== Field Operations =====

    /**
     * Gets all declared fields from a node's children.
     *
     * @param node the node to search
     * @return collection of field declarations
     * @throws NullNodeException if node is null
     * @see AstNodeHandler#getDeclaredFields(Node)
     */
    public static Collection<FieldDeclaration> getDeclaredFields(Node node) {
        return AstNodeHandler.getDeclaredFields(node);
    }

    // ===== Object Creation Operations =====

    /**
     * Gets the first object creation expression from a node's children.
     *
     * @param node the node to search
     * @return Optional containing the object creation expression
     * @throws NullNodeException if node is null
     * @see AstNodeHandler#getObjectCreationExpr(Node)
     */
    public static Optional<ObjectCreationExpr> getObjectCreationExpr(Node node) {
        return AstNodeHandler.getObjectCreationExpr(node);
    }

    /**
     * Gets all object creation expressions from a node tree.
     *
     * @param node the root node
     * @return list of object creation expressions
     * @throws NullNodeException if node is null
     * @see AstNodeHandler#getObjectCreationExprList(Node)
     */
    public static List<ObjectCreationExpr> getObjectCreationExprList(Node node) {
        return AstNodeHandler.getObjectCreationExprList(node);
    }

    // ===== Statement Operations =====

    /**
     * Gets the return statement from an if statement's then block.
     *
     * @param ifStmt the if statement
     * @return Optional containing the return statement
     * @throws NullIfStmtException if ifStmt is null
     * @see AstNodeHandler#getReturnStmt(IfStmt)
     */
    public static Optional<ReturnStmt> getReturnStmt(IfStmt ifStmt) {
        return AstNodeHandler.getReturnStmt(ifStmt);
    }

    /**
     * Gets the expression statement for a node.
     *
     * @param node the node
     * @return Optional containing the expression statement
     * @see AstStatementHandler#getExpressionStatement(Node)
     */
    public static Optional<ExpressionStmt> getExpressionStatement(Node node) {
        return AstStatementHandler.getExpressionStatement(node);
    }

    /**
     * Checks if a node tree contains a return statement.
     *
     * @param node the node to check
     * @return true if return statement found
     * @see AstStatementHandler#nodeHasReturnStatement(Node)
     */
    public static boolean nodeHasReturnStatement(Node node) {
        return AstStatementHandler.nodeHasReturnStatement(node);
    }

    /**
     * Checks if a node tree contains a throw statement.
     *
     * @param node the node to check
     * @return true if throw statement found
     * @see AstNodeHandler#nodeHasClazz(Node, Class)
     */
    public static boolean nodeThrowsException(Node node) {
        return AstNodeHandler.nodeHasClazz(node, ThrowStmt.class);
    }

    // ===== Name Expression Operations =====

    /**
     * Gets the first name expression from a node's children.
     *
     * @param node the node to search
     * @return Optional containing the name expression
     * @throws NullNodeException if node is null
     * @see AstNodeHandler#getNameExpr(Node)
     */
    public static Optional<NameExpr> getNameExpr(Node node) {
        return AstNodeHandler.getNameExpr(node);
    }

    /**
     * Gets the first simple name from a node's children.
     *
     * @param node the node to search
     * @return Optional containing the simple name
     * @throws NullNodeException if node is null
     * @see AstNodeHandler#getSimpleName(Node)
     */
    public static Optional<SimpleName> getSimpleName(Node node) {
        return AstNodeHandler.getSimpleName(node);
    }

    // ===== Variable Operations =====

    /**
     * Gets the simple name from a variable declaration expression.
     *
     * @param node the variable declaration expression
     * @return Optional containing the simple name
     * @throws NullNodeException if node is null
     * @see AstVariableHandler#getVariableSimpleName(VariableDeclarationExpr)
     */
    public static Optional<SimpleName> getVariableSimpleName(VariableDeclarationExpr node) {
        return AstVariableHandler.getVariableSimpleName(node);
    }

    /**
     * Gets the variable name from a variable declaration expression.
     *
     * @param var the variable declaration
     * @return the simple name
     * @throws VariableDeclarationExpectedException if var is null
     * @see AstVariableHandler#getVariableName(VariableDeclarationExpr)
     */
    public static SimpleName getVariableName(VariableDeclarationExpr var) {
        return AstVariableHandler.getVariableName(var);
    }

    /**
     * Checks if two variables have matching names.
     *
     * @param var1 first variable
     * @param var2 second variable
     * @return true if names match
     * @see AstMethodHelper#doVariableNamesMatch(VariableDeclarationExpr, VariableDeclarationExpr)
     */
    public static boolean doVariablesNameMatch(VariableDeclarationExpr var1, VariableDeclarationExpr var2) {
        return AstMethodHelper.doVariableNamesMatch(var1, var2);
    }

    /**
     * Checks if a node uses a specific variable.
     *
     * @param node the node to check
     * @param var  the variable
     * @return true if variable is used
     * @see AstVariableHandler#doesNodeUsesVar(Node, VariableDeclarator)
     */
    public static boolean doesNodeUsesVar(Node node, VariableDeclarator var) {
        return AstVariableHandler.doesNodeUsesVar(node, var);
    }

    /**
     * Gets all variable declarations from a node tree.
     *
     * @param node the root node
     * @return list of variable declarators
     * @throws NullNodeException if node is null
     * @see AstVariableHandler#getVariableDeclarations(Node)
     */
    public static Collection<VariableDeclarator> getVariableDeclarations(Node node) {
        return AstVariableHandler.getVariableDeclarations(node);
    }

    /**
     * Gets a variable declaration by name from a node tree.
     *
     * @param node       the node to search
     * @param returnName the variable name
     * @return Optional containing the variable declarator
     * @throws NullNodeException if node is null
     * @see AstVariableHandler#getVariableDeclarationInNode(Node, String)
     */
    public static Optional<VariableDeclarator> getVariableDeclarationInNode(Node node, String returnName) {
        return AstVariableHandler.getVariableDeclarationInNode(node, returnName);
    }

    /**
     * Extracts variable declaration expressions from a node tree.
     *
     * @param node the root node
     * @return collection of variable declaration expressions
     * @throws NullNodeException if node is null
     */
    public static Collection<VariableDeclarationExpr> extractVariableDclrFromNode(Node node) {
        var nonNullNode = Optional.ofNullable(node)
                .orElseThrow(NullNodeException::new);

        if (node instanceof VariableDeclarationExpr) {
            var variables = new ArrayList<VariableDeclarationExpr>();
            variables.add((VariableDeclarationExpr) node);
            return variables;
        }

        if (node.getChildNodes() == null || node.getChildNodes().isEmpty()) {
            return new ArrayList<>();
        }
        return node.getChildNodes().stream()
                .flatMap(cn -> extractVariableDclrFromNode(cn).stream())
                .collect(Collectors.toList());
    }

    /**
     * Checks if a variable is used in a method call.
     *
     * @param var         the variable declaration
     * @param methodCall  the method call
     * @return true if variable is present in method call
     * @throws MethodCallExpectedException if methodCall is null
     * @see AstNodeHandler#variableIsPresentInMethodCall(VariableDeclarationExpr, MethodCallExpr)
     */
    public static boolean variableIsPresentInMethodCall(VariableDeclarationExpr var, MethodCallExpr methodCall) {
        return AstNodeHandler.variableIsPresentInMethodCall(var, methodCall);
    }

    /**
     * Checks if a node tree contains a specific simple name.
     *
     * @param name the simple name to search for
     * @param node the root node
     * @return true if the simple name is found
     * @throws SimpleNameException if name is null
     * @throws NullNodeException   if node is null
     */
    public static boolean nodeHasSimpleName(SimpleName name, Node node) {
        var nonNullName = Optional.ofNullable(name)
                .orElseThrow(SimpleNameException::new);
        var nonNullNode = Optional.ofNullable(node)
                .orElseThrow(NullNodeException::new);

        if (nonNullNode instanceof SimpleName && nonNullNode.equals(nonNullName)) {
            return true;
        }

        if (nonNullNode.getChildNodes() == null || nonNullNode.getChildNodes().isEmpty()) {
            return false;
        }

        return nonNullNode.getChildNodes()
                .stream()
                .anyMatch(n -> nodeHasSimpleName(nonNullName, n));
    }

    // ===== Type and Class Operations =====

    /**
     * Gets the parent type of a compilation unit.
     *
     * @param cUnit the compilation unit
     * @return Optional containing the parent type
     * @see AstTypeHandler#getParentType(CompilationUnit)
     */
    public static Optional<ClassOrInterfaceType> getParentType(CompilationUnit cUnit) {
        return AstTypeHandler.getParentType(cUnit);
    }

    /**
     * Gets the parent type of a class declaration.
     *
     * @param classDclr the class declaration
     * @return Optional containing the parent type
     * @see AstTypeHandler#getParentType(ClassOrInterfaceDeclaration)
     */
    public static Optional<ClassOrInterfaceType> getParentType(ClassOrInterfaceDeclaration classDclr) {
        return AstTypeHandler.getParentType(classDclr);
    }

    /**
     * Finds the parent compilation unit by type name.
     *
     * @param cUnit      the child compilation unit
     * @param allClasses all available compilation units
     * @return Optional containing the parent compilation unit
     * @see AstNodeHandler#getParent(CompilationUnit, Collection)
     */
    public static Optional<CompilationUnit> getParent(CompilationUnit cUnit, Collection<CompilationUnit> allClasses) {
        return AstNodeHandler.getParent(cUnit, allClasses);
    }

    /**
     * Checks if a specific compilation unit is the parent of another.
     *
     * @param cUnit  the child compilation unit
     * @param parent the potential parent compilation unit
     * @return Optional containing the parent if it matches
     * @see AstNodeHandler#getParent(CompilationUnit, CompilationUnit)
     */
    public static Optional<CompilationUnit> getParent(CompilationUnit cUnit, CompilationUnit parent) {
        return AstNodeHandler.getParent(cUnit, parent);
    }

    /**
     * Gets the package declaration from a compilation unit.
     *
     * @param cUnit the compilation unit
     * @return the package declaration
     * @throws NullCompilationUnitException if cUnit is null
     * @throws NoPackageDeclarationException if no package declaration found
     * @see AstTypeHandler#getPackageDeclaration(CompilationUnit)
     */
    public static PackageDeclaration getPackageDeclaration(CompilationUnit cUnit) {
        return AstTypeHandler.getPackageDeclaration(cUnit);
    }

    /**
     * Checks if two compilation units match by package and class name.
     *
     * @param c1 first compilation unit
     * @param c2 second compilation unit
     * @return true if they match
     * @see AstTypeHandler#doesCompilationUnitsMatch(CompilationUnit, CompilationUnit)
     */
    public static boolean doesCompilationUnitsMatch(CompilationUnit c1, CompilationUnit c2) {
        return AstTypeHandler.doesCompilationUnitsMatch(c1, c2);
    }

    /**
     * Checks if two compilation units match.
     *
     * @param c1                  first compilation unit
     * @param classOrInterface2   optional class declaration
     * @param package2            optional package declaration
     * @return true if they match
     * @see AstTypeHandler#doesCompilationUnitsMatch(CompilationUnit, Optional, Optional)
     */
    public static boolean doesCompilationUnitsMatch(CompilationUnit c1, Optional<ClassOrInterfaceDeclaration> classOrInterface2,
                                                    Optional<PackageDeclaration> package2) {
        return AstTypeHandler.doesCompilationUnitsMatch(c1, classOrInterface2, package2);
    }

    /**
     * Gets the class or interface declaration from a compilation unit.
     *
     * @param cUnit the compilation unit
     * @return Optional containing the class or interface declaration
     * @throws NoClassOrInterfaceException if cUnit is null
     * @see AstNodeHandler#getClassOrInterfaceDeclaration(CompilationUnit)
     */
    public static Optional<ClassOrInterfaceDeclaration> getClassOrInterfaceDeclaration(CompilationUnit cUnit) {
        return AstNodeHandler.getClassOrInterfaceDeclaration(cUnit);
    }

    /**
     * Gets the method return type as a class or interface type.
     *
     * @param method the method declaration
     * @return Optional containing the return type
     * @see AstMethodHandler#getMethodReturnClassType(MethodDeclaration)
     */
    public static Optional<ClassOrInterfaceType> getMethodReturnClassType(MethodDeclaration method) {
        return AstMethodHandler.getMethodReturnClassType(method);
    }

    // ===== Method Operations =====

    /**
     * Gets all methods from a compilation unit.
     *
     * @param cUnit the compilation unit
     * @return list of method declarations
     * @see AstMethodHandler#getMethods(CompilationUnit)
     */
    public static List<MethodDeclaration> getMethods(CompilationUnit cUnit) {
        return AstMethodHandler.getMethods(cUnit);
    }

    /**
     * Gets all methods from a class or interface declaration.
     *
     * @param classOrInterfaceDeclaration the class or interface
     * @return list of method declarations
     * @see AstMethodHandler#getMethods(ClassOrInterfaceDeclaration)
     */
    public static List<MethodDeclaration> getMethods(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        return AstMethodHandler.getMethods(classOrInterfaceDeclaration);
    }

    /**
     * Finds a method by name in a class.
     *
     * @param clazz  the class declaration
     * @param method the method name
     * @return the method or null if not found
     * @see AstNodeHandler#getMethodByName(ClassOrInterfaceDeclaration, String)
     */
    public static MethodDeclaration getMethodByName(ClassOrInterfaceDeclaration clazz, String method) {
        return AstNodeHandler.getMethodByName(clazz, method);
    }

    /**
     * Finds a method by name in a compilation unit.
     *
     * @param cUnit  the compilation unit
     * @param method the method name
     * @return the method or null if not found
     * @see AstNodeHandler#getMethodByName(CompilationUnit, String)
     */
    public static MethodDeclaration getMethodByName(CompilationUnit cUnit, String method) {
        return AstNodeHandler.getMethodByName(cUnit, method);
    }

    /**
     * Retrieves the overridden method from the parent class.
     *
     * @param parent            the parent compilation unit
     * @param overridingMethod the overriding method
     * @return the overridden method or null
     * @see AstMethodHandler#retrieveOverriddenMethod(CompilationUnit, MethodDeclaration)
     */
    public static MethodDeclaration retrieveOverriddenMethod(CompilationUnit parent,
                                                             MethodDeclaration overridingMethod) {
        return AstMethodHandler.retrieveOverriddenMethod(parent, overridingMethod);
    }

    /**
     * Checks if two methods have matching parameters.
     *
     * @param m1 first method
     * @param m2 second method
     * @return true if parameters match
     * @see AstMethodHandler#methodsParamsMatch(MethodDeclaration, MethodDeclaration)
     */
    public static boolean methodsParamsMatch(MethodDeclaration m1, MethodDeclaration m2) {
        return AstMethodHandler.methodsParamsMatch(m1, m2);
    }

    /**
     * Gets all if statements from a method.
     *
     * @param method the method
     * @return collection of if statements
     * @see AstMethodHandler#getIfStatements(MethodDeclaration)
     */
    public static Collection<IfStmt> getIfStatements(MethodDeclaration method) {
        return AstMethodHandler.getIfStatements(method);
    }

    /**
     * Gets the block statement from a node.
     *
     * @param node the node
     * @return Optional containing the block statement
     * @see AstNodeHandler#getBlockStatement(Node)
     */
    public static Optional<BlockStmt> getBlockStatement(Node node) {
        return AstNodeHandler.getBlockStatement(node);
    }

    // ===== Super Call Operations =====

    /**
     * Gets all super calls from a node tree.
     *
     * @param node the root node
     * @return list of super expressions
     * @throws NullNodeException if node is null
     * @see AstSuperCallHandler#getSuperCalls(Node)
     */
    public static List<SuperExpr> getSuperCalls(Node node) {
        return AstSuperCallHandler.getSuperCalls(node);
    }

    /**
     * Checks if a child node has a direct super call.
     *
     * @param node the node to check
     * @return true if direct super call found
     * @throws NullNodeException if node is null
     * @see AstSuperCallHandler#childHasDirectSuperCall(Node)
     */
    public static boolean childHasDirectSuperCall(Node node) {
        return AstSuperCallHandler.childHasDirectSuperCall(node);
    }

    // ===== Generic Node Operations =====

    /**
     * Checks if a node tree contains an instance of the specified class.
     *
     * @param node  the node to search
     * @param clazz the class to search for
     * @return true if the class is found
     * @throws ClassExpectedException if clazz is null
     * @see AstNodeHandler#nodeHasClazz(Node, Class)
     */
    public static boolean nodeHasClazz(Node node, Class<?> clazz) {
        return AstNodeHandler.nodeHasClazz(node, clazz);
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
     * @see AstNodeHandler#getNodeByType(Node, Class)
     */
    public static <T extends Node> Collection<T> getNodeByType(Node node, Class<T> type) {
        return AstNodeHandler.getNodeByType(node, type);
    }

    /**
     * Gets all method call expressions from a node tree.
     *
     * @param node the root node
     * @return list of method call expressions
     * @see AstNodeHandler#getMethodCallExpr(Node)
     */
    public static List<MethodCallExpr> getMethodCallExpr(Node node) {
        return AstNodeHandler.getMethodCallExpr(node);
    }

    /**
     * Checks if a node contains a matching method call.
     *
     * @param node       the node to search
     * @param methodCall the method call to match against
     * @return true if a matching method call is found
     * @see AstNodeHandler#doesNodeContainMatchingMethodCall(Node, MethodCallExpr)
     */
    public static boolean doesNodeContainMatchingMethodCall(Node node, MethodCallExpr methodCall) {
        return AstNodeHandler.doesNodeContainMatchingMethodCall(node, methodCall);
    }

    /**
     * Checks if two method calls match.
     *
     * @param mc1 first method call
     * @param mc2 second method call
     * @return true if they match
     * @throws NullMethodException if either method call is null
     * @see AstMethodHelper#doesMethodCallsMatch(MethodCallExpr, MethodCallExpr)
     */
    public static boolean doesMethodCallsMatch(MethodCallExpr mc1, MethodCallExpr mc2) {
        return AstMethodHelper.doesMethodCallsMatch(mc1, mc2);
    }

    /**
     * Gets the first literal expression from a node's children.
     *
     * @param node the node to search
     * @return Optional containing the literal expression
     * @throws NullNodeException if node is null
     * @see AstNodeHandler#getLiteralExpr(Node)
     */
    public static Optional<LiteralExpr> getLiteralExpr(Node node) {
        return AstNodeHandler.getLiteralExpr(node);
    }

    /**
     * Gets the first variable declarator from a list of nodes.
     *
     * @param nodes the nodes to search
     * @return the variable declarator
     * @throws VariableDeclarationExpectedException if no declarator found
     * @see AstNodeHandler#getVariableDeclarator(List)
     */
    public static VariableDeclarator getVariableDeclarator(List<Node> nodes) {
        return AstNodeHandler.getVariableDeclarator(nodes);
    }

    // ===== Private Helper Methods =====

    /**
     * Checks if parameter position is out of bounds.
     *
     * @param position the position
     * @param list     the list
     * @return true if out of bounds
     * @see AstMethodHelper#isPositionOutOfBounds(int, NodeList)
     */
    private static boolean isPositionOutOfBounds(int position, NodeList<?> list) {
        return AstMethodHelper.isPositionOutOfBounds(position, list);
    }
}
