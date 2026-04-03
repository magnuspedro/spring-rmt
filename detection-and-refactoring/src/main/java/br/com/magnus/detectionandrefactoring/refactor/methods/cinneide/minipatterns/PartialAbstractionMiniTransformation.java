package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;

import java.util.List;
import java.util.Set;

public class PartialAbstractionMiniTransformation implements MiniTransformation {

    private final String concreteClassName;
    private final String abstractClassName;
    private final Set<String> abstractMethodNames;

    public PartialAbstractionMiniTransformation(String concreteClassName, String abstractClassName, Set<String> abstractMethodNames) {
        this.concreteClassName = concreteClassName;
        this.abstractClassName = abstractClassName;
        this.abstractMethodNames = abstractMethodNames;
    }

    @Override
    public void apply(CinneideContext context) {
        var concreteClass = context.classDeclaration(concreteClassName);
        var concreteCu = context.compilationUnit(concreteClassName);
        var abstractCu = new CompilationUnit();
        concreteCu.getPackageDeclaration().ifPresent(pkg -> abstractCu.setPackageDeclaration(pkg.getNameAsString()));
        var abstractClass = abstractCu.addClass(abstractClassName);
        abstractClass.setAbstract(true);

        var methods = List.copyOf(AstHandler.getMethods(concreteClass));
        for (var method : methods) {
            if (abstractMethodNames.contains(method.getNameAsString())) {
                abstractClass.addMember(toAbstractSignature(method));
                continue;
            }
            abstractClass.addMember(method.clone());
            concreteClass.remove(method);
        }

        concreteClass.getExtendedTypes().clear();
        concreteClass.addExtendedType(abstractClassName);
        var path = context.classFile(concreteClassName).getPath();
        context.addClass(abstractClassName, path, abstractCu);
    }

    private MethodDeclaration toAbstractSignature(MethodDeclaration method) {
        var signature = new MethodDeclaration();
        signature.setName(method.getNameAsString());
        signature.setType(method.getType());
        if (method.isProtected()) {
            signature.setModifiers(Modifier.Keyword.PROTECTED, Modifier.Keyword.ABSTRACT);
        } else {
            signature.setModifiers(Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT);
        }
        method.getParameters().forEach(parameter -> signature.addParameter(new Parameter(parameter.getType(), parameter.getName())));
        return signature;
    }
}
