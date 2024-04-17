package br.com.detection.detectionagent.domain.methods.zeiferisVE.preconditions;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions.SimpleNameException;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.ZafeirisEtAl2016Candidate;
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

    public boolean violates(Collection<ZafeirisEtAl2016Candidate> candidatesOfSameOverriddenMethod) {
        final List<ZafeirisEtAl2016Candidate.CandidateWithVariables> candidatesWithVariables = candidatesOfSameOverriddenMethod.stream()
                .map(ZafeirisEtAl2016Candidate::toCandidateWithVariables)
                .collect(Collectors.toList());

        return !beforeFragmentReturnEqual(candidatesWithVariables)
                && !this.beforeReturnIsUsedInSuper(candidatesWithVariables)
                && this.isAShortHierarchy(candidatesWithVariables);
    }

    private boolean isAShortHierarchy(List<ZafeirisEtAl2016Candidate.CandidateWithVariables> candidatesWithVariables) {

        final var hierarchies = new ArrayList<Hierarchy>();

        for (var candidate : candidatesWithVariables) {
            boolean belongsToHierarchy = false;
            for (var hierarchy : hierarchies) {
                if (hierarchy.belongs(candidate.candidate().getClassDeclaration())) {
                    belongsToHierarchy = true;
                    hierarchy.declarations.add(candidate.candidate().getClassDeclaration());
                }
            }

            if (!belongsToHierarchy) {
                hierarchies.add(new Hierarchy());
                hierarchies.getFirst().declarations.add(candidate.candidate().getClassDeclaration());
            }
        }

        return hierarchies.stream().anyMatch(h -> h.declarations.size() < 2);
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
                areEqual &= this.astHandler.doVariablesNameMatch(
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

            isUsed &= this.astHandler.variableIsPresentInMethodCall(var, methodCall);
        }
        return isUsed;
    }

    private class Hierarchy {
        final Set<ClassOrInterfaceDeclaration> declarations = new HashSet<>();

        boolean belongs(ClassOrInterfaceDeclaration dclr) {
            return declarations.contains(dclr) && this.isChild(dclr) && this.isParent(dclr);
        }

        private boolean isChild(ClassOrInterfaceDeclaration dclr) {
            final Optional<ClassOrInterfaceType> parent = astHandler.getParentType(dclr);
            return parent.filter(classOrInterfaceType -> declarations.stream()
                            .map(astHandler::getSimpleName)
                            .flatMap(Optional::stream)
                            .anyMatch(n -> n.equals(astHandler.getSimpleName(classOrInterfaceType).orElseThrow(SimpleNameException::new))))
                    .isPresent();
        }

        private boolean isParent(ClassOrInterfaceDeclaration dclr) {
            return this.declarations.stream()
                    .map(astHandler::getParentType)
                    .flatMap(Optional::stream)
                    .map(astHandler::getSimpleName)
                    .flatMap(Optional::stream)
                    .anyMatch(n -> n.equals(astHandler.getSimpleName(dclr).orElseThrow(SimpleNameException::new)));
        }

    }

}
