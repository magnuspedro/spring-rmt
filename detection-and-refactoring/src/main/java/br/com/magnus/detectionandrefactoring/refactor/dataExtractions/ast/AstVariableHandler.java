package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.exceptions.*;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.ReturnStmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handler for variable-related AST operations.
 */
public final class AstVariableHandler {

    private AstVariableHandler() {
    }

    /**
     * Gets the simple name from a variable declaration.
     *
     * @param node the variable declaration expression
     * @return optional simple name
     */
    public static Optional<SimpleName> getVariableSimpleName(VariableDeclarationExpr node) {
        return Optional.ofNullable(node)
                .map(VariableDeclarationExpr::getVariables)
                .map(vars -> vars.getFirst()
                        .orElseThrow(VariableDeclarationExpectedException::new))
                .map(VariableDeclarator::getName);
    }

    /**
     * Gets the variable name from a variable declaration.
     *
     * @param var the variable declaration expression
     * @return the simple name
     */
    public static SimpleName getVariableName(VariableDeclarationExpr var) {
        return Optional.ofNullable(var)
                .map(VariableDeclarationExpr::getVariables)
                .map(vars -> vars.getFirst()
                        .orElseThrow(VariableDeclarationExpectedException::new))
                .map(VariableDeclarator::getName)
                .orElseThrow(SimpleNameException::new);
    }

    /**
     * Checks if a variable is used in a node.
     *
     * @param node the node to check
     * @param var  the variable
     * @return true if variable is used
     */
    public static boolean doesNodeUsesVar(Node node, VariableDeclarator var) {
        if (node == null) {
            throw new NullNodeException();
        }
        if (var == null) {
            throw new VariableDeclarationExpectedException();
        }

        if (node instanceof NameExpr nameExpr) {
            return nameExpr.getNameAsString().equals(var.getNameAsString());
        }
        return node.getChildNodes().stream().anyMatch(child -> doesNodeUsesVar(child, var));
    }

    /**
     * Gets all variable declarations from a node.
     *
     * @param node the node
     * @return list of variable declarators
     */
    public static List<VariableDeclarator> getVariableDeclarations(Node node) {
        if (node == null) {
            throw new NullNodeException();
        }

        if (node instanceof VariableDeclarator) {
            return List.of((VariableDeclarator) node);
        }
        return node.getChildNodes().stream()
                .flatMap(child -> getVariableDeclarations(child).stream())
                .toList();
    }

    /**
     * Gets a variable declaration by name from a node.
     *
     * @param node       the node to search
     * @param returnName the variable name
     * @return optional variable declarator
     */
    public static Optional<VariableDeclarator> getVariableDeclarationInNode(Node node, String returnName) {
        if (node == null) {
            throw new NullNodeException();
        }

        if (node instanceof VariableDeclarationExpr varDclrExpr) {
            return varDclrExpr.getVariables().stream()
                    .filter(v -> v.getNameAsString().equals(returnName))
                    .findFirst();
        }

        return node.getChildNodes().stream()
                .map(child -> getVariableDeclarationInNode(child, returnName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    /**
     * Checks if a return statement contains a variable.
     *
     * @param returnStmt the return statement
     * @param var         the variable
     * @return true if variable is used
     */
    public static boolean returnStmtUsesVar(ReturnStmt returnStmt, VariableDeclarator var) {
        return returnStmt.getExpression()
                .filter(expr -> expr instanceof NameExpr)
                .map(NameExpr.class::cast)
                .map(NameExpr::getNameAsString)
                .map(var.getNameAsString()::equals)
                .orElse(false);
    }
}
