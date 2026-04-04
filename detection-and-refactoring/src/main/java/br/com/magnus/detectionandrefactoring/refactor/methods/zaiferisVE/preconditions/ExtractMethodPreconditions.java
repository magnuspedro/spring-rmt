package br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.preconditions;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.FragmentsSplitter;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithCondition;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Preconditions for extract method refactoring.
 * <p>
 * Validates that a method meets all requirements for extracting
 * a method to the super class based on Zafeiris et al. 2016.
 */
@Component
@RequiredArgsConstructor
public class ExtractMethodPreconditions {

    /**
     * Minimum number of fragments required for extraction.
     */
    private static final int MIN_FRAGMENTS_SIZE = 2;

    /**
     * Validates if the method can be extracted to super class.
     *
     * @param overriddenMethod the method being overridden
     * @param overridingMethod the overriding method
     * @return true if extraction is valid
     */
    public boolean isValid(MethodDeclaration overriddenMethod, MethodDeclaration overridingMethod) {
        return isValidIgnoringMinSize(overriddenMethod, overridingMethod)
                && hasMinimumFragmentsSize(overridingMethod);
    }

    /**
     * Validates preconditions except for minimum fragment size.
     *
     * @param overriddenMethod the method being overridden
     * @param overridingMethod the overriding method
     * @return true if all other preconditions are met
     */
    public boolean isValidIgnoringMinSize(MethodDeclaration overriddenMethod, MethodDeclaration overridingMethod) {
        var fragmentsSplitter = FragmentsSplitter.splitByMethod(overridingMethod);

        return fragmentsSplitter.hasSpecificNode()
                && superCallIsNotNested(overridingMethod)
                && beforeFragmentThrowsNoException(fragmentsSplitter)
                && beforeFragmentHasNoReturn(fragmentsSplitter)
                && !hasMultipleVariablesInBeforeFragmentsMethodCalls(fragmentsSplitter)
                && superCallArgumentsMatchParameters(overriddenMethod, overridingMethod);
    }

    /**
     * Validates that the method has minimum fragment size for extraction.
     *
     * @param method the method to check
     * @return true if fragments meet minimum size requirement
     */
    public boolean hasMinimumFragmentsSize(MethodDeclaration method) {
        var fragmentsSplitter = FragmentsSplitter.splitByMethod(method);
        return fragmentsHaveMinSize(fragmentsSplitter);
    }

    /**
     * Checks if fragments meet minimum size requirement.
     *
     * @param fragmentsSplitter the fragment splitter
     * @return true if before or after fragment has at least 2 statements
     */
    private boolean fragmentsHaveMinSize(FragmentsSplitter fragmentsSplitter) {
        return fragmentsSplitter.getBeforeFragment().size() >= MIN_FRAGMENTS_SIZE
                || fragmentsSplitter.getAfterFragment().size() >= MIN_FRAGMENTS_SIZE;
    }

    /**
     * Checks if super call appears in an allowed context.
     * <p>
     * Super calls are allowed only at the top level or within try blocks,
     * not in catch/finally blocks or conditional statements.
     *
     * @param method the method to check
     * @return true if super call is in allowed context
     */
    private boolean superCallIsNotNested(MethodDeclaration method) {
        return AstHandler.getBlockStatement(method)
                .filter(this::hasSuperAtTopLevelOrInTryOnly)
                .isPresent();
    }

    /**
     * Checks if super call is at top level or only in try block.
     *
     * @param blockStmt the block statement to check
     * @return true if super is in allowed position
     */
    private boolean hasSuperAtTopLevelOrInTryOnly(BlockStmt blockStmt) {
        return blockStmt.getStatements()
                .stream()
                .anyMatch(this::isAllowedSuperStatement);
    }

    /**
     * Checks if a statement is an allowed super statement.
     * <p>
     * Super is not allowed in conditional contexts (if, while, etc.).
     *
     * @param statement the statement to check
     * @return true if statement is an allowed super statement
     */
    private boolean isAllowedSuperStatement(Node statement) {
        if (statement instanceof NodeWithCondition) {
            return false;
        }
        return hasSuperInAllowedContext(statement);
    }

    /**
     * Checks if super call is in an allowed context within the node tree.
     *
     * @param node the node to inspect
     * @return true if super call is in allowed context
     */
    private boolean hasSuperInAllowedContext(Node node) {
        if (node == null || node instanceof NodeWithCondition) {
            return false;
        }
        if (node instanceof TryStmt tryStmt) {
            return isSuperAllowedInTry(tryStmt);
        }
        return isDirectSuperCall(node) || childrenContainSuperInAllowedContext(node);
    }

    /**
     * Checks if super call is allowed within a try statement.
     * <p>
     * Super is not allowed in catch or finally blocks.
     *
     * @param tryStmt the try statement to check
     * @return true if super is allowed
     */
    private boolean isSuperAllowedInTry(TryStmt tryStmt) {
        if (hasSuperInCatchClauses(tryStmt) || hasSuperInFinally(tryStmt)) {
            return false;
        }
        return hasSuperInAllowedContext(tryStmt.getTryBlock());
    }

    /**
     * Checks if any catch clause contains a super call.
     *
     * @param tryStmt the try statement
     * @return true if super found in catch clauses
     */
    private boolean hasSuperInCatchClauses(TryStmt tryStmt) {
        return tryStmt.getCatchClauses().stream()
                .anyMatch(this::containsSuperAnywhere);
    }

    /**
     * Checks if the finally block contains a super call.
     *
     * @param tryStmt the try statement
     * @return true if super found in finally block
     */
    private boolean hasSuperInFinally(TryStmt tryStmt) {
        return tryStmt.getFinallyBlock()
                .map(this::containsSuperAnywhere)
                .orElse(false);
    }

