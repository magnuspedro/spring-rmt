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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectSelectionPlanner {

    private static final String KEY_SEPARATOR = "::";

    private final FileExtractor fileExtractor;

    public ProjectSelection plan(BaseProject project, List<String> requestedFileKeys) {
        var candidates = Optional.ofNullable(project.getCandidatesInformation()).orElseGet(List::of);
        if (candidates.isEmpty()) {
            return ProjectSelection.builder()
                    .candidates(List.of())
                    .requestedFileKeys(List.of())
                    .selectedFileKeys(List.of())
                    .selectableFileCount(0)
                    .blockedCount(0)
                    .downloadable(false)
                    .build();
        }

        var analysis = analyze(project, candidates);
        var validKeys = analysis.fileToCandidate().keySet();
        var requested = Optional.ofNullable(requestedFileKeys)
                .orElseGet(List::of)
                .stream()
                .filter(validKeys::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var selected = expand(requested, analysis.internalDependenciesByKey());

        var candidateSelections = candidates.stream()
                .map(candidate -> {
                    var fileSelections = filesChanged(candidate).stream()
                            .map(file -> {
                                var key = fileKey(candidate.getId(), file);
                                return FileSelection.builder()
                                        .key(key)
                                        .file(file)
                                        .dependencyFiles(analysis.dependencyFilesByKey().getOrDefault(key, List.of()))
                                        .blockingReasons(List.of())
                                        .requested(requested.contains(key))
                                        .selected(selected.contains(key))
                                        .locked(selected.contains(key) && !requested.contains(key))
                                        .blocked(false)
                                        .build();
                            })
                            .toList();
                    return CandidateSelection.builder()
                            .candidate(candidate)
                            .files(fileSelections)
                            .selected(fileSelections.stream().anyMatch(FileSelection::isSelected))
                            .blocked(fileSelections.stream().allMatch(FileSelection::isBlocked))
                            .build();
                })
                .toList();

        return ProjectSelection.builder()
                .candidates(candidateSelections)
                .requestedFileKeys(List.copyOf(requested))
                .selectedFileKeys(List.copyOf(selected))
                .selectableFileCount(validKeys.size())
                .blockedCount(0)
                .downloadable(!selected.isEmpty())
                .build();
    }

    public void validateSelection(BaseProject project, List<String> selectedFileKeys) {
        var candidates = Optional.ofNullable(project.getCandidatesInformation()).orElseGet(List::of);
        var analysis = analyze(project, candidates);
        var validKeys = analysis.fileToCandidate().keySet();
        var selected = Optional.ofNullable(selectedFileKeys)
                .orElseGet(List::of)
                .stream()
                .filter(validKeys::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (selected.isEmpty()) {
            throw new SelectionValidationException("Select at least one file before downloading the refactored project.", List.of());
        }

        var closure = expand(selected, analysis.internalDependenciesByKey());
        if (!closure.equals(selected)) {
            var missing = closure.stream()
                    .filter(key -> !selected.contains(key))
                    .toList();
            throw new SelectionValidationException("Some dependent files are missing from the selection.", missing);
        }
    }

    public Map<String, List<String>> groupSelectedFilesByCandidate(List<String> selectedFileKeys) {
        return Optional.ofNullable(selectedFileKeys)
                .orElseGet(List::of)
                .stream()
                .map(ProjectSelectionPlanner::parseFileKey)
                .collect(Collectors.groupingBy(FileKey::candidateId,
                        LinkedHashMap::new,
                        Collectors.mapping(FileKey::file, Collectors.toList())));
    }

    private SelectionAnalysis analyze(BaseProject project, List<CandidateInformation> candidates) {
        var sourceFiles = buildSourceFiles(project);
        var sourceFilesByPath = sourceFiles.stream()
                .collect(Collectors.toMap(SourceFile::fullName, file -> file, (left, right) -> left, LinkedHashMap::new));
        var typeToFile = new LinkedHashMap<String, String>();
        sourceFiles.forEach(sourceFile -> sourceFile.declaredTypes().forEach(type -> typeToFile.putIfAbsent(type, sourceFile.fullName())));

        var fileToCandidate = new LinkedHashMap<String, String>();
        var dependencyFilesByKey = new LinkedHashMap<String, List<String>>();
        var internalDependenciesByKey = new LinkedHashMap<String, Set<String>>();

        candidates.forEach(candidate -> {
            var candidateFiles = filesChanged(candidate);
            candidateFiles.forEach(file -> {
                var key = fileKey(candidate.getId(), file);
                fileToCandidate.put(key, candidate.getId());

                var dependencies = Optional.ofNullable(sourceFilesByPath.get(file))
                        .stream()
                        .flatMap(sourceFile -> sourceFile.referencedTypes().stream())
                        .map(typeToFile::get)
                        .filter(Objects::nonNull)
                        .filter(dependencyFile -> !dependencyFile.equals(file))
                        .distinct()
                        .toList();

                dependencyFilesByKey.put(key, dependencies);
                internalDependenciesByKey.put(key, dependencies.stream()
                        .filter(candidateFiles::contains)
                        .map(dependencyFile -> fileKey(candidate.getId(), dependencyFile))
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
            });
        });

        return new SelectionAnalysis(fileToCandidate, dependencyFilesByKey, internalDependenciesByKey);
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

    private static LinkedHashSet<String> expand(Set<String> requested, Map<String, Set<String>> dependencies) {
        var expanded = new LinkedHashSet<String>();
        var queue = new ArrayDeque<>(requested);
        while (!queue.isEmpty()) {
            var key = queue.removeFirst();
            if (!expanded.add(key)) {
                continue;
            }
            dependencies.getOrDefault(key, Set.of()).forEach(queue::addLast);
        }
        return expanded;
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

    private static Set<String> filesChanged(CandidateInformation candidate) {
        return Optional.ofNullable(candidate.getFilesChanged()).orElseGet(Set::of);
    }

    public static String fileKey(String candidateId, String file) {
        return candidateId + KEY_SEPARATOR + file;
    }

    private static FileKey parseFileKey(String key) {
        var split = key.split(KEY_SEPARATOR, 2);
        return new FileKey(split[0], split.length > 1 ? split[1] : "");
    }

    private record SelectionAnalysis(Map<String, String> fileToCandidate,
                                     Map<String, List<String>> dependencyFilesByKey,
                                     Map<String, Set<String>> internalDependenciesByKey) {
    }

    private record FileKey(String candidateId, String file) {
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
