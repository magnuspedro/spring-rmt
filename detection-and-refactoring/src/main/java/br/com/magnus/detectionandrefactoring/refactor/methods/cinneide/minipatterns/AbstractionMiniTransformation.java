package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideTransformationException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;

public class AbstractionMiniTransformation implements MiniTransformation {

    private final String concreteClassName;
    private final String interfaceName;

    public AbstractionMiniTransformation(String concreteClassName, String interfaceName) {
        this.concreteClassName = concreteClassName;
        this.interfaceName = interfaceName;
    }

    @Override
    public void apply(CinneideContext context) {
        var concreteClass = context.classDeclaration(concreteClassName);
        var concreteCu = context.compilationUnit(concreteClassName);

        if (concreteClass.getImplementedTypes().stream().noneMatch(i -> i.getNameAsString().equals(interfaceName))) {
            concreteClass.addImplementedType(interfaceName);
        }

        CompilationUnit interfaceCu;
        com.github.javaparser.ast.body.ClassOrInterfaceDeclaration interfaceDcl;
        try {
            interfaceCu = context.compilationUnit(interfaceName);
            interfaceDcl = context.classDeclaration(interfaceName);
        } catch (CinneideTransformationException ignored) {
            interfaceCu = new CompilationUnit();
            var packageDeclaration = concreteCu.getPackageDeclaration();
            if (packageDeclaration.isPresent()) {
                interfaceCu.setPackageDeclaration(packageDeclaration.get().getNameAsString());
            }
            interfaceDcl = interfaceCu.addInterface(interfaceName);
        }
        var finalInterfaceCu = interfaceCu;
        var finalInterfaceDcl = interfaceDcl;

        AstHandler.getMethods(concreteClass).stream()
                .filter(MethodDeclaration::isPublic)
                .filter(method -> finalInterfaceDcl.getMethodsByName(method.getNameAsString()).stream()
                        .noneMatch(existing -> existing.getSignature().equals(method.getSignature())))
                .forEach(method -> finalInterfaceDcl.addMember(toAbstractSignature(method)));

        var path = context.classFile(concreteClassName).getPath();
        context.addClass(interfaceName, path, finalInterfaceCu);
    }

    private MethodDeclaration toAbstractSignature(MethodDeclaration method) {
        var signature = new MethodDeclaration();
        signature.setName(method.getNameAsString());
        signature.setType(method.getType());
        signature.setModifiers(Modifier.Keyword.PUBLIC);
        method.getParameters().forEach(signature::addParameter);
        return signature;
    }
}
