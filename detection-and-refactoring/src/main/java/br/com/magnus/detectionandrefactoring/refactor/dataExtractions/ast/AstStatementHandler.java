package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handler for statement-related AST operations.
 */
public final class AstStatementHandler {

    private AstStatementHandler() {
    }

    /**
     * Gets the expression statement for a node.
     *
     * @param node the node
     * @return optional expression statement
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
     * Checks if a node contains a return statement.
     *
     * @param node the node to check
     * @return true if return statement found
     */
    public static boolean nodeHasReturnStatement(Node node) {
        return AstNodeHandler.nodeHasClazz(node, ReturnStmt.class);
    }

    /**
     * Checks if a node's return statement uses a variable.
     *
     * @param node the node
     * @return true if return uses variable
     */
    public static boolean returnStmtUsesVariable(Node node, String varName) {
        return Optional.ofNullable(node)
                .filter(ReturnStmt.class::isInstance)
                .map(ReturnStmt.class::cast)
                .flatMap(ReturnStmt::getExpression)
                .filter(expr -> expr instanceof NameExpr)
                .map(expr -> (NameExpr) expr)
                .map(NameExpr::getNameAsString)
                .map(varName::equals)
                .orElse(false);
    }

    /**
     * Gets a statement from an if statement's then block.
     *
     * @param ifStmt the if statement
     * @return optional statement
     */
    public static Optional<Statement> getStatementFromIf(IfStmt ifStmt) {
        if (!ifStmt.hasThenBlock()) {
            return Optional.empty();
        }
        var thenStmt = ifStmt.getThenStmt();
        return thenStmt.getStatements().stream().findFirst();
    }
}
