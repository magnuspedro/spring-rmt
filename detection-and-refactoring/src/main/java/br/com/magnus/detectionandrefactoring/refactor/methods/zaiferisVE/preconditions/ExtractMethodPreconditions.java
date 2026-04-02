package br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.preconditions;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.FragmentsSplitter;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithCondition;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExtractMethodPreconditions {

    public boolean isValid(MethodDeclaration overriddenMethod, MethodDeclaration m) {
        return this.isValidIgnoringMinSize(overriddenMethod, m) && this.hasMinimumFragmentsSize(m);
    }

    public boolean isValidIgnoringMinSize(MethodDeclaration overriddenMethod, MethodDeclaration m) {
        final var fragmentsSplitter = FragmentsSplitter.splitByMethod(m);

        return fragmentsSplitter.hasSpecificNode()
                && this.superCallIsNotNested(m)
                && this.beforeFragmentThrowsNoException(fragmentsSplitter)
                && this.beforeFragmentHasNoReturn(fragmentsSplitter)
                && !this.hasMultipleVariablesInBeforeFragmentsMethodCalls(fragmentsSplitter)
                && this.methodsValuesMatch(overriddenMethod, m);
    }

    public boolean hasMinimumFragmentsSize(MethodDeclaration method) {
        final var fragmentsSplitter = FragmentsSplitter.splitByMethod(method);
        return this.fragmentsHaveMinSize(fragmentsSplitter);
    }

    private boolean fragmentsHaveMinSize(FragmentsSplitter fragmentsSplitter) {
        return fragmentsSplitter.getBeforeFragment().size() >= 2 || fragmentsSplitter.getAfterFragment().size() >= 2;
    }

    private boolean methodsValuesMatch(MethodDeclaration m1, MethodDeclaration m2) {

        if (m1.getParameters() == null && m2.getParameters() == null) {
            return true;
        } else if ((m1.getParameters() == null && m2.getParameters() != null) || (m1.getParameters() != null && m2.getParameters() == null)) {
            return false;
        } else if (m1.getParameters().size() != m2.getParameters().size()) {
            return false;
        }

        int differentValuesCounter = 0;
        for (int i = 0; i < m1.getParameters().size(); i++) {

            final var v1 = m1.getParameters().get(i).findData(new DataKey<>() {
            }).orElse(null);

            final var v2 = m2.getParameters().get(i).findData(new DataKey<>() {
            }).orElse(null);

            differentValuesCounter += v1 == null && v2 == null ? 0 : (Objects.equals(v1, v2) ? 0 : 1);
        }
        return differentValuesCounter <= 1;
    }

    private boolean hasMultipleVariablesInBeforeFragmentsMethodCalls(FragmentsSplitter fragmentsSplitter) {
        final Collection<VariableDeclarationExpr> variables = fragmentsSplitter.getVariablesOnBeforeFragmentsMethodClass();
        return variables.size() > 1 || (variables.size() == 1 && variables.stream().findFirst().get().getVariables().size() > 1);
    }

    private boolean beforeFragmentHasNoReturn(FragmentsSplitter fragmentsSplitter) {
        return fragmentsSplitter.getBeforeFragment().stream()
                .noneMatch(AstHandler::nodeHasReturnStatement);
    }

    private boolean beforeFragmentThrowsNoException(FragmentsSplitter fragmentsSplitter) {
        return fragmentsSplitter.getBeforeFragment().stream()
                .noneMatch(AstHandler::nodeThrowsException);
    }

    private boolean superCallIsNotNested(MethodDeclaration m) {

        final Optional<BlockStmt> blockStmt = AstHandler.getBlockStatement(m);

        return blockStmt.filter(this::hasSuperAtTopLevelOrInTryOnly).isPresent();
    }

    private boolean hasSuperAtTopLevelOrInTryOnly(BlockStmt blockStmt) {
        return blockStmt.getStatements()
                .stream()
                .anyMatch(this::isAllowedSuperStatement);
    }

    private boolean isAllowedSuperStatement(Statement statement) {
        if (statement instanceof NodeWithCondition) {
            return false;
        }
        return this.hasSuperInAllowedContext(statement);
    }

    private boolean hasSuperInAllowedContext(Node node) {
        if (node == null) {
            return false;
        }

        if (node instanceof NodeWithCondition) {
            return false;
        }

        if (node instanceof TryStmt tryStmt) {
            final var hasSuperInCatch = tryStmt.getCatchClauses().stream()
                    .anyMatch(this::containsSuperAnywhere);
            final var hasSuperInFinally = tryStmt.getFinallyBlock()
                    .map(this::containsSuperAnywhere)
                    .orElse(false);

            if (hasSuperInCatch || hasSuperInFinally) {
                return false;
            }

            return this.hasSuperInAllowedContext(tryStmt.getTryBlock());
        }

        if (AstHandler.getSuperCalls(node).stream().anyMatch(superExpr -> superExpr.equals(node))) {
            return true;
        }

        return node.getChildNodes().stream().anyMatch(this::hasSuperInAllowedContext);
    }

    private boolean containsSuperAnywhere(Node node) {
        return !AstHandler.getSuperCalls(node).isEmpty();

    }

}
