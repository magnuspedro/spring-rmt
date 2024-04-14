package br.com.detection.detectionagent.domain.methods.weiL.verifiers;

import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014Canditate;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014FactoryCanditate;
import br.com.detection.detectionagent.file.JavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class WeiEtAl2014FactoryVerifier extends WeiEtAl2014Verifier {

    protected boolean ifStmtsAreValid(List<JavaFile> dataHandler, CompilationUnit parsedClazz,
                                      ClassOrInterfaceDeclaration classOrInterface, MethodDeclaration method, Collection<IfStmt> ifStatements) {
        final Optional<ClassOrInterfaceType> baseType = this.astHandler
                .getMethodReturnClassType(method);

        if (baseType.isEmpty()) {
            return false;
        }

        final Parameter parameter = method.getParameters().stream().findFirst().get();

        return !ifStatements.isEmpty()
                && ifStatements.stream().allMatch(s -> ifStmtIsValid(dataHandler, baseType.get(), parameter, s));
    }

    private boolean ifStmtIsValid(List<JavaFile> dataHandler, ClassOrInterfaceType baseType, Parameter parameter,
                                  IfStmt ifStmt) {

        final Optional<BinaryExpr> binaryExpr = ifStmt.getChildNodes().stream().filter(BinaryExpr.class::isInstance)
                .map(BinaryExpr.class::cast).findFirst();

        final Optional<MethodCallExpr> methodCallExpr = ifStmt.getChildNodes().stream()
                .filter(MethodCallExpr.class::isInstance).map(MethodCallExpr.class::cast).findFirst();

        final boolean parameterIsUsed = (binaryExpr.isPresent() || methodCallExpr.isPresent())
                && isParameterUsedInIfStmtConditional(parameter, binaryExpr, methodCallExpr);

        final boolean hasValidReturn = this.hasReturnTypeAndHasValidSubtype(dataHandler, baseType, ifStmt);

        return parameterIsUsed && hasValidReturn;
    }

    private boolean hasReturnTypeAndHasValidSubtype(List<JavaFile> dataHandler, ClassOrInterfaceType baseType,
                                                    IfStmt ifStmt) {
        final Optional<ReturnStmt> returnStmt = this.astHandler.getReturnStmt(ifStmt);

        if (returnStmt.isPresent()) {
            final Optional<Node> node = returnStmt.get().getChildNodes().stream().findFirst();

            if (node.filter(NameExpr.class::isInstance).isPresent()) {

                final String returnName = node.map(NameExpr.class::cast).get().getNameAsString();

                final Optional<VariableDeclarator> varDclr = this.astHandler.getVariableDeclarationInNode(ifStmt.getThenStmt(),
                        returnName);

                final Optional<ObjectCreationExpr> objectCreationExpr = varDclr
                        .map(this.astHandler::getObjectCreationExpr).filter(Optional::isPresent).map(Optional::get);

                return this.isOfTypeOrIsSubtype(dataHandler, baseType, objectCreationExpr);
            } else if (node.filter(ObjectCreationExpr.class::isInstance).isPresent()) {

                final Optional<ObjectCreationExpr> objCreationExpr = node.map(ObjectCreationExpr.class::cast);

                return this.isOfTypeOrIsSubtype(dataHandler, baseType, objCreationExpr);
            }
        }
        return false;
    }

    private boolean isOfTypeOrIsSubtype(List<JavaFile> dataHandler, ClassOrInterfaceType type,
                                        Optional<ObjectCreationExpr> objCreationExpr) {

        if (objCreationExpr.isEmpty()) {
            return false;
        }

        final Optional<ClassOrInterfaceType> classOrInterfaceType = objCreationExpr.map(ObjectCreationExpr::getType);

        if (classOrInterfaceType.isEmpty()) {
            return false;
        }

        final Optional<CompilationUnit> cu =
                dataHandler.stream().filter(f -> f.getName().equals(classOrInterfaceType.map(NodeWithSimpleName::getNameAsString).orElse("")))
                        .map(m -> (CompilationUnit) m.getParsed())
                        .findFirst();

        final Optional<ClassOrInterfaceDeclaration> declaration = cu.flatMap(this.astHandler::getClassOrInterfaceDeclaration);

        return declaration.isPresent() && (declaration.get().getExtendedTypes().stream().anyMatch(t -> t.equals(type)) || declaration.get().getImplementedTypes().stream().anyMatch(t -> t.equals(type)));
    }

    private boolean isParameterUsedInIfStmtConditional(Parameter parameter, Optional<BinaryExpr> binaryExpr,
                                                       Optional<MethodCallExpr> methodCallExpr) {

        if (binaryExpr.isPresent()) {
            final String name2 = this.astHandler.getNameExpr(binaryExpr.get()).map(NodeWithSimpleName::getNameAsString).orElse("");

            return parameter.getNameAsString().equals(name2)
                    && BinaryExpr.Operator.EQUALS.equals(binaryExpr.get().getOperator());
        } else if (methodCallExpr.isPresent()) {

            final boolean hasParam = methodCallExpr.get().getChildNodes().stream().filter(NameExpr.class::isInstance)
                    .map(NameExpr.class::cast).anyMatch(n -> n.getNameAsString().equals(parameter.getNameAsString()));

            final boolean isAnEqualsMethod = methodCallExpr.get().getChildNodes().stream()
                    .filter(SimpleName.class::isInstance).map(SimpleName.class::cast).anyMatch(n -> n.asString().equals(parameter.getNameAsString()));

            return hasParam && isAnEqualsMethod;
        }

        return false;
    }

    @Override
    protected WeiEtAl2014Canditate createCandidate(JavaFile file, CompilationUnit parsedClazz,
                                                   PackageDeclaration pkgDcl, ClassOrInterfaceDeclaration classOrInterface, MethodDeclaration method,
                                                   Collection<IfStmt> ifStatements) {

        final ClassOrInterfaceType methodReturnType = this.astHandler.getMethodReturnClassType(method)
                .orElse(null);

        return new WeiEtAl2014FactoryCanditate(file, parsedClazz, pkgDcl, classOrInterface, method,
                methodReturnType, ifStatements);
    }

}