    /**
     * Checks if the node itself is a direct super call.
     *
     * @param node the node to check
     * @return true if node is a super call
     */
    private boolean isDirectSuperCall(Node node) {
        return AstHandler.getSuperCalls(node).stream()
                .anyMatch(superExpr -> superExpr.equals(node));
    }

    /**
     * Checks if any child node contains a super call in allowed context.
     *
     * @param node the parent node
     * @return true if any child has allowed super call
     */
    private boolean childrenContainSuperInAllowedContext(Node node) {
        return node.getChildNodes().stream()
                .anyMatch(this::hasSuperInAllowedContext);
    }

    /**
     * Checks if the node contains a super call anywhere.
     *
     * @param node the node to check
     * @return true if super call found
     */
    private boolean containsSuperAnywhere(Node node) {
        return !AstHandler.getSuperCalls(node).isEmpty();
    }

    /**
     * Checks if before fragment has no return statements.
     *
     * @param fragmentsSplitter the fragment splitter
     * @return true if no return statements in before fragment
     */
    private boolean beforeFragmentHasNoReturn(FragmentsSplitter fragmentsSplitter) {
        return fragmentsSplitter.getBeforeFragment().stream()
                .noneMatch(AstHandler::nodeHasReturnStatement);
    }

    /**
     * Checks if before fragment throws no exceptions.
     *
     * @param fragmentsSplitter the fragment splitter
     * @return true if no exceptions thrown in before fragment
     */
    private boolean beforeFragmentThrowsNoException(FragmentsSplitter fragmentsSplitter) {
        return fragmentsSplitter.getBeforeFragment().stream()
                .noneMatch(AstHandler::nodeThrowsException);
    }

    /**
     * Checks if before fragment has multiple variables in method calls.
     *
     * @param fragmentsSplitter the fragment splitter
     * @return true if multiple variables found
     */
    private boolean hasMultipleVariablesInBeforeFragmentsMethodCalls(FragmentsSplitter fragmentsSplitter) {
        var variables = fragmentsSplitter.getVariablesOnBeforeFragmentsMethodClass();
        return variables.size() > 1
                || (variables.size() == 1 && variables.stream().findFirst().get().getVariables().size() > 1);
    }

    /**
     * Validates that super call arguments match method parameters.
     * <p>
     * Allows at most one parameter to differ from the super call argument,
     * enabling template method patterns where one parameter may be transformed.
     *
     * @param overriddenMethod  the method being overridden
     * @param overridingMethod the overriding method containing super call
     * @return true if parameters are compatible
     */
    private boolean superCallArgumentsMatchParameters(
            MethodDeclaration overriddenMethod,
            MethodDeclaration overridingMethod) {

        if (parameterCountsDiffer(overriddenMethod, overridingMethod)) {
            return false;
        }

        var superCallArguments = extractSuperCallArguments(overridingMethod);
        if (superCallArguments.isEmpty()) {
            return false;
        }

        return argumentCountMismatch(superCallArguments.get(), overridingMethod.getParameters())
                ? false
                : countMismatchedParameters(superCallArguments.get(), overridingMethod) <= 1;
    }

    /**
     * Checks if the parameter counts differ between methods.
     *
     * @param m1 first method
     * @param m2 second method
     * @return true if parameter counts differ
     */
    private boolean parameterCountsDiffer(MethodDeclaration m1, MethodDeclaration m2) {
        return m1.getParameters().size() != m2.getParameters().size();
    }

    /**
     * Extracts non-super arguments from the super call expression.
     *
     * @param method the method containing super call
     * @return Optional with list of arguments
     */
    private Optional<List<com.github.javaparser.ast.expr.Expression>> extractSuperCallArguments(MethodDeclaration method) {
        return AstHandler.getSuperCalls(method).stream()
                .findFirst()
                .flatMap(SuperExpr::getParentNode)
                .filter(MethodCallExpr.class::isInstance)
                .map(MethodCallExpr.class::cast)
                .map(MethodCallExpr::getArguments)
                .map(args -> args.stream()
                        .filter(arg -> !arg.isSuperExpr())
                        .toList());
    }

    /**
     * Checks if argument count matches parameter count.
     *
     * @param arguments  the argument list
     * @param parameters the parameter list
     * @return true if counts don't match
     */
    private boolean argumentCountMismatch(
            List<com.github.javaparser.ast.expr.Expression> arguments,
            com.github.javaparser.ast.NodeList parameters) {
        return arguments.size() != parameters.size();
    }

    /**
     * Counts how many arguments don't match their corresponding parameter names.
     *
     * @param arguments the argument list
     * @param method    the method with parameters
     * @return count of mismatched parameters
     */
    private long countMismatchedParameters(
            List<com.github.javaparser.ast.expr.Expression> arguments,
            MethodDeclaration method) {

        var parameters = method.getParameters();
        var mismatchCount = 0L;

        for (var i = 0; i < arguments.size(); i++) {
            if (!argumentMatchesParameter(arguments.get(i), parameters.get(i))) {
                mismatchCount++;
            }
        }
        return mismatchCount;
    }

    /**
     * Checks if an argument matches its parameter by name.
     *
     * @param argument  the argument expression
     * @param parameter the parameter
     * @return true if they match
     */
    private boolean argumentMatchesParameter(
            com.github.javaparser.ast.expr.Expression argument,
            com.github.javaparser.ast.body.Parameter parameter) {

        return argument.isNameExpr()
                && parameter.getNameAsString().equals(argument.asNameExpr().getNameAsString());
    }
}
