package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AbstractSyntaxTree;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class CinneideContext {

    private final List<JavaFile> javaFiles;

    public CinneideContext(List<JavaFile> javaFiles) {
        this.javaFiles = javaFiles;
    }

    public List<JavaFile> files() {
        return javaFiles;
    }

    public JavaFile classFile(String className) {
        return javaFiles.stream()
                .filter(file -> file.getFileNameWithoutExtension().equals(className))
                .findFirst()
                .orElseThrow(() -> new CinneideTransformationException("Class not found: " + className));
    }

    public CompilationUnit compilationUnit(String className) {
        return classFile(className).getCompilationUnit();
    }

    public ClassOrInterfaceDeclaration classDeclaration(String className) {
        return AstHandler.getClassOrInterfaceDeclaration(compilationUnit(className))
                .orElseThrow(() -> new CinneideTransformationException("Class declaration not found: " + className));
    }

    public Optional<PackageDeclaration> packageDeclaration(String className) {
        return compilationUnit(className).getPackageDeclaration();
    }

    public void addClass(String className, String path, CompilationUnit cu) {
        var javaFile = JavaFile.builder()
                .name(className + ".java")
                .path(path)
                .originalClass(cu.toString())
                .parsed(AbstractSyntaxTree.parseSingle(cu.toString()))
                .build();
        var existingIndex = IntStream.range(0, javaFiles.size())
                .filter(index -> javaFiles.get(index).getFileNameWithoutExtension().equals(className))
                .findFirst();
        if (existingIndex.isPresent()) {
            javaFiles.set(existingIndex.getAsInt(), javaFile);
            return;
        }
        javaFiles.add(javaFile);
    }
}
