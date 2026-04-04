package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.List;
import java.util.Set;

/**
 * Mini transformation for creating a wrapper class.
 * <p>
 * Creates a wrapper (decorator) class that implements an interface
 * and delegates all calls to a wrapped receiver object.
 * Updates client classes to use the wrapper instead of direct implementations.
 */
public class WrapperMiniTransformation implements MiniTransformation {

    /**
     * The names of client classes to update.
     */
    private final Set<String> clientClassNames;

    /**
     * The interface name to implement.
     */
    private final String interfaceName;

    /**
     * The name of the wrapper class to create.
     */
    private final String wrapperClassName;

    /**
     * Creates a new wrapper mini transformation.
     *
     * @param clientClassNames the client class names to update
     * @param interfaceName     the interface to implement
     * @param wrapperClassName  the wrapper class name to create
     */
    public WrapperMiniTransformation(Set<String> clientClassNames, String interfaceName, String wrapperClassName) {
        this.clientClassNames = clientClassNames;
        this.interfaceName = interfaceName;
        this.wrapperClassName = wrapperClassName;
    }

    /**
     * Applies the wrapper transformation.
     * <p>
     * Creates a wrapper class that delegates to a receiver,
     * and updates client classes to use the wrapper.
     *
     * @param context the refactoring context
     */
    @Override
    public void apply(CinneideContext context) {
        var interfaceFile = context.classFile(interfaceName);
        var interfaceDcl = AstHandler.getClassOrInterfaceDeclaration(interfaceFile.getCompilationUnit())
                .orElseThrow(() -> new IllegalArgumentException("Interface not found: " + interfaceName));

        var wrapperCu = new CompilationUnit();
        interfaceFile.getCompilationUnit().getPackageDeclaration().ifPresent(pkg -> wrapperCu.setPackageDeclaration(pkg.getNameAsString()));
        var wrapperClass = wrapperCu.addClass(wrapperClassName);
        wrapperClass.addImplementedType(interfaceName);
        wrapperClass.addFieldWithInitializer(interfaceName, "receiver", new NameExpr("receiver"), Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);
        var constructor = wrapperClass.addConstructor(Modifier.Keyword.PUBLIC);
        constructor.addParameter(interfaceName, "receiver");
        constructor.setBody(new BlockStmt().addStatement(new AssignExpr(new FieldAccessExpr(new ThisExpr(), "receiver"), new NameExpr("receiver"), AssignExpr.Operator.ASSIGN)));
        var getter = wrapperClass.addMethod("getReceiver", Modifier.Keyword.PUBLIC);
        getter.setType(interfaceName);
        getter.setBody(new BlockStmt().addStatement(new ReturnStmt(new NameExpr("receiver"))));

        AstHandler.getMethods(interfaceDcl).forEach(interfaceMethod -> wrapperClass.addMember(delegateMethod(interfaceMethod)));
        context.addClass(wrapperClassName, interfaceFile.getPath(), wrapperCu);

        var implClassNames = context.files().stream()
                .map(JavaFile::getCompilationUnit)
                .map(AstHandler::getClassOrInterfaceDeclaration)
                .flatMap(java.util.Optional::stream)
                .filter(dcl -> dcl.getImplementedTypes().stream().anyMatch(t -> t.getNameAsString().equals(interfaceName)))
                .map(dcl -> dcl.getNameAsString())
                .toList();

        for (var clientClassName : clientClassNames) {
            var clientClass = context.classDeclaration(clientClassName);
            clientClass.findAll(ObjectCreationExpr.class).stream()
                    .filter(creation -> implClassNames.contains(creation.getType().getNameAsString()))
                    .forEach(creation -> {
                        var wrapped = new ObjectCreationExpr();
                        wrapped.setType(wrapperClassName);
                        wrapped.addArgument(creation.clone());
                        creation.replace(wrapped);
                    });
            clientClass.findAll(VariableDeclarator.class).forEach(var -> {
                if (var.getType().isClassOrInterfaceType()
                        && implClassNames.contains(var.getType().asClassOrInterfaceType().getNameAsString())) {
                    var.setType(new ClassOrInterfaceType(wrapperClassName));
                }
            });
        }
    }

    /**
     * Creates a delegating method for the wrapper.
     *
     * @param interfaceMethod the interface method to delegate
     * @return the delegating method
     */
    private MethodDeclaration delegateMethod(MethodDeclaration interfaceMethod) {
        var method = new MethodDeclaration();
        method.setName(interfaceMethod.getNameAsString());
        method.setType(interfaceMethod.getType());
        method.setModifiers(Modifier.Keyword.PUBLIC);
        interfaceMethod.getParameters().forEach(parameter -> method.addParameter(new Parameter(parameter.getType(), parameter.getName())));

        var delegateCall = new MethodCallExpr(new NameExpr("receiver"), method.getNameAsString());
        method.getParameters().stream()
                .map(Parameter::getNameAsString)
                .map(NameExpr::new)
                .forEach(delegateCall::addArgument);

        if (method.getType().isVoidType()) {
            method.setBody(new BlockStmt().addStatement(delegateCall));
        } else {
            method.setBody(new BlockStmt().addStatement(new ReturnStmt(delegateCall)));
        }

        return method;
    }
}
