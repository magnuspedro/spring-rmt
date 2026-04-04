package br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.NodeConverter;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Splits method body into fragments based on super call position.
 * <p>
 * The before fragment contains statements before the super call,
 * and the after fragment contains statements after the super call.
 * The super call statement itself is stored as the pivot node.
 */
@Slf4j
@NoArgsConstructor
public class FragmentsSplitter {

    private final List<Node> beforeFragment = new ArrayList<>();
    private Node pivotNode;
    private final List<Node> afterFragment = new ArrayList<>();

    /**
     * Splits a method into fragments based on super call position.
     *
     * @param method the method to split
     * @return FragmentsSplitter with populated fragments
     * @throws IllegalArgumentException if method has no body
     */
    public static FragmentsSplitter splitByMethod(MethodDeclaration method) {
        var fragment = new FragmentsSplitter();
        var blockStmt = AstHandler.getBlockStatement(method)
                .orElseThrow(() -> new IllegalArgumentException("Method has no body"));

        var hasPassedSuper = false;
        for (var child : blockStmt.getChildNodes()) {
            if (AstHandler.childHasDirectSuperCall(child)) {
                hasPassedSuper = true;
                fragment.pivotNode = child;
                continue;
            }
            fragment.addToFragment(hasPassedSuper, child);
        }

        if (fragment.pivotNode == null) {
            log.warn("Fragment Splitter node is null, in method splitByMethod");
        }
        return fragment;
    }

    /**
     * Splits a method into fragments based on method call position.
     *
     * @param method      the method to split
     * @param methodCall the method call to use as pivot
     * @return FragmentsSplitter with populated fragments
     * @throws IllegalArgumentException if method has no body
     */
    public static FragmentsSplitter splitByMethodAndMethodCall(MethodDeclaration method, MethodCallExpr methodCall) {
        var fragment = new FragmentsSplitter();
        var blockStmt = AstHandler.getBlockStatement(method)
                .orElseThrow(() -> new IllegalArgumentException("Method has no body"));

        var hasPassedMethodCall = false;
        for (var child : blockStmt.getChildNodes()) {
            if (AstHandler.doesNodeContainMatchingMethodCall(child, methodCall)) {
                hasPassedMethodCall = true;
                fragment.pivotNode = child;
                continue;
            }
            fragment.addToFragment(hasPassedMethodCall, child);
        }

        if (fragment.pivotNode == null) {
            log.warn("Fragment Splitter node is null");
        }
        return fragment;
    }

    /**
     * Adds a node to the appropriate fragment based on position.
     *
     * @param hasPassedPivot true if pivot node has been encountered
     * @param node           the node to add
     */
    private void addToFragment(boolean hasPassedPivot, Node node) {
        if (hasPassedPivot) {
            afterFragment.add(node);
        } else {
            beforeFragment.add(node);
        }
    }

    /**
     * Returns an unmodifiable view of the before fragment.
     *
     * @return immutable list of nodes before the pivot
     */
    public List<Node> getBeforeFragment() {
        return List.copyOf(beforeFragment);
    }

    /**
     * Returns an unmodifiable view of the after fragment.
     *
     * @return immutable list of nodes after the pivot
     */
    public List<Node> getAfterFragment() {
        return List.copyOf(afterFragment);
    }

    /**
     * Returns the pivot node if present.
     *
     * @return Optional containing the node, or empty if not set
     */
    public Optional<Node> getNode() {
        return Optional.ofNullable(pivotNode);
    }

    /**
     * Checks if a specific pivot node was found.
     *
     * @return true if node is present
     */
    public boolean hasSpecificNode() {
        return pivotNode != null;
    }

    /**
     * Gets variables on before fragments that are referenced in method calls.
     *
     * @return list of variable declarations
     */
    public List<VariableDeclarationExpr> getVariablesOnBeforeFragmentsMethodClass() {
        var variables = beforeFragment.stream()
                .flatMap(node -> AstHandler.extractVariableDclrFromNode(node).stream())
                .toList();

        var methodCall = AstHandler.getMethodCallExpr(pivotNode).stream().findFirst();

        if (methodCall.isEmpty()) {
            log.info("Method call not found - {}", NodeConverter.toString(pivotNode));
            return List.of();
        }

        var referencedVariables = new ArrayList<VariableDeclarationExpr>();
        for (var variable : variables) {
            if (AstHandler.variableIsPresentInMethodCall(variable, methodCall.get())) {
                referencedVariables.add(variable);
                continue;
            }
            if (afterFragmentContainsVariable(variable)) {
                referencedVariables.add(variable);
            }
        }
        return referencedVariables;
    }

