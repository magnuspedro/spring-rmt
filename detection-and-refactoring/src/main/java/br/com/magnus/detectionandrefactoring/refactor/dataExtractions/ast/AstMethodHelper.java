package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.exceptions.*;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;

import java.util.Optional;

/**
 * Utility class for static AST helper methods.
 * <p>
 * Provides generic helper operations used across multiple AST handlers.
 */
public final class AstMethodHelper {

    private AstMethodHelper() {
    }

    /**
     * Checks if parameter position is out of bounds.
     *
     * @param position the position to check
     * @param list     the node list
     * @return true if position is out of bounds
     */
    public static boolean isPositionOutOfBounds(int position, NodeList<?> list) {
        return (list.size() - 1) < position;
    }

    /**
     * Checks if two method calls match by name and arguments.
     *
     * @param mc1 first method call
     * @param mc2 second method call
     * @return true if method calls match
     * @throws NullMethodException if either method call is null
     */
    public static boolean doesMethodCallsMatch(MethodCallExpr mc1, MethodCallExpr mc2) {
        if (mc1 == null || mc2 == null) {
            throw new NullMethodException();
        }

        if (!mc1.getName().equals(mc2.getName())) {
            return false;
        }

        for (var i = 0; i < mc1.getArguments().size(); i++) {
            if (isPositionOutOfBounds(i, mc2.getArguments())
                    || !mc1.getArguments().get(i).equals(mc2.getArguments().get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if two variable declarations have matching simple names.
     *
     * @param var1 first variable declaration
     * @param var2 second variable declaration
     * @return true if names match
     */
    public static boolean doVariableNamesMatch(VariableDeclarationExpr var1, VariableDeclarationExpr var2) {
        return AstVariableHandler.getVariableSimpleName(var1)
                .equals(AstVariableHandler.getVariableSimpleName(var2));
    }

    /**
     * Checks if a return statement uses a specific variable.
     *
     * @param returnStmt the return statement
     * @param varName    the variable name
     * @return true if return statement uses the variable
     */
    public static boolean returnStmtUsesVariable(ReturnStmt returnStmt, String varName) {
        return returnStmt.getExpression()
                .filter(expr -> expr instanceof NameExpr)
                .map(NameExpr.class::cast)
                .map(NameExpr::getNameAsString)
                .map(varName::equals)
                .orElse(false);
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
}
