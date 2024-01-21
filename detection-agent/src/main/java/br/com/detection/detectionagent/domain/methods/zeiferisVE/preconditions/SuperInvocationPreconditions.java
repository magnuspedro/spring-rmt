package br.com.detection.detectionagent.domain.methods.zeiferisVE.preconditions;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.AstHandler;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class SuperInvocationPreconditions {
    private static final String GET = "get";
    private static final String SET = "set";
    private static final Set<String> invalidMethodNames = Stream.of("toString", "equals", "hashCode", "clone", "finalize", "compareTo").collect(Collectors.toSet());

    private final AstHandler astParser;

    public boolean violatesAmountOfSuperCallsOrName(MethodDeclaration method, Collection<SuperExpr> superCalls) {

        final String name = this.astParser.getSimpleName(method).map(SimpleName::asString).orElse("");

        return superCalls.size() != 1 || invalidMethodNames.contains(name) && (name.startsWith(GET) || name.startsWith(SET));
    }

    public boolean isOverriddenMethodValid(MethodDeclaration overridenMethod, MethodDeclaration method) {

        final Optional<BlockStmt> blk = overridenMethod.getChildNodes().stream().filter(BlockStmt.class::isInstance)
                .map(BlockStmt.class::cast).findFirst();

        return blk.isPresent() && blk.get().getChildNodes().size() > 1
                && this.isOverridenMethodLessAccesible(overridenMethod, method);
    }

    private boolean isOverridenMethodLessAccesible(MethodDeclaration overridenMethod, MethodDeclaration method) {
        if (overridenMethod.getModifiers().contains(Modifier.PUBLIC)) {
            return true;
        } else if (overridenMethod.getModifiers().contains(Modifier.PROTECTED)) {
            return method.getModifiers().stream()
                    .anyMatch(m -> m.equals(Modifier.PROTECTED) || m.equals(Modifier.PRIVATE));
        } else if (overridenMethod.getModifiers().contains(Modifier.PRIVATE)) {
            return method.getModifiers().stream().anyMatch(m -> m.equals(Modifier.PRIVATE));
        }
        throw new IllegalStateException();
    }

}