    /**
     * Extracts the super return variable from the pivot node.
     *
     * @return Optional containing SuperReturnVar if present
     */
    public Optional<SuperReturnVar> getSuperReturnVariable() {
        return getNode()
                .map(Node::getChildNodes)
                .filter(children -> !children.isEmpty())
                .flatMap(children -> children.getFirst()
                        .map(this::extractSuperReturnVarFromChild));
    }

    /**
     * Extracts super return variable from a child node using pattern matching.
     *
     * @param child the child node
     * @return Optional containing SuperReturnVar if found
     */
    private Optional<SuperReturnVar> extractSuperReturnVarFromChild(Node child) {
        if (child instanceof VariableDeclarationExpr varDecl) {
            return SuperReturnVar.fromVariableDeclaration(varDecl);
        }
        if (child instanceof AssignExpr assign) {
            return SuperReturnVar.fromAssignment(assign, this);
        }
        return Optional.empty();
    }

    /**
     * Checks if the after fragment contains a reference to the variable.
     *
     * @param variable the variable declaration
     * @return true if variable is referenced in after fragment
     */
    private boolean afterFragmentContainsVariable(VariableDeclarationExpr variable) {
        return afterFragment.stream()
                .anyMatch(node -> AstHandler.nodeHasSimpleName(AstHandler.getVariableName(variable), node));
    }

    /**
     * Gets the type of a variable from its name expression.
     *
     * @param nameExpr the name expression
     * @return the variable type
     */
    private Type getTypeOfVar(NameExpr nameExpr) {
        var declarations = List.of(beforeFragment.stream(), Optional.ofNullable(pivotNode).stream(), afterFragment.stream())
                .stream()
                .flatMap(s -> s)
                .flatMap(node -> AstHandler.extractVariableDclrFromNode(node).stream())
                .toList();

        return declarations.stream()
                .map(VariableDeclarationExpr::getVariables)
                .flatMap(List::stream)
                .filter(v -> v.getNameAsString().equals(nameExpr.getNameAsString()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Variable name is not the same"))
                .getType();
    }

    /**
     * Returns before fragment as statements.
     *
     * @return list of statements
     */
    public List<Statement> getBeforeStatements() {
        return beforeFragment.stream()
                .filter(Statement.class::isInstance)
                .map(Statement.class::cast)
                .toList();
    }

    /**
     * Returns after fragment as statements.
     *
     * @return list of statements
     */
    public List<Statement> getAfterStatements() {
        return afterFragment.stream()
                .filter(Statement.class::isInstance)
                .map(Statement.class::cast)
                .toList();
    }

    /**
     * Represents a variable that captures the return value of a super call.
     *
     * @param type the type of the return variable
     * @param name the name of the return variable
     */
    public record SuperReturnVar(Type type, SimpleName name) {

        /**
         * Extracts super return variable from a variable declaration expression.
         *
         * @param varDeclExpr the variable declaration expression
         * @return Optional containing SuperReturnVar if valid
         */
        public static Optional<SuperReturnVar> fromVariableDeclaration(VariableDeclarationExpr varDeclExpr) {
            return Optional.of(varDeclExpr)
                    .filter(e -> !e.getVariables().isEmpty())
                    .map(e -> new SuperReturnVar(
                            e.getVariable(0).getType(),
                            e.getVariable(0).getName()));
        }

        /**
         * Extracts super return variable from an assignment expression.
         *
         * @param assignExpr the assignment expression
         * @param splitter   the fragment splitter for type resolution
         * @return Optional containing SuperReturnVar if found
         */
        public static Optional<SuperReturnVar> fromAssignment(AssignExpr assignExpr, FragmentsSplitter splitter) {
            return assignExpr.getChildNodes().stream()
                    .filter(NameExpr.class::isInstance)
                    .map(NameExpr.class::cast)
                    .findFirst()
                    .map(nameExpr -> new SuperReturnVar(
                            splitter.getTypeOfVar(nameExpr),
                            nameExpr.getName()));
        }
    }
}
