package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithCondition;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.TryStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for super call-related AST operations.
 */
public final class AstSuperCallHandler {

    private AstSuperCallHandler() {
    }

    /**
     * Gets all super calls from a node tree.
     *
     * @param node the root node
     * @return list of super expressions
     */
    public static List<SuperExpr> getSuperCalls(Node node) {
        var superCalls = new ArrayList<SuperExpr>();

        if (node == null) {
            return superCalls;
        }

        if (node instanceof SuperExpr) {
            superCalls.add((SuperExpr) node);
        }

        if (node.getChildNodes() == null || node.getChildNodes().isEmpty()) {
            return superCalls;
        }

        superCalls.addAll(node.getChildNodes()
                .stream()
                .flatMap(cn -> getSuperCalls(cn).stream())
                .toList());

        return superCalls;
    }

    /**
     * Checks if a node has a direct super call.
     *
     * @param node the node to check
     * @return true if node contains or is a super call
     */
    public static boolean hasDirectSuperCall(Node node) {
        if (node == null) {
            return false;
        }
        if (node instanceof NodeWithCondition) {
            return false;
        }
        if (node instanceof SuperExpr) {
            return true;
        }
        if (node instanceof TryStmt tryStmt) {
            return hasSuperInTryBlock(tryStmt);
        }
        return node.getChildNodes().stream().anyMatch(AstSuperCallHandler::hasDirectSuperCall);
    }

    /**
     * Checks if a try block has a super call.
     *
     * @param tryStmt the try statement
     * @return true if super is in try block
     */
    private static boolean hasSuperInTryBlock(TryStmt tryStmt) {
        var hasSuperInCatch = tryStmt.getCatchClauses().stream()
                .anyMatch(AstSuperCallHandler::hasSuperInCatchClause);
        var hasSuperInFinally = tryStmt.getFinallyBlock()
                .map(AstSuperCallHandler::hasSuperInFinally)
                .orElse(false);

        return !hasSuperInCatch && !hasSuperInFinally
                && tryStmt.getTryBlock() != null
                && hasDirectSuperCall(tryStmt.getTryBlock());
    }

    /**
     * Checks if a catch clause has a super call.
     *
     * @param catchClause the catch clause
     * @return true if super is in catch clause
     */
    private static boolean hasSuperInCatchClause(CatchClause catchClause) {
        return !getSuperCalls(catchClause).isEmpty();
    }

    /**
     * Checks if a finally block has a super call.
     *
     * @param finallyBlock the finally block
     * @return true if super is in finally block
     */
    private static boolean hasSuperInFinally(BlockStmt finallyBlock) {
        return finallyBlock != null && !getSuperCalls(finallyBlock).isEmpty();
    }

    /**
     * Checks if a child node has a direct super call.
     *
     * @param node the node
     * @return true if direct super call found
     */
    public static boolean childHasDirectSuperCall(Node node) {
        if (node == null) {
            return false;
        }
        if (node instanceof NodeWithCondition || node instanceof CatchClause) {
            return false;
        }
        if (node instanceof TryStmt tryStmt) {
            var hasSuperInCatch = tryStmt.getCatchClauses().stream()
                    .anyMatch(AstSuperCallHandler::hasSuperInCatchClause);
            var hasSuperInFinally = tryStmt.getFinallyBlock()
                    .map(AstSuperCallHandler::hasSuperInFinally)
                    .orElse(false);

            if (hasSuperInCatch || hasSuperInFinally) {
                return false;
            }
            return hasDirectSuperCall(tryStmt.getTryBlock());
        }
        if (node instanceof MethodCallExpr methodCallExpr && methodCallExpr.getArguments() != null) {
            return methodCallExpr.getArguments().stream()
                    .anyMatch(AstSuperCallHandler::hasDirectSuperCall);
        }
        if (node.getChildNodes() == null || node.getChildNodes().isEmpty()) {
            return false;
        }
        if (node instanceof SuperExpr || node.getChildNodes().stream().anyMatch(c -> c instanceof SuperExpr)) {
            return true;
        }
        return node.getChildNodes().stream().anyMatch(AstSuperCallHandler::hasDirectSuperCall);
    }
}
