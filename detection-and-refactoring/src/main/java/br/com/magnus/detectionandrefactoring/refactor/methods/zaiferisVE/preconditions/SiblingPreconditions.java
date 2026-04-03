package br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.preconditions;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.FragmentsSplitter;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.ZafeirisEtAl2016Candidate;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
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
        return this.violates(candidatesOfSameOverriddenMethod, List.of());
    }

    public boolean violates(Collection<ZafeirisEtAl2016Candidate> candidatesOfSameOverriddenMethod,
                            Collection<JavaFile> javaFiles) {
        final List<ZafeirisEtAl2016Candidate.CandidateWithVariables> candidatesWithVariables = candidatesOfSameOverriddenMethod.stream()
                .map(ZafeirisEtAl2016Candidate::toCandidateWithVariables)
                .collect(Collectors.toList());
        if (candidatesWithVariables.size() < 2) {
            return false;
        }

        if (this.allCandidatesHaveTrivialFragments(candidatesOfSameOverriddenMethod)) {
            return true;
        }

        return !beforeFragmentReturnEqual(candidatesWithVariables)
                || !this.beforeReturnIsUsedInSuper(candidatesWithVariables)
                || this.hasFurtherOverrideInHierarchy(candidatesOfSameOverriddenMethod, javaFiles)
                || this.isAShortHierarchy(candidatesWithVariables);
    }

    private boolean allCandidatesHaveTrivialFragments(Collection<ZafeirisEtAl2016Candidate> candidates) {
        return candidates.stream()
                .map(ZafeirisEtAl2016Candidate::getOverridingMethod)
                .map(FragmentsSplitter::splitByMethod)
                .allMatch(fragments -> fragments.getBeforeFragment().size() < 2
                        && fragments.getAfterFragment().size() < 2);
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

        for (int i = 0; i < candidatesWithVariables.size() - 1; i++) {
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

    private boolean hasFurtherOverrideInHierarchy(Collection<ZafeirisEtAl2016Candidate> candidates,
                                                  Collection<JavaFile> javaFiles) {
        if (this.hasFurtherOverrideInCandidateHierarchy(candidates)) {
            return true;
        }
        if (javaFiles == null || javaFiles.isEmpty() || candidates.isEmpty()) {
            return false;
        }

        final var overriddenMethod = candidates.stream()
                .findFirst()
                .map(ZafeirisEtAl2016Candidate::getOverriddenMethod)
                .orElse(null);
        if (overriddenMethod == null) {
            return false;
        }

        final var classNames = candidates.stream()
                .map(ZafeirisEtAl2016Candidate::getClassDeclaration)
                .filter(Objects::nonNull)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .collect(Collectors.toSet());

        final Map<String, ClassOrInterfaceDeclaration> classesByName = javaFiles.stream()
                .map(JavaFile::getCompilationUnit)
                .map(AstHandler::getClassOrInterfaceDeclaration)
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(
                        ClassOrInterfaceDeclaration::getNameAsString,
                        classDcl -> classDcl,
                        (left, right) -> left
                ));

        return classesByName.entrySet().stream()
                .filter(entry -> !classNames.contains(entry.getKey()))
                .filter(entry -> this.extendsAnyCandidate(entry.getValue(), classNames, classesByName, new HashSet<>()))
                .map(Map.Entry::getValue)
                .anyMatch(classDcl -> this.hasMethodOverride(classDcl, overriddenMethod));
    }

    private boolean hasFurtherOverrideInCandidateHierarchy(Collection<ZafeirisEtAl2016Candidate> candidates) {
        final var classNames = candidates.stream()
                .map(ZafeirisEtAl2016Candidate::getClassDeclaration)
                .filter(Objects::nonNull)
                .map(classDeclaration -> classDeclaration.getNameAsString())
                .collect(Collectors.toSet());

        return candidates.stream()
                .map(ZafeirisEtAl2016Candidate::getClassDeclaration)
                .filter(Objects::nonNull)
                .flatMap(classDeclaration -> classDeclaration.getExtendedTypes().stream())
                .map(ClassOrInterfaceType::asString)
                .anyMatch(classNames::contains);
    }

    private boolean extendsAnyCandidate(ClassOrInterfaceDeclaration classDcl,
                                        Set<String> candidateClassNames,
                                        Map<String, ClassOrInterfaceDeclaration> classesByName,
                                        Set<String> visiting) {
        final var className = classDcl.getNameAsString();
        if (!visiting.add(className)) {
            return false;
        }

        final var extendedTypes = classDcl.getExtendedTypes().stream()
                .map(ClassOrInterfaceType::asString)
                .toList();

        for (var extendedType : extendedTypes) {
            if (candidateClassNames.contains(extendedType)) {
                return true;
            }
            final var parentClass = classesByName.get(extendedType);
            if (parentClass != null && this.extendsAnyCandidate(parentClass, candidateClassNames, classesByName, visiting)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMethodOverride(ClassOrInterfaceDeclaration classDcl, MethodDeclaration overriddenMethod) {
        return AstHandler.getMethods(classDcl).stream()
                .filter(method -> method.getNameAsString().equals(overriddenMethod.getNameAsString()))
                .anyMatch(method -> AstHandler.methodsParamsMatch(method, overriddenMethod));
    }
}
