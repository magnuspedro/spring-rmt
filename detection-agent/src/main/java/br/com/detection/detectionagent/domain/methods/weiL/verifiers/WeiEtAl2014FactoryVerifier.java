package br.com.detection.detectionagent.domain.methods.weiL.verifiers;

import br.com.detection.detectionagent.domain.dataExtractions.ast.AstHandler;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014Candidate;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014FactoryCandidate;
import br.com.detection.detectionagent.file.JavaFile;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class WeiEtAl2014FactoryVerifier extends WeiEtAl2014Verifier {

    protected boolean areIfStmtsValid(List<JavaFile> javaFiles, JavaFile file, MethodDeclaration method, Collection<IfStmt> ifStatements) {
        final var baseType = AstHandler.getMethodReturnClassType(method);
        if (baseType.isEmpty()) {
            return false;
        }

        final var parameter = method.getParameters()
                .stream()
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

        return !ifStatements.isEmpty()
                && ifStatements.stream().allMatch(ifStmt -> ifStmtIsValid(javaFiles, baseType.get(), parameter, ifStmt));
    }

    private boolean ifStmtIsValid(List<JavaFile> dataHandler, ClassOrInterfaceType baseType, Parameter parameter, IfStmt ifStmt) {

        final var binaryExpr = ifStmt.getChildNodes().stream()
                .filter(BinaryExpr.class::isInstance)
                .map(BinaryExpr.class::cast)
                .findFirst();

        final var objectCreationExpr = AstHandler.getObjectCreationExprList(ifStmt);

        final boolean parameterIsUsed = (binaryExpr.isPresent() || !objectCreationExpr.isEmpty())
                && objectCreationExpr.stream().anyMatch(it -> isParameterUsedInIfStmtConditional(parameter, binaryExpr, it));

        final boolean hasValidReturn = this.hasReturnTypeAndHasValidSubtype(dataHandler, baseType, ifStmt);

        return parameterIsUsed && hasValidReturn;
    }

    private boolean hasReturnTypeAndHasValidSubtype(List<JavaFile> javaFiles, ClassOrInterfaceType baseType, IfStmt ifStmt) {

        final var returnStmt = AstHandler.getReturnStmt(ifStmt);
        if (returnStmt.isPresent()) {

            final var node = returnStmt.get().getChildNodes().stream().findFirst();
            if (node.filter(NameExpr.class::isInstance).isPresent()) {
                final var returnName = node.map(NameExpr.class::cast).get().getNameAsString();
                final var varDclr = AstHandler.getVariableDeclarationInNode(ifStmt.getThenStmt(), returnName);
                final var objectCreationExpr = varDclr.map(AstHandler::getObjectCreationExpr)
                        .filter(Optional::isPresent)
                        .map(Optional::get);

                return this.isOfTypeOrIsSubtype(javaFiles, baseType, objectCreationExpr);
            } else if (node.filter(ObjectCreationExpr.class::isInstance).isPresent()) {

                final var objCreationExpr = node.map(ObjectCreationExpr.class::cast);
                return this.isOfTypeOrIsSubtype(javaFiles, baseType, objCreationExpr);
            }
        }
        return false;
    }

    private boolean isOfTypeOrIsSubtype(List<JavaFile> javaFiles, ClassOrInterfaceType type,
                                        Optional<ObjectCreationExpr> objCreationExpr) {

        if (objCreationExpr.isEmpty()) {
            return false;
        }

        final var classOrInterfaceType = objCreationExpr.map(ObjectCreationExpr::getType);
        if (classOrInterfaceType.isEmpty()) {
            return false;
        }

        final var declaration = javaFiles.stream()
                .filter(f -> f.getFileNameWithoutExtension().equals(classOrInterfaceType.map(NodeWithSimpleName::getNameAsString).orElse("")))
                .map(JavaFile::getCompilationUnit)
                .map(AstHandler::getClassOrInterfaceDeclaration)
                .flatMap(Optional::stream)
                .findFirst();


        return declaration.isPresent() && (declaration.get().getExtendedTypes().stream().anyMatch(t -> t.equals(type))
                || declaration.get().getImplementedTypes().stream().anyMatch(t -> t.equals(type)));
    }

    private boolean isParameterUsedInIfStmtConditional(Parameter parameter, Optional<BinaryExpr> binaryExpr,
                                                       ObjectCreationExpr methodCallExpr) {

        if (binaryExpr.isPresent()) {
            final String name2 = AstHandler.getNameExpr(binaryExpr.get()).map(NodeWithSimpleName::getNameAsString).orElse("");

            return parameter.getNameAsString().equals(name2)
                    && BinaryExpr.Operator.EQUALS.equals(binaryExpr.get().getOperator());
        } else if (methodCallExpr != null) {

            final var hasParam = methodCallExpr.getChildNodes()
                    .stream()
                    .filter(NameExpr.class::isInstance)
                    .map(NameExpr.class::cast)
                    .anyMatch(n -> n.getNameAsString().equals(parameter.getNameAsString()));
            final var isAnEqualsMethod = methodCallExpr.getChildNodes()
                    .stream()
                    .filter(SimpleName.class::isInstance)
                    .map(SimpleName.class::cast)
                    .anyMatch(n -> n.asString().equals(parameter.getNameAsString()));

            return hasParam && isAnEqualsMethod;
        }

        return false;
    }

    @Override
    protected WeiEtAl2014Candidate createCandidate(JavaFile file, MethodDeclaration method, Collection<IfStmt> ifStatements) {

        final var methodReturnType = AstHandler.getMethodReturnClassType(method).orElse(null);
        final var classOrInterface = AstHandler.getClassOrInterfaceDeclaration(file.getCompilationUnit()).orElseThrow(IllegalArgumentException::new);
        final var packageDeclaration = file.getCompilationUnit().getPackageDeclaration().orElseThrow(IllegalArgumentException::new);

        return new WeiEtAl2014FactoryCandidate(file, file.getCompilationUnit(), packageDeclaration, classOrInterface, method, methodReturnType, ifStatements);
    }

}
