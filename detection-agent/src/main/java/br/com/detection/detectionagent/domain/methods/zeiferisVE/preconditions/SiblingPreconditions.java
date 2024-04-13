package br.com.detection.detectionagent.domain.methods.zeiferisVE.preconditions;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.ZafeirisEtAl2016Canditate;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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

    private final AstHandler astHandler;

    public boolean violates(Collection<ZafeirisEtAl2016Canditate> canditadesOfSameOverridenMethod) {
        final List<ZafeirisEtAl2016Canditate.CandidateWithVariables> candidatesWithVariables = canditadesOfSameOverridenMethod.stream()
                .map(ZafeirisEtAl2016Canditate::toCandidateWithVariables)
                .collect(Collectors.toList());

        return !beforeFragmentReturnIsSame(candidatesWithVariables)
                && !this.beforeReturnIsUsedInSuper(candidatesWithVariables)
                && this.isHierarchyShort(candidatesWithVariables);
    }

    private boolean isHierarchyShort(List<ZafeirisEtAl2016Canditate.CandidateWithVariables> candidatesWithVariables) {

        final List<Hierarchy> hierarchies = new ArrayList<>();

        for (ZafeirisEtAl2016Canditate.CandidateWithVariables candidate : candidatesWithVariables) {
            boolean isPartOfAny = false;
            for (Hierarchy hierarchy : hierarchies) {
                if (hierarchy.isPartOf(candidate.candidate().getClassDeclaration())) {
                    isPartOfAny = true;
                    hierarchy.declarations.add(candidate.candidate().getClassDeclaration());
                }
            }

            if (!isPartOfAny) {
                hierarchies.add(new Hierarchy());
                hierarchies.get(0).declarations.add(candidate.candidate().getClassDeclaration());
            }
        }

        return hierarchies.stream().anyMatch(h -> h.declarations.size() < 2);
    }

    private boolean beforeFragmentReturnIsSame(List<ZafeirisEtAl2016Canditate.CandidateWithVariables> candidatesWithVariables) {

        boolean areEqual = true;

        for (int i = 1; i < candidatesWithVariables.size() - 1; i++) {
            if (candidatesWithVariables.get(i).variables().size() > 1) {
                throw new IllegalStateException();
            } else if (candidatesWithVariables.get(i).variables().size() != candidatesWithVariables.get(i + 1).variables().size()) {
                areEqual = false;
            } else if (candidatesWithVariables.get(i).variables().isEmpty()) {
                areEqual = false;
            } else {
                areEqual &= this.astHandler.doVariablesNameMatch(
                        candidatesWithVariables.get(i).variables().stream().findFirst().get(),
                        candidatesWithVariables.get(i + 1).variables().stream().findFirst().get());
            }
        }
        return areEqual;
    }

    private boolean beforeReturnIsUsedInSuper(List<ZafeirisEtAl2016Canditate.CandidateWithVariables> candidatesWithVariables) {
        var isFirstCandidateWithoutVariables = candidatesWithVariables.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No candidates with variables found"))
                .variables()
                .isEmpty();

        if (isFirstCandidateWithoutVariables) {
            return true;
        }

        boolean isUsed = true;
        for (ZafeirisEtAl2016Canditate.CandidateWithVariables candidate : candidatesWithVariables) {
            final VariableDeclarationExpr var = candidate.variables().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No variables found in candidate"));

            final MethodCallExpr methodCall = (MethodCallExpr) candidate
                    .candidate()
                    .getSuperCall()
                    .getParentNode()
                    .orElseThrow(() -> new IllegalArgumentException("No method call found in candidate"));

            isUsed &= this.astHandler.variableIsPresentInMethodCall(var, methodCall);
        }
        return isUsed;
    }

    private class Hierarchy {
        final Set<ClassOrInterfaceDeclaration> declarations = new HashSet<>();

        boolean isPartOf(ClassOrInterfaceDeclaration dclr) {
            return declarations.contains(dclr) && this.isChildOfAny(dclr) && this.isParentOfAny(dclr);
        }

        boolean isChildOfAny(ClassOrInterfaceDeclaration dclr) {
            final Optional<ClassOrInterfaceType> parent = astHandler.getParentType(dclr);
            return parent.filter(classOrInterfaceType -> declarations.stream().map(astHandler::getSimpleName).map(Optional::get)
                    .anyMatch(n -> n.equals(astHandler.getSimpleName(classOrInterfaceType).get()))).isPresent();
        }

        boolean isParentOfAny(ClassOrInterfaceDeclaration dclr) {
            return this.declarations.stream().map(astHandler::getParentType).filter(Optional::isPresent)
                    .map(Optional::get).map(astHandler::getSimpleName).map(Optional::get)
                    .anyMatch(n -> n.equals(astHandler.getSimpleName(dclr).get()));
        }

    }

}
