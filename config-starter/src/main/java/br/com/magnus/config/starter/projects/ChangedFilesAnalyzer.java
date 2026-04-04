package br.com.magnus.config.starter.projects;

import br.com.magnus.config.starter.configuration.JavaParserSingleton;
import br.com.magnus.config.starter.file.JavaFile;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import lombok.extern.slf4j.Slf4j;

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

/**
 * Analyzes changed Java files to determine dependencies and group interconnected files.
 * <p>
 * This class builds a dependency graph between changed files based on type references
 * and computes connected components for grouped processing.
 */
@Slf4j
public final class ChangedFilesAnalyzer {

    private static final int DEFAULT_GROUP_SIZE = 1;

    private ChangedFilesAnalyzer() {
    }

    /**
     * Analyzes the provided Java files to determine relationships between changed files.
     *
     * @param javaFiles   all Java files in the project
     * @param changedFiles set of file paths that were modified
     * @return analysis containing related files and grouped components
     */
    public static Analysis analyze(List<JavaFile> javaFiles, Set<String> changedFiles) {
        var changed = normalizeChangedFiles(changedFiles);

        if (shouldReturnEmptyAnalysis(javaFiles, changed)) {
            return emptyAnalysis(changed);
        }

        var sourceFiles = buildSourceFiles(javaFiles);
        var typeToFile = buildTypeToFileMapping(sourceFiles);
        var adjacencyByFile = buildAdjacencyGraph(changed, sourceFiles, typeToFile);

        var relatedFilesByFile = buildRelatedFilesMapping(adjacencyByFile);
        var groupedFilesByFile = buildGroupedFiles(changed, adjacencyByFile);

        return new Analysis(relatedFilesByFile, groupedFilesByFile);
    }

