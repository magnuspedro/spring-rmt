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

public class WrapperMiniTransformation implements MiniTransformation {

    private final Set<String> clientClassNames;
    private final String interfaceName;
    private final String wrapperClassName;

    public WrapperMiniTransformation(Set<String> clientClassNames, String interfaceName, String wrapperClassName) {
        this.clientClassNames = clientClassNames;
        this.interfaceName = interfaceName;
        this.wrapperClassName = wrapperClassName;
    }

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
