package br.com.magnus.detectionandrefactoring.refactor.methods.cinneid.minitransformations;

import br.com.magnus.detectionandrefactoring.refactor.methods.cinneid.helpers.Helpers;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class Minitransformation {

    public static ClassOrInterfaceDeclaration Abstraction(ClassOrInterfaceDeclaration clazz) {
        var name = clazz.getNameAsString() + "Interface";
        var implementedTypes = NodeList.nodeList(clazz.getImplementedTypes());
        implementedTypes.add(new ClassOrInterfaceType().setName(name));
        clazz.setImplementedTypes(implementedTypes);
        var newClazz = Helpers.abstractClass(clazz, name);
        return newClazz.orElse(null);
    }

    public static void encapsulateConstruction(ClassOrInterfaceDeclaration creator, ClassOrInterfaceDeclaration product, String createP) {
        product.getConstructors().forEach(c -> {
            var method = Helpers.makeAbstract(c, createP);
            method.ifPresent(creator::addMember);
        });
    }
}
