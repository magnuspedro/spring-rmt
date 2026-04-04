package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.List;
import java.util.Set;

/**
 * Mini transformation for extracting methods to a delegation class.
 * <p>
 * Moves specified methods from the context class to a new delegation class.
 * The context class retains delegating wrapper methods that forward calls
 * to the delegation instance.
 */
public class DelegationMiniTransformation implements MiniTransformation {

    /**
     * The name of the context class to extract methods from.
     */
    private final String contextClassName;

    /**
     * The names of methods to move to the delegation class.
     */
    private final Set<String> moveMethodNames;

    /**
     * The name of the delegation class to create.
     */
    private final String delegationClassName;

    /**
     * Creates a new delegation mini transformation.
     *
     * @param contextClassName    the context class name
     * @param moveMethodNames     the method names to move
     * @param delegationClassName the delegation class name
     */
    public DelegationMiniTransformation(String contextClassName, Set<String> moveMethodNames, String delegationClassName) {
        this.contextClassName = contextClassName;
        this.moveMethodNames = moveMethodNames;
        this.delegationClassName = delegationClassName;
    }

    /**
     * Applies the delegation transformation.
     * <p>
     * Creates a new delegation class with the specified methods and
     * replaces the original methods with delegating wrappers.
     *
     * @param context the refactoring context
     */
    @Override
    public void apply(CinneideContext context) {
        var contextClass = context.classDeclaration(contextClassName);
        var contextCu = context.compilationUnit(contextClassName);
        var delegationCu = new CompilationUnit();
        contextCu.getPackageDeclaration().ifPresent(pkg -> delegationCu.setPackageDeclaration(pkg.getNameAsString()));
        var delegationClass = delegationCu.addClass(delegationClassName);

        if (contextClass.getFieldByName("delegation").isEmpty()) {
            var field = new VariableDeclarator(new ClassOrInterfaceType(delegationClassName), "delegation", new ObjectCreationExpr().setType(delegationClassName));
            contextClass.addMember(new com.github.javaparser.ast.body.FieldDeclaration().addVariable(field).setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL));
        }

        var contextMethods = List.copyOf(AstHandler.getMethods(contextClass));
        for (var method : contextMethods) {
            if (!moveMethodNames.contains(method.getNameAsString())) {
                continue;
            }
            var moved = method.clone();
            moved.setModifiers(Modifier.Keyword.PUBLIC);
            delegationClass.addMember(moved);
            method.setBody(delegateBody(method));
        }

        var path = context.classFile(contextClassName).getPath();
        context.addClass(delegationClassName, path, delegationCu);
    }

    /**
     * Creates a delegating method body.
     *
     * @param method the original method
     * @return a block statement that delegates to the delegation object
     */
    private BlockStmt delegateBody(MethodDeclaration method) {
        var call = new MethodCallExpr(new NameExpr("delegation"), method.getNameAsString());
        method.getParameters().stream()
                .map(Parameter::getNameAsString)
                .map(NameExpr::new)
                .forEach(call::addArgument);
        if (method.getType().isVoidType()) {
            return new BlockStmt().addStatement(new ExpressionStmt(call));
        }
        return new BlockStmt().addStatement(new ReturnStmt(call));
    }
}
