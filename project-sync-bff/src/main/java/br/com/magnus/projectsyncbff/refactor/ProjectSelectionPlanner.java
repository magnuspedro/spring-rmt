package br.com.magnus.projectsyncbff.refactor;

import br.com.magnus.config.starter.configuration.JavaParserSingleton;
import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.file.extractor.FileExtractor;
import br.com.magnus.config.starter.projects.BaseProject;
import br.com.magnus.config.starter.projects.CandidateInformation;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectSelectionPlanner {

    private final FileExtractor fileExtractor;

    public ProjectSelection plan(BaseProject project, Collection<String> requestedCandidateIds) {
        var candidates = Optional.ofNullable(project.getCandidatesInformation()).orElseGet(List::of);
        if (candidates.isEmpty()) {
            return ProjectSelection.builder()
                    .candidates(List.of())
                    .requestedCandidateIds(List.of())
                    .selectedCandidateIds(List.of())
                    .autoSelectedCount(0)
                    .blockedCount(0)
                    .downloadable(false)
                    .build();
        }

        var graph = buildGraph(project, candidates);
        var candidateIds = candidates.stream()
                .map(CandidateInformation::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var requested = Optional.ofNullable(requestedCandidateIds)
                .orElseGet(List::of)
                .stream()
                .filter(candidateIds::contains)
                .filter(id -> !graph.blockedReasons().containsKey(id))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var selected = expand(requested, graph.requiredCandidateIds());

        var candidateSelections = candidates.stream()
                .map(candidate -> CandidateSelection.builder()
                        .candidate(candidate)
                        .requiredCandidateIds(graph.requiredCandidateIds().getOrDefault(candidate.getId(), Set.of()))
                        .dependencyReasons(graph.dependencyReasons().getOrDefault(candidate.getId(), List.of()))
                        .blockingReasons(graph.blockedReasons().getOrDefault(candidate.getId(), List.of()))
                        .requested(requested.contains(candidate.getId()))
                        .selected(selected.contains(candidate.getId()))
                        .locked(selected.contains(candidate.getId()) && !requested.contains(candidate.getId()))
                        .blocked(graph.blockedReasons().containsKey(candidate.getId()))
                        .build())
                .toList();

        return ProjectSelection.builder()
                .candidates(candidateSelections)
                .requestedCandidateIds(List.copyOf(requested))
                .selectedCandidateIds(List.copyOf(selected))
                .autoSelectedCount(Math.max(0, selected.size() - requested.size()))
                .blockedCount(graph.blockedReasons().size())
                .downloadable(!selected.isEmpty())
                .build();
    }

    public void validateSelection(BaseProject project, Collection<String> selectedCandidateIds) {
        var candidates = Optional.ofNullable(project.getCandidatesInformation()).orElseGet(List::of);
        var graph = buildGraph(project, candidates);
        var availableIds = candidates.stream()
                .map(CandidateInformation::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var selected = Optional.ofNullable(selectedCandidateIds)
                .orElseGet(List::of)
                .stream()
                .filter(availableIds::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (selected.isEmpty()) {
            throw new SelectionValidationException("Select at least one candidate before downloading the refactored project.", List.of());
        }

        var blockedSelections = selected.stream()
                .filter(graph.blockedReasons()::containsKey)
                .toList();
        if (!blockedSelections.isEmpty()) {
            throw new SelectionValidationException("One or more selected candidates are not allowed because dependent files are not also refactorable.",
                    blockedSelections);
        }

        var requiredClosure = expand(selected, graph.requiredCandidateIds());
        if (!requiredClosure.equals(selected)) {
            var missing = requiredClosure.stream()
                    .filter(id -> !selected.contains(id))
                    .toList();
            throw new SelectionValidationException("Some dependent candidates are missing from the selection.", missing);
        }
    }

    private DependencyGraph buildGraph(BaseProject project, List<CandidateInformation> candidates) {
        var sourceFiles = buildSourceFiles(project);
        var sourceFilesByPath = sourceFiles.stream()
                .collect(Collectors.toMap(SourceFile::fullName, file -> file, (left, right) -> left, LinkedHashMap::new));
        var candidateById = candidates.stream()
                .collect(Collectors.toMap(CandidateInformation::getId, candidate -> candidate, (left, right) -> left, LinkedHashMap::new));
        var candidateIdsByFile = new LinkedHashMap<String, Set<String>>();
        candidates.forEach(candidate -> filesChanged(candidate)
                .forEach(file -> candidateIdsByFile.computeIfAbsent(file, ignored -> new LinkedHashSet<>()).add(candidate.getId())));

        var directDependencies = new LinkedHashMap<String, Set<String>>();
        var dependencyReasons = new LinkedHashMap<String, Set<String>>();
        var blockedReasons = new LinkedHashMap<String, Set<String>>();

        candidates.forEach(candidate -> {
            directDependencies.put(candidate.getId(), new LinkedHashSet<>());
            dependencyReasons.put(candidate.getId(), new LinkedHashSet<>());
        });

        candidates.forEach(candidate -> {
            var candidateId = candidate.getId();
            filesChanged(candidate).forEach(file -> {
                var overlapping = candidateIdsByFile.getOrDefault(file, Set.of()).stream()
                        .filter(otherId -> !otherId.equals(candidateId))
                        .toList();
                overlapping.forEach(otherId -> {
                    directDependencies.get(candidateId).add(otherId);
                    dependencyReasons.get(candidateId).add("Also includes " + file + " because multiple candidates change it.");
                });
            });

            var changedTypes = filesChanged(candidate).stream()
                    .map(sourceFilesByPath::get)
                    .filter(Objects::nonNull)
                    .flatMap(file -> file.declaredTypes().stream())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (changedTypes.isEmpty()) {
                return;
            }

            sourceFiles.forEach(sourceFile -> {
                if (!Collections.disjoint(filesChanged(candidate), Set.of(sourceFile.fullName()))) {
                    return;
                }
                var referencedTypes = sourceFile.referencedTypes();
                if (Collections.disjoint(changedTypes, referencedTypes)) {
                    return;
                }

                var matchingTypes = referencedTypes.stream()
                        .filter(changedTypes::contains)
                        .map(ProjectSelectionPlanner::getSimpleTypeName)
                        .collect(Collectors.toCollection(TreeSet::new));
                var dependentCandidates = candidateIdsByFile.getOrDefault(sourceFile.fullName(), Set.of()).stream()
                        .filter(otherId -> !otherId.equals(candidateId))
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                if (dependentCandidates.isEmpty()) {
                    blockedReasons.computeIfAbsent(candidateId, ignored -> new LinkedHashSet<>())
                            .add(sourceFile.fullName() + " depends on " + String.join(", ", matchingTypes) + " but is not part of any refactoring candidate.");
                    return;
                }

                dependentCandidates.forEach(otherId -> {
                    directDependencies.get(candidateId).add(otherId);
                    dependencyReasons.get(candidateId).add(sourceFile.fullName() + " also needs refactoring because it references " + String.join(", ", matchingTypes) + ".");
                });
            });
        });

        var propagatedBlockedReasons = propagateBlockedCandidates(candidateById, directDependencies, blockedReasons);

        return new DependencyGraph(
                directDependencies.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> Set.copyOf(entry.getValue()), (left, right) -> left, LinkedHashMap::new)),
                dependencyReasons.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> List.copyOf(entry.getValue()), (left, right) -> left, LinkedHashMap::new)),
                propagatedBlockedReasons.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> List.copyOf(entry.getValue()), (left, right) -> left, LinkedHashMap::new))
        );
    }

    private Map<String, Set<String>> propagateBlockedCandidates(Map<String, CandidateInformation> candidateById,
                                                                Map<String, Set<String>> directDependencies,
                                                                Map<String, Set<String>> initialBlockedReasons) {
        var blockedReasons = new LinkedHashMap<String, Set<String>>();
        initialBlockedReasons.forEach((candidateId, reasons) -> blockedReasons.put(candidateId, new LinkedHashSet<>(reasons)));

        var changed = true;
        while (changed) {
            changed = false;
            for (var entry : directDependencies.entrySet()) {
                var candidateId = entry.getKey();
                var requiredIds = entry.getValue();
                var reasons = blockedReasons.computeIfAbsent(candidateId, ignored -> new LinkedHashSet<>());
                var sizeBefore = reasons.size();
                requiredIds.stream()
                        .filter(blockedReasons::containsKey)
                        .forEach(requiredId -> reasons.add("Requires " + describeCandidate(candidateById.get(requiredId)) + ", which is not selectable."));
                if (reasons.isEmpty()) {
                    blockedReasons.remove(candidateId);
                } else if (reasons.size() != sizeBefore) {
                    changed = true;
                }
            }
        }
        return blockedReasons;
    }

    private List<SourceFile> buildSourceFiles(BaseProject project) {
        final List<JavaFile> javaFiles;
        try {
            javaFiles = fileExtractor.extract(project);
        } catch (Exception exception) {
            log.warn("Skipping dependency analysis for project {} because the original sources could not be extracted", project.getId(), exception);
            return List.of();
        }
        if (CollectionUtils.isEmpty(javaFiles)) {
            return List.of();
        }

        var rawFiles = javaFiles.stream()
                .map(javaFile -> {
                    var compilationUnit = JavaParserSingleton.getInstance()
                            .parse(javaFile.getOriginalClass())
                            .getResult()
                            .orElse(null);
                    if (compilationUnit == null) {
                        return null;
                    }
                    var declaredTypes = compilationUnit.findAll(TypeDeclaration.class).stream()
                            .map(TypeDeclaration::getNameAsString)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    var packageName = compilationUnit.getPackageDeclaration()
                            .map(packageDeclaration -> packageDeclaration.getNameAsString())
                            .orElse("");
                    var explicitImports = compilationUnit.getImports().stream()
                            .filter(importDeclaration -> !importDeclaration.isAsterisk())
                            .collect(Collectors.toMap(ProjectSelectionPlanner::getImportedSimpleName, ImportDeclaration::getNameAsString,
                                    (left, right) -> left, LinkedHashMap::new));
                    var rawTypeReferences = compilationUnit.findAll(ClassOrInterfaceType.class).stream()
                            .map(ClassOrInterfaceType::getNameAsString)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    compilationUnit.findAll(ObjectCreationExpr.class).stream()
                            .map(objectCreationExpr -> objectCreationExpr.getType().getNameAsString())
                            .forEach(rawTypeReferences::add);
                    return RawSourceFile.builder()
                            .fullName(javaFile.getFullName())
                            .packageName(packageName)
                            .declaredTypes(declaredTypes)
                            .explicitImports(explicitImports)
                            .rawTypeReferences(rawTypeReferences)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        var knownTypesBySimpleName = new HashMap<String, Set<String>>();
        rawFiles.forEach(file -> file.declaredTypes().forEach(type -> {
            var fqcn = toQualifiedName(file.packageName(), type);
            knownTypesBySimpleName.computeIfAbsent(type, ignored -> new LinkedHashSet<>()).add(fqcn);
        }));

        return rawFiles.stream()
                .map(file -> SourceFile.builder()
                        .fullName(file.fullName())
                        .declaredTypes(file.declaredTypes().stream()
                                .map(type -> toQualifiedName(file.packageName(), type))
                                .collect(Collectors.toCollection(LinkedHashSet::new)))
                        .referencedTypes(file.rawTypeReferences().stream()
                                .map(type -> resolveType(type, file, knownTypesBySimpleName))
                                .flatMap(Optional::stream)
                                .collect(Collectors.toCollection(LinkedHashSet::new)))
                        .build())
                .toList();
    }

    private static Optional<String> resolveType(String simpleType,
                                                RawSourceFile file,
                                                Map<String, Set<String>> knownTypesBySimpleName) {
        if (file.explicitImports().containsKey(simpleType)) {
            return Optional.of(file.explicitImports().get(simpleType));
        }

        var packageLocal = toQualifiedName(file.packageName(), simpleType);
        if (knownTypesBySimpleName.getOrDefault(simpleType, Set.of()).contains(packageLocal)) {
            return Optional.of(packageLocal);
        }

        var knownTypes = knownTypesBySimpleName.getOrDefault(simpleType, Set.of());
        if (knownTypes.size() == 1) {
            return knownTypes.stream().findFirst();
        }

        return Optional.empty();
    }

    private static String toQualifiedName(String packageName, String simpleName) {
        if (packageName == null || packageName.isBlank()) {
            return simpleName;
        }
        return packageName + "." + simpleName;
    }

    private static String getImportedSimpleName(ImportDeclaration importDeclaration) {
        return importDeclaration.getName().getIdentifier();
    }

    private static String getSimpleTypeName(String qualifiedName) {
        var lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot < 0) {
            return qualifiedName;
        }
        return qualifiedName.substring(lastDot + 1);
    }

    private static LinkedHashSet<String> expand(Collection<String> requestedIds, Map<String, Set<String>> directDependencies) {
        var expanded = new LinkedHashSet<String>();
        var queue = new ArrayDeque<>(requestedIds);
        while (!queue.isEmpty()) {
            var candidateId = queue.removeFirst();
            if (!expanded.add(candidateId)) {
                continue;
            }
            directDependencies.getOrDefault(candidateId, Set.of()).forEach(queue::addLast);
        }
        return expanded;
    }

    private static String describeCandidate(CandidateInformation candidate) {
        if (candidate == null) {
            return "another candidate";
        }
        return candidate.getDesignPattern() + " candidate " + candidate.getId();
    }

    private static Set<String> filesChanged(CandidateInformation candidate) {
        return Optional.ofNullable(candidate.getFilesChanged()).orElseGet(Set::of);
    }

    private record DependencyGraph(Map<String, Set<String>> requiredCandidateIds,
                                   Map<String, List<String>> dependencyReasons,
                                   Map<String, List<String>> blockedReasons) {
    }

    @Builder
    private record RawSourceFile(String fullName,
                                 String packageName,
                                 Set<String> declaredTypes,
                                 Map<String, String> explicitImports,
                                 Set<String> rawTypeReferences) {
    }

    @Builder
    private record SourceFile(String fullName,
                              Set<String> declaredTypes,
                              Set<String> referencedTypes) {
    }
}
