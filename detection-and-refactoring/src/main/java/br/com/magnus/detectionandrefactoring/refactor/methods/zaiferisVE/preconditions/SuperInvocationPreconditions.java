package br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.preconditions;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SuperInvocationPreconditions {
    private static final String GET = "get";
    private static final String SET = "set";
    private static final Set<String> invalidMethodNames = Set.of("toString", "equals", "hashCode", "clone", "finalize", "compareTo");

    public boolean violatesAmountOfSuperCallsOrName(MethodDeclaration method, Collection<SuperExpr> superCalls) {

        final var name = AstHandler.getSimpleName(method)
                .map(SimpleName::asString)
                .orElse("");

        return superCalls.size() != 1 || invalidMethodNames.contains(name) && (name.startsWith(GET) || name.startsWith(SET));
    }

    public boolean isOverriddenMethodValid(MethodDeclaration overriddenMethod, MethodDeclaration method) {

        final var blk = overriddenMethod.getChildNodes()
                .stream()
                .filter(BlockStmt.class::isInstance)
                .map(BlockStmt.class::cast)
                .findFirst();

        return blk.isPresent() && blk.get().getChildNodes().size() > 1
                && this.isOverriddenMethodLessAccessible(overriddenMethod, method);
    }

    private boolean isOverriddenMethodLessAccessible(MethodDeclaration overriddenMethod, MethodDeclaration method) {
        if (overriddenMethod.getModifiers().contains(Modifier.publicModifier())) {
            return true;
        } else if (overriddenMethod.getModifiers().contains(Modifier.protectedModifier())) {
            return method.getModifiers().stream()
                    .anyMatch(m -> m.equals(Modifier.protectedModifier()) || m.equals(Modifier.privateModifier()));
        } else if (overriddenMethod.getModifiers().contains(Modifier.privateModifier())) {
            return method.getModifiers().stream().anyMatch(m -> m.equals(Modifier.privateModifier()));
        }
        throw new IllegalStateException();
    }
}
