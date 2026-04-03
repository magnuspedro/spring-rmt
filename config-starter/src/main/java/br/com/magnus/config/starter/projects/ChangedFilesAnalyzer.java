package br.com.magnus.config.starter.projects;

import br.com.magnus.config.starter.configuration.JavaParserSingleton;
import br.com.magnus.config.starter.file.JavaFile;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

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

public final class ChangedFilesAnalyzer {

    private ChangedFilesAnalyzer() {
    }

    public static Analysis analyze(List<JavaFile> javaFiles, Set<String> changedFiles) {
        var changed = Optional.ofNullable(changedFiles)
                .orElseGet(Set::of)
                .stream()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (javaFiles == null || javaFiles.isEmpty() || changed.isEmpty()) {
            return emptyAnalysis(changed);
        }

        var sourceFiles = buildSourceFiles(javaFiles);
        var sourceFilesByPath = sourceFiles.stream()
                .collect(Collectors.toMap(SourceFile::fullName, file -> file, (left, right) -> left, LinkedHashMap::new));
        var typeToFile = new LinkedHashMap<String, String>();
        sourceFiles.forEach(sourceFile -> sourceFile.declaredTypes().forEach(type -> typeToFile.putIfAbsent(type, sourceFile.fullName())));

        var adjacencyByFile = new LinkedHashMap<String, Set<String>>();
        changed.forEach(file -> adjacencyByFile.put(file, new LinkedHashSet<>()));

        changed.forEach(file -> Optional.ofNullable(sourceFilesByPath.get(file))
                .stream()
                .flatMap(sourceFile -> sourceFile.referencedTypes().stream())
                .map(typeToFile::get)
                .filter(Objects::nonNull)
                .filter(changed::contains)
                .filter(relatedFile -> !relatedFile.equals(file))
                .distinct()
                .forEach(relatedFile -> {
                    adjacencyByFile.get(file).add(relatedFile);
                    adjacencyByFile.get(relatedFile).add(file);
                }));

        var relatedFilesByFile = adjacencyByFile.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue()),
                        (left, right) -> left,
                        LinkedHashMap::new));
        var groupedFilesByFile = buildGroupedFiles(changed, adjacencyByFile);

        return new Analysis(relatedFilesByFile, groupedFilesByFile);
    }

    private static Analysis emptyAnalysis(Set<String> changedFiles) {
        var relatedFilesByFile = new LinkedHashMap<String, List<String>>();
        var groupedFilesByFile = new LinkedHashMap<String, List<String>>();
        changedFiles.forEach(file -> {
            relatedFilesByFile.put(file, List.of());
            groupedFilesByFile.put(file, List.of(file));
        });
        return new Analysis(relatedFilesByFile, groupedFilesByFile);
    }

    private static Map<String, List<String>> buildGroupedFiles(Set<String> changedFiles, Map<String, Set<String>> adjacencyByFile) {
        var groupedFilesByFile = new LinkedHashMap<String, List<String>>();
        var visited = new LinkedHashSet<String>();

        changedFiles.forEach(file -> {
            if (!visited.add(file)) {
                return;
            }
            var component = new LinkedHashSet<String>();
            var queue = new ArrayDeque<String>();
            queue.add(file);
            component.add(file);

            while (!queue.isEmpty()) {
                var current = queue.removeFirst();
                adjacencyByFile.getOrDefault(current, Set.of()).forEach(relatedFile -> {
                    if (component.add(relatedFile)) {
                        visited.add(relatedFile);
                        queue.addLast(relatedFile);
                    }
                });
            }

            var orderedComponent = changedFiles.stream()
                    .filter(component::contains)
                    .toList();
            orderedComponent.forEach(componentFile -> groupedFilesByFile.put(componentFile, orderedComponent));
        });

        changedFiles.stream()
                .filter(file -> !groupedFilesByFile.containsKey(file))
                .forEach(file -> groupedFilesByFile.put(file, List.of(file)));

        return groupedFilesByFile;
    }

    private static List<SourceFile> buildSourceFiles(List<JavaFile> javaFiles) {
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
                            .collect(Collectors.toMap(ChangedFilesAnalyzer::getImportedSimpleName, ImportDeclaration::getNameAsString,
                                    (left, right) -> left, LinkedHashMap::new));
                    var rawTypeReferences = compilationUnit.findAll(ClassOrInterfaceType.class).stream()
                            .map(ClassOrInterfaceType::getNameAsString)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    compilationUnit.findAll(ObjectCreationExpr.class).stream()
                            .map(objectCreationExpr -> objectCreationExpr.getType().getNameAsString())
                            .forEach(rawTypeReferences::add);
                    return new RawSourceFile(javaFile.getFullName(), packageName, declaredTypes, explicitImports, rawTypeReferences);
                })
                .filter(Objects::nonNull)
                .toList();

        var knownTypesBySimpleName = new HashMap<String, Set<String>>();
        rawFiles.forEach(file -> file.declaredTypes().forEach(type -> {
            var fqcn = toQualifiedName(file.packageName(), type);
            knownTypesBySimpleName.computeIfAbsent(type, ignored -> new LinkedHashSet<>()).add(fqcn);
        }));

        return rawFiles.stream()
                .map(file -> new SourceFile(file.fullName(),
                        file.declaredTypes().stream()
                                .map(type -> toQualifiedName(file.packageName(), type))
                                .collect(Collectors.toCollection(LinkedHashSet::new)),
                        file.rawTypeReferences().stream()
                                .map(type -> resolveType(type, file, knownTypesBySimpleName))
                                .flatMap(Optional::stream)
                                .collect(Collectors.toCollection(LinkedHashSet::new))))
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

    public record Analysis(Map<String, List<String>> relatedFilesByFile,
                           Map<String, List<String>> groupedFilesByFile) {

        public List<String> relatedFiles(String file) {
            return relatedFilesByFile.getOrDefault(file, List.of());
        }

        public List<String> groupedFiles(String file) {
            return groupedFilesByFile.getOrDefault(file, List.of(file));
        }
    }

    private record RawSourceFile(String fullName,
                                 String packageName,
                                 Set<String> declaredTypes,
                                 Map<String, String> explicitImports,
                                 Set<String> rawTypeReferences) {
    }

    private record SourceFile(String fullName,
                              Set<String> declaredTypes,
                              Set<String> referencedTypes) {
    }
}
