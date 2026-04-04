package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideTransformationException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Mini transformation for extracting an interface from a concrete class.
 * <p>
 * Creates a new interface containing the public methods of the concrete class.
 * If the interface already exists, adds any missing public methods.
 * The concrete class is then modified to implement the interface.
 */
public class AbstractionMiniTransformation implements MiniTransformation {

    /**
     * The name of the concrete class to extract interface from.
     */
    private final String concreteClassName;

    /**
     * The name of the interface to create.
     */
    private final String interfaceName;

    /**
     * Creates a new abstraction mini transformation.
     *
     * @param concreteClassName the concrete class name
     * @param interfaceName      the interface name to create
     */
    public AbstractionMiniTransformation(String concreteClassName, String interfaceName) {
        this.concreteClassName = concreteClassName;
        this.interfaceName = interfaceName;
    }

    /**
     * Applies the abstraction transformation.
     * <p>
     * Creates or updates an interface with the public methods from the concrete class.
     *
     * @param context the refactoring context
     */
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

    /**
     * Converts a concrete method to an abstract method signature.
     *
     * @param method the method to convert
     * @return the abstract method signature
     */
    private MethodDeclaration toAbstractSignature(MethodDeclaration method) {
        var signature = new MethodDeclaration();
        signature.setName(method.getNameAsString());
        signature.setType(method.getType());
        signature.setModifiers(Modifier.Keyword.PUBLIC);
        method.getParameters().forEach(signature::addParameter);
        return signature;
    }
}
