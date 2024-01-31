package br.com.detection.detectionagent.domain.methods.zeiferisVE.preconditions;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.FragmentsSplitter;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExtractMethodPreconditions {

    private final AstHandler astHandler;

    public boolean isValid(MethodDeclaration overriddenMethod, MethodDeclaration m, SuperExpr superCall) {
        final FragmentsSplitter fragmentsSplitter = new FragmentsSplitter(m, superCall);

        return fragmentsSplitter.hasSpecificNode()
                && this.superCallIsNotNested(m, superCall)
                && this.beforeFragmentThrowsNoException(fragmentsSplitter)
                && this.beforeFragmentHasNoReturn(fragmentsSplitter)
                && !this.hasMultipleVariablesInBeforeFragmentsMethodCalls(fragmentsSplitter)
                && this.methodsValuesMatch(overriddenMethod, m)
                && this.fragmentsHaveMinSize(fragmentsSplitter);
    }

    private boolean fragmentsHaveMinSize(FragmentsSplitter fragmentsSplitter) {
        return fragmentsSplitter.getBeforeFragment().size() > 2 || fragmentsSplitter.getAfterFragment().size() > 2;
    }

    private boolean methodsValuesMatch(MethodDeclaration m1, MethodDeclaration m2) {

        if (m1.getParameters() == null && m2.getParameters() == null) {
            return true;
        } else if ((m1.getParameters() == null && m2.getParameters() != null) || (m1.getParameters() != null && m2.getParameters() == null)) {
            return false;
        }

        int differentValuesCounter = 0;
        for (int i = 0; i < m1.getParameters().size(); i++) {

            final Object v1 = m1.getParameters().get(i).getData(new DataKey<Object>() {
            });

            final Object v2 = m2.getParameters().get(i).getData(new DataKey<Object>() {
            });

            differentValuesCounter += v1 == null && v2 == null ? 0 : (v1.equals(v2) ? 0 : 1);
        }
        return differentValuesCounter <= 1;
    }

    private boolean hasMultipleVariablesInBeforeFragmentsMethodCalls(FragmentsSplitter fragmentsSplitter) {
        final Collection<VariableDeclarationExpr> variables = fragmentsSplitter.getVariablesOnBeforeFragmentsMethodCalss();
        return variables.size() > 1 || (variables.size() == 1 && variables.stream().findFirst().get().getVariables().size() > 1);
    }

    private boolean beforeFragmentHasNoReturn(FragmentsSplitter fragmentsSplitter) {
        return fragmentsSplitter.getBeforeFragment().stream()
                .noneMatch(this.astHandler::nodeHasReturnStatement);
    }

    private boolean beforeFragmentThrowsNoException(FragmentsSplitter fragmentsSplitter) {
        return fragmentsSplitter.getBeforeFragment().stream()
                .noneMatch(this.astHandler::nodeThrowsException);
    }

    private boolean superCallIsNotNested(MethodDeclaration m, SuperExpr superCall) {

        final Optional<BlockStmt> blockStmt = this.astHandler.getBlockStatement(m);

        return blockStmt.filter(stmt -> this.astHandler.childHasDirectSuperCall(stmt, superCall)).isPresent();

    }

}
