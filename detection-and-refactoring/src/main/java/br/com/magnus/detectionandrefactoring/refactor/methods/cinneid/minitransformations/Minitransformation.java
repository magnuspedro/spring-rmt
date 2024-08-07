package br.com.magnus.detectionandrefactoring.refactor.methods.cinneid.minitransformations;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneid.helpers.Helpers;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Minitransformation {

    public static Optional<CompilationUnit> Abstraction(ClassOrInterfaceDeclaration clazz) {
        if (clazz == null || clazz.isInterface() || clazz.getMethods().isEmpty()) {
            return Optional.empty();
        }

        var cu = new CompilationUnit();
        var name = clazz.getNameAsString() + "Interface";
        var implementedTypes = NodeList.nodeList(clazz.getImplementedTypes());
        implementedTypes.add(new ClassOrInterfaceType().setName(name));
        clazz.setImplementedTypes(implementedTypes);

        return Helpers.abstractClass(clazz, name);
    }

    public static void encapsulateConstruction(ClassOrInterfaceDeclaration creator, ClassOrInterfaceDeclaration product, String createP) {
        product.getConstructors().forEach(c -> {
            var method = Helpers.makeAbstract(c, createP);
            method.ifPresent(creator::addMember);
        });

        creator.getMethods().stream()
                .filter(m -> !m.getNameAsString().equals(createP))
                .map(AstHandler::getObjectCreationExprList)
                .flatMap(Collection::stream)
                .filter(o -> o.getTypeAsString().equals(product.getNameAsString()))
                .forEach(o -> Helpers.replaceObjCreationWithMethInvocation(o, createP)
                        .ifPresent(m -> o.replace(m)));
    }

    public static void abstractAccess(ClassOrInterfaceDeclaration context, ClassOrInterfaceDeclaration concrete, ClassOrInterfaceDeclaration inf, Set<String> skipMethods) {
        Assert.isTrue(inf.isInterface(), "The class must be an interface");
        Assert.notNull(context, "The context must not be null");
        Assert.notNull(concrete, "The concrete must not be null");
        Assert.notNull(skipMethods, "The skipMethods must not be null");

        context.getFields().stream()
                .map(FieldDeclaration::getVariables)
                .flatMap(Collection::stream)
                .forEach(v -> Helpers.replaceClassWithInterface(v, inf));

        context.getMethods().stream().filter(m -> !skipMethods.contains(m.getNameAsString()))
                .map(AstHandler::getVariableDeclarations)
                .flatMap(Collection::stream)
                .forEach(v -> Helpers.replaceClassWithInterface(v, inf));
    }

    public static CompilationUnit partialAbstraction(List<JavaFile> files, ClassOrInterfaceDeclaration concrete, String newName, Set<String> abstractMethods) {
        var cu = new CompilationUnit();
        var newClass = cu.addClass(newName).addModifier(Modifier.Keyword.ABSTRACT);
        var superClass = concrete.getExtendedTypes().getFirst()
                .map(ClassOrInterfaceType::getNameAsString)
                .flatMap(sup -> files.stream().
                        filter(f -> f.getName().equals(sup))
                        .findFirst()
                        .map(JavaFile::getCompilationUnit)
                        .flatMap(AstHandler::getClassOrInterfaceDeclaration))
                .orElse(null);

        Helpers.addClass(newClass, superClass, Set.of(concrete));

        concrete.getMethods().stream()
                .filter(m -> abstractMethods.contains(m.getNameAsString()))
                .forEach(m -> newClass.getMembers().add(Helpers.abstractMethod(m)));

        concrete.getMethods().stream()
                .filter(m -> !abstractMethods.contains(m.getNameAsString()))
                .forEach(m -> Helpers.pullUpMethod(newClass, m));

        return cu;
    }
}