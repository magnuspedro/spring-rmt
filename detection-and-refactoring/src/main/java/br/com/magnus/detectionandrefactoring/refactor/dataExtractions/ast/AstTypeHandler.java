package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.exceptions.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.Optional;

/**
 * Handler for type and class-related AST operations.
 * <p>
 * Provides methods for extracting type information, package declarations,
 * and checking compilation unit compatibility.
 */
public final class AstTypeHandler {

    private AstTypeHandler() {
    }

    /**
     * Gets the parent type of a compilation unit.
     *
     * @param cUnit the compilation unit
     * @return Optional containing the parent class or interface type
     */
    public static Optional<ClassOrInterfaceType> getParentType(CompilationUnit cUnit) {
        return AstNodeHandler.getClassOrInterfaceDeclaration(cUnit)
                .flatMap(classOrInterfaceDeclaration -> classOrInterfaceDeclaration
                        .getChildNodes()
                        .stream()
                        .filter(ClassOrInterfaceType.class::isInstance)
                        .map(ClassOrInterfaceType.class::cast)
                        .findFirst());
    }

    /**
     * Gets the parent type of a class declaration.
     *
     * @param classDclr the class declaration
     * @return Optional containing the parent class or interface type
     * @throws NoClassOrInterfaceDeclarationException if classDclr is null
     */
    public static Optional<ClassOrInterfaceType> getParentType(ClassOrInterfaceDeclaration classDclr) {
        return Optional.ofNullable(classDclr)
                .map(Node::getParentNode)
                .orElseThrow(NoClassOrInterfaceDeclarationException::new)
                .stream()
                .filter(ClassOrInterfaceType.class::isInstance)
                .map(ClassOrInterfaceType.class::cast)
                .findFirst();
    }

    /**
     * Gets the package declaration from a compilation unit.
     *
     * @param cUnit the compilation unit
     * @return the package declaration
     * @throws NullCompilationUnitException if cUnit is null
     * @throws NoPackageDeclarationException if no package declaration found
     */
    public static PackageDeclaration getPackageDeclaration(CompilationUnit cUnit) {
        return Optional.ofNullable(cUnit)
                .map(Node::getChildNodes)
                .orElseThrow(NullCompilationUnitException::new)
                .stream()
                .filter(PackageDeclaration.class::isInstance)
                .map(PackageDeclaration.class::cast)
                .findFirst()
                .orElseThrow(NoPackageDeclarationException::new);
    }

    /**
     * Checks if two compilation units match by package and class name.
     *
     * @param c1                  first compilation unit
     * @param classOrInterface2   optional class declaration from second unit
     * @param package2            optional package declaration from second unit
     * @return true if they match
     */
    public static boolean doesCompilationUnitsMatch(CompilationUnit c1,
                                                      Optional<ClassOrInterfaceDeclaration> classOrInterface2,
                                                      Optional<PackageDeclaration> package2) {
        var p1 = Optional.ofNullable(c1)
                .flatMap(CompilationUnit::getPackageDeclaration)
                .map(PackageDeclaration::getNameAsString)
                .orElse("");
        var p2 = package2
                .map(PackageDeclaration::getNameAsString)
                .orElse("");

        var type1 = AstNodeHandler.getClassOrInterfaceDeclaration(c1)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElse("");
        var type2 = classOrInterface2
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElse("");

        return p1.equals(p2) && !type1.isEmpty() && type1.equals(type2);
    }

    /**
     * Checks if two compilation units match.
     *
     * @param c1 first compilation unit
     * @param c2 second compilation unit
     * @return true if they match
     */
    public static boolean doesCompilationUnitsMatch(CompilationUnit c1, CompilationUnit c2) {
        return doesCompilationUnitsMatch(c1,
                AstNodeHandler.getClassOrInterfaceDeclaration(c2),
                c2.getPackageDeclaration());
    }
}
