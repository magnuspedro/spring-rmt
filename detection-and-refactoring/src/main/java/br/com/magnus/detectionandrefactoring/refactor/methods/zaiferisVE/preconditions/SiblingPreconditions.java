package br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.preconditions;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.ZafeirisEtAl2016Candidate;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SiblingPreconditions {

    public boolean violates(Collection<ZafeirisEtAl2016Candidate> candidatesOfSameOverriddenMethod) {
        final List<ZafeirisEtAl2016Candidate.CandidateWithVariables> candidatesWithVariables = candidatesOfSameOverriddenMethod.stream()
                .map(ZafeirisEtAl2016Candidate::toCandidateWithVariables)
                .collect(Collectors.toList());
        if (candidatesWithVariables.size() < 2) {
            return false;
        }

        return !beforeFragmentReturnEqual(candidatesWithVariables)
                || !this.beforeReturnIsUsedInSuper(candidatesWithVariables)
                || this.isAShortHierarchy(candidatesWithVariables);
    }

    private boolean isAShortHierarchy(List<ZafeirisEtAl2016Candidate.CandidateWithVariables> candidatesWithVariables) {
        final var byParent = candidatesWithVariables.stream()
                .map(c -> c.candidate().getClassDeclaration())
                .filter(Objects::nonNull)
                .map(AstHandler::getParentType)
                .flatMap(Optional::stream)
                .collect(Collectors.groupingBy(ClassOrInterfaceType::asString, Collectors.counting()));

        if (byParent.isEmpty()) {
            return false;
        }

        return byParent.values().stream().anyMatch(size -> size < 2);
    }

    private boolean beforeFragmentReturnEqual(List<ZafeirisEtAl2016Candidate.CandidateWithVariables> candidatesWithVariables) {

        boolean areEqual = true;

        for (int i = 1; i < candidatesWithVariables.size() - 1; i++) {
            if (candidatesWithVariables.get(i).variables().size() > 1) {
                throw new IllegalStateException("Candidate with multiple variables found");
            } else if (candidatesWithVariables.get(i).variables().size() != candidatesWithVariables.get(i + 1).variables().size()) {
                areEqual = false;
            } else if (candidatesWithVariables.get(i).variables().isEmpty()) {
                areEqual = false;
            } else {
                areEqual &= AstHandler.doVariablesNameMatch(
                        candidatesWithVariables.get(i).variables()
                                .stream()
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("No variables found")),
                        candidatesWithVariables.get(i + 1).variables()
                                .stream()
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("No variables found"))
                );
            }
        }
        return areEqual;
    }

    private boolean beforeReturnIsUsedInSuper(List<ZafeirisEtAl2016Candidate.CandidateWithVariables> candidatesWithVariables) {
        var isFirstCandidateWithoutVariables = candidatesWithVariables.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No candidates with variables found"))
                .variables()
                .isEmpty();

        if (isFirstCandidateWithoutVariables) {
            return true;
        }

        boolean isUsed = true;
        for (ZafeirisEtAl2016Candidate.CandidateWithVariables candidate : candidatesWithVariables) {
            final VariableDeclarationExpr var = candidate.variables().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No variables found in candidate"));

            final MethodCallExpr methodCall = (MethodCallExpr) candidate
                    .candidate()
                    .getSuperCall()
                    .getParentNode()
                    .orElseThrow(() -> new IllegalArgumentException("No method call found in candidate"));

            isUsed &= AstHandler.variableIsPresentInMethodCall(var, methodCall);
        }
        return isUsed;
    }

}