    private static Set<String> normalizeChangedFiles(Set<String> changedFiles) {
        return Optional.ofNullable(changedFiles)
                .orElseGet(Set::of)
                .stream()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static boolean shouldReturnEmptyAnalysis(List<JavaFile> javaFiles, Set<String> changedFiles) {
        return javaFiles == null || javaFiles.isEmpty() || changedFiles.isEmpty();
    }

    private static Map<String, String> buildTypeToFileMapping(List<SourceFile> sourceFiles) {
        var typeToFile = new LinkedHashMap<String, String>();
        for (var sourceFile : sourceFiles) {
            for (var type : sourceFile.declaredTypes()) {
                typeToFile.putIfAbsent(type, sourceFile.fullName());
            }
        }
        return typeToFile;
    }

    private static Map<String, Set<String>> buildAdjacencyGraph(Set<String> changedFiles,
                                                                 List<SourceFile> sourceFiles,
                                                                 Map<String, String> typeToFile) {
        var sourceFilesByPath = sourceFiles.stream()
                .collect(Collectors.toMap(SourceFile::fullName, f -> f, (a, b) -> a, LinkedHashMap::new));

        var adjacencyByFile = new LinkedHashMap<String, Set<String>>();
        changedFiles.forEach(file -> adjacencyByFile.put(file, new LinkedHashSet<>()));

        for (var file : changedFiles) {
            var sourceFile = sourceFilesByPath.get(file);
            if (sourceFile == null) {
                log.debug("Changed file not found in parsed source files: {}", file);
                continue;
            }

            for (var referencedType : sourceFile.referencedTypes()) {
                var relatedFile = typeToFile.get(referencedType);
                if (shouldAddEdge(changedFiles, file, relatedFile)) {
                    addBidirectionalEdge(adjacencyByFile, file, relatedFile);
                }
            }
        }

        return adjacencyByFile;
    }

    private static boolean shouldAddEdge(Set<String> changedFiles, String currentFile, String relatedFile) {
        return relatedFile != null
                && changedFiles.contains(relatedFile)
                && !relatedFile.equals(currentFile);
    }

    private static void addBidirectionalEdge(Map<String, Set<String>> adjacencyByFile,
                                             String file1,
                                             String file2) {
        adjacencyByFile.get(file1).add(file2);
        adjacencyByFile.get(file2).add(file1);
    }

    private static Map<String, List<String>> buildRelatedFilesMapping(Map<String, Set<String>> adjacencyByFile) {
        return adjacencyByFile.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue()),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private static Analysis emptyAnalysis(Set<String> changedFiles) {
        var relatedFilesByFile = new LinkedHashMap<String, List<String>>();
        var groupedFilesByFile = new LinkedHashMap<String, List<String>>();

        for (var file : changedFiles) {
            relatedFilesByFile.put(file, List.of());
            groupedFilesByFile.put(file, List.of(file));
        }

        return new Analysis(relatedFilesByFile, groupedFilesByFile);
    }

    /**
     * Builds connected components using BFS traversal.
     * Files that reference each other (directly or transitively) are grouped together.
     */
    private static Map<String, List<String>> buildGroupedFiles(Set<String> changedFiles,
                                                                Map<String, Set<String>> adjacencyByFile) {
        var groupedFilesByFile = new LinkedHashMap<String, List<String>>();
        var visited = new LinkedHashSet<String>();

        for (var file : changedFiles) {
            if (!visited.add(file)) {
                continue;
            }

            var component = findConnectedComponent(file, adjacencyByFile);
            var orderedComponent = orderComponentByOriginalList(component, changedFiles);

            // All files in component share the same group
            for (var componentFile : orderedComponent) {
                groupedFilesByFile.put(componentFile, orderedComponent);
            }
        }

        // Handle isolated files (no connections)
        for (var file : changedFiles) {
            groupedFilesByFile.putIfAbsent(file, List.of(file));
        }

        return groupedFilesByFile;
    }

    private static Set<String> findConnectedComponent(String startFile, Map<String, Set<String>> adjacencyByFile) {
        var component = new LinkedHashSet<String>();
        var queue = new ArrayDeque<String>();

        queue.add(startFile);
        component.add(startFile);

        while (!queue.isEmpty()) {
            var current = queue.removeFirst();
            var neighbors = adjacencyByFile.getOrDefault(current, Set.of());

            for (var neighbor : neighbors) {
                if (component.add(neighbor)) {
                    queue.addLast(neighbor);
                }
            }
        }

        return component;
    }

    private static List<String> orderComponentByOriginalList(Set<String> component, Set<String> originalOrder) {
        return originalOrder.stream()
                .filter(component::contains)
                .toList();
    }

    private static List<SourceFile> buildSourceFiles(List<JavaFile> javaFiles) {
        var rawFiles = parseJavaFiles(javaFiles);
        var knownTypesBySimpleName = indexKnownTypes(rawFiles);

        return rawFiles.stream()
                .map(file -> resolveSourceFile(file, knownTypesBySimpleName))
                .filter(Objects::nonNull)
                .toList();
    }

    private static List<RawSourceFile> parseJavaFiles(List<JavaFile> javaFiles) {
        return javaFiles.stream()
                .map(ChangedFilesAnalyzer::parseJavaFile)
                .filter(Objects::nonNull)
                .toList();
    }

    private static RawSourceFile parseJavaFile(JavaFile javaFile) {
        try {
            var compilationUnit = JavaParserSingleton.getInstance()
                    .parse(javaFile.getOriginalClass())
                    .getResult()
                    .orElse(null);

            if (compilationUnit == null) {
                log.warn("Failed to parse Java file: {}", javaFile.getFullName());
                return null;
            }

            var declaredTypes = extractDeclaredTypes(compilationUnit);
            var packageName = extractPackageName(compilationUnit);
            var explicitImports = extractExplicitImports(compilationUnit);
            var rawTypeReferences = extractTypeReferences(compilationUnit);

            return new RawSourceFile(javaFile.getFullName(), packageName, declaredTypes, explicitImports, rawTypeReferences);
        } catch (Exception e) {
            log.warn("Error parsing Java file: {}", javaFile.getFullName(), e);
            return null;
        }
    }

    private static Set<String> extractDeclaredTypes(com.github.javaparser.ast.CompilationUnit compilationUnit) {
        return compilationUnit.findAll(TypeDeclaration.class).stream()
                .map(TypeDeclaration::getNameAsString)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String extractPackageName(com.github.javaparser.ast.CompilationUnit compilationUnit) {
        return compilationUnit.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("");
    }

    private static Map<String, String> extractExplicitImports(com.github.javaparser.ast.CompilationUnit compilationUnit) {
        return compilationUnit.getImports().stream()
                .filter(importDecl -> !importDecl.isAsterisk())
                .collect(Collectors.toMap(
                        importDecl -> importDecl.getName().getIdentifier(),
                        ImportDeclaration::getNameAsString,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    private static Set<String> extractTypeReferences(com.github.javaparser.ast.CompilationUnit compilationUnit) {
        var references = new LinkedHashSet<String>();

        // References in type declarations
        compilationUnit.findAll(ClassOrInterfaceType.class).stream()
                .map(ClassOrInterfaceType::getNameAsString)
                .forEach(references::add);

        // References in object creation expressions
        compilationUnit.findAll(ObjectCreationExpr.class).stream()
                .map(expr -> expr.getType().getNameAsString())
                .forEach(references::add);

        return references;
    }

    private static Map<String, Set<String>> indexKnownTypes(List<RawSourceFile> rawFiles) {
        var knownTypesBySimpleName = new HashMap<String, Set<String>>();

        for (var file : rawFiles) {
            for (var type : file.declaredTypes()) {
                var fqcn = toQualifiedName(file.packageName(), type);
                knownTypesBySimpleName
                        .computeIfAbsent(type, k -> new LinkedHashSet<>())
                        .add(fqcn);
            }
        }

        return knownTypesBySimpleName;
    }

    private static SourceFile resolveSourceFile(RawSourceFile file, Map<String, Set<String>> knownTypesBySimpleName) {
        var declaredTypes = file.declaredTypes().stream()
                .map(type -> toQualifiedName(file.packageName(), type))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        var referencedTypes = file.rawTypeReferences().stream()
                .map(type -> resolveType(type, file, knownTypesBySimpleName))
                .flatMap(Optional::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new SourceFile(file.fullName(), declaredTypes, referencedTypes);
    }

    private static Optional<String> resolveType(String simpleType,
                                                 RawSourceFile file,
                                                 Map<String, Set<String>> knownTypesBySimpleName) {
        // 1. Check explicit imports first (highest priority)
        if (file.explicitImports().containsKey(simpleType)) {
            return Optional.of(file.explicitImports().get(simpleType));
        }

        // 2. Check package-local class
        var packageLocal = toQualifiedName(file.packageName(), simpleType);
        var knownTypes = knownTypesBySimpleName.getOrDefault(simpleType, Set.of());
        if (knownTypes.contains(packageLocal)) {
            return Optional.of(packageLocal);
        }

        // 3. Unique type in known types (ambiguity = can't resolve)
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

    /**
     * Contains the results of analyzing changed files.
     *
     * @param relatedFilesByFile direct bidirectional relationships between files
     * @param groupedFilesByFile  connected components of interdependent files
     */
    public record Analysis(
            Map<String, List<String>> relatedFilesByFile,
            Map<String, List<String>> groupedFilesByFile) {

        public List<String> relatedFiles(String file) {
            return relatedFilesByFile.getOrDefault(file, List.of());
        }

        public List<String> groupedFiles(String file) {
            return groupedFilesByFile.getOrDefault(file, List.of(file));
        }
    }

    private record RawSourceFile(
            String fullName,
            String packageName,
            Set<String> declaredTypes,
            Map<String, String> explicitImports,
            Set<String> rawTypeReferences) {
    }

    private record SourceFile(
            String fullName,
            Set<String> declaredTypes,
            Set<String> referencedTypes) {
    }
}
