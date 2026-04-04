package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;

import java.util.List;
import java.util.Set;

/**
 * Mini transformation for creating a partial abstract superclass.
 * <p>
 * Creates an abstract class containing selected methods as abstract,
 * and moves other methods to the abstract class.
 * The original concrete class extends the new abstract class.
 */
public class PartialAbstractionMiniTransformation implements MiniTransformation {

    /**
     * The name of the concrete class.
     */
    private final String concreteClassName;

    /**
     * The name of the abstract class to create.
     */
    private final String abstractClassName;

    /**
     * The names of methods to make abstract.
     */
    private final Set<String> abstractMethodNames;

    /**
     * Creates a new partial abstraction mini transformation.
     *
     * @param concreteClassName   the concrete class name
     * @param abstractClassName   the abstract class name to create
     * @param abstractMethodNames methods to make abstract
     */
    public PartialAbstractionMiniTransformation(String concreteClassName, String abstractClassName, Set<String> abstractMethodNames) {
        this.concreteClassName = concreteClassName;
        this.abstractClassName = abstractClassName;
        this.abstractMethodNames = abstractMethodNames;
    }

    /**
     * Applies the partial abstraction transformation.
     * <p>
     * Creates an abstract class with abstract methods and common methods,
     * then modifies the concrete class to extend it.
     *
     * @param context the refactoring context
     */
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

    /**
     * Converts a concrete method to an abstract method signature.
     * <p>
     * Preserves the visibility: protected methods stay protected,
     * public methods stay public.
     *
     * @param method the method to convert
     * @return the abstract method signature
     */
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
