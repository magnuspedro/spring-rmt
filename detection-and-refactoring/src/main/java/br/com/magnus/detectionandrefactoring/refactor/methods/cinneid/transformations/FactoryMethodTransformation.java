package br.com.magnus.detectionandrefactoring.refactor.methods.cinneid.transformations;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneid.minitransformations.Minitransformation;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FactoryMethodTransformation {

    public boolean preCondition(List<JavaFile> files, CompilationUnit creator) {
        var candidates = new ArrayList<ClassOrInterfaceDeclaration>();

        var objectCreation = AstHandler.getObjectCreationExprList(creator);
        var objectCreationTypes = objectCreation.stream()
                .map(ObjectCreationExpr::getTypeAsString)
                .collect(Collectors.toSet());
        var objectCreationVariables = objectCreation.stream()
                .map(ObjectCreationExpr::getParentNode)
                .flatMap(Optional::stream)
                .filter(VariableDeclarator.class::isInstance)
                .toList();
        var fields = AstHandler.getMethodCallExpr(creator)
                .stream()
                .filter(m -> m.getScope().isPresent())
                .filter(m -> objectCreationVariables.contains(m.getScope().get()))
                .map(MethodCallExpr::getNameAsString)
                .collect(Collectors.toSet());
        var creationFiles = files.stream()
                .filter(f -> objectCreationTypes.contains(f.getName()))
                .map(JavaFile::getCompilationUnit)
                .map(AstHandler::getClassOrInterfaceDeclaration)
                .flatMap(Optional::stream)
                .toList();
        creationFiles.forEach(c -> {
            var fieldSet = c.getFields()
                    .stream()
                    .filter(FieldDeclaration::isPublic)
                    .map(FieldDeclaration::getVariables)
                    .flatMap(NodeList::stream)
                    .map(VariableDeclarator::getNameAsString)
                    .collect(Collectors.toSet());
            fieldSet.forEach(f -> {
                if (fields.stream().noneMatch(f::equals)) {
                    candidates.add(c);
                }
            });
        });

        var createdStaticMethods = candidates.stream()
                .map(ClassOrInterfaceDeclaration::getMethods)
                .flatMap(List::stream)
                .filter(MethodDeclaration::isStatic)
                .map(MethodDeclaration::getNameAsString)
                .collect(Collectors.toSet());

        var staticCall = AstHandler.getMethodCallExpr(creator)
                .stream()
                .filter(m -> m.getScope().isPresent())
                .filter(m -> candidates.stream().map(NodeWithSimpleName::getNameAsString).toList().contains(AstHandler.getNameExpr(m).get().getNameAsString()));


        return true;
    }

    public void applyFactoryMethod(List<JavaFile> javaFiles, ClassOrInterfaceDeclaration creator,
                                   ClassOrInterfaceDeclaration product, String productInf, String absCreator, String createProduct) {
        var crateProductClazz = javaFiles.stream()
                .filter(file -> file.getName().equals(createProduct))
                .findFirst()
                .map(JavaFile::getCompilationUnit)
                .flatMap(AstHandler::getClassOrInterfaceDeclaration)
                .orElseThrow(
                        () -> new IllegalArgumentException("The class " + createProduct + " should be on the project"))
                .getMethods()
                .stream().map(NodeWithSimpleName::getNameAsString)
                .collect(Collectors.toSet());
        var infCu = Minitransformation.abstraction(product, productInf)
                .orElseThrow(() -> new IllegalArgumentException("Compilation Unit Interface Should exists"));
        var infClazz = AstHandler.getClassOrInterfaceDeclaration(infCu)
                .orElseThrow(() -> new IllegalArgumentException("Interface Should exists"));
        Minitransformation.encapsulateConstruction(creator, product, createProduct);
        Minitransformation.abstractAccess(creator, product, infClazz, crateProductClazz);
        Minitransformation.partialAbstraction(javaFiles, creator, absCreator, Set.of(createProduct));
    }
}
