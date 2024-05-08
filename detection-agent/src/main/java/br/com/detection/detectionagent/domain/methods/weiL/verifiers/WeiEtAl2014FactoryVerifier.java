package br.com.detection.detectionagent.domain.methods.weiL.verifiers;

import br.com.detection.detectionagent.domain.dataExtractions.ast.AstHandler;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014Candidate;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014FactoryCandidate;
import br.com.detection.detectionagent.file.JavaFile;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
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

        return baseType.filter(classOrInterfaceType -> !ifStatements.isEmpty()
                && ifStatements.stream()
                .allMatch(ifStmt -> ifStmtIsValid(javaFiles, classOrInterfaceType, method.getParameters(), ifStmt)))
                .isPresent();

    }

    private boolean ifStmtIsValid(List<JavaFile> javaFiles, ClassOrInterfaceType baseType, List<Parameter> parameter, IfStmt ifStmt) {
        final var binaryExpr = ifStmt.getChildNodes().stream()
                .filter(BinaryExpr.class::isInstance)
                .map(BinaryExpr.class::cast)
                .findFirst();

        final var objectCreationExpr = AstHandler.getObjectCreationExprList(ifStmt);
        final var hasReturn = AstHandler.getReturnStmt(ifStmt).isPresent();
        final var hasValidReturn = this.hasReturnTypeAndHasValidSubtype(javaFiles, baseType, objectCreationExpr);

        return ((binaryExpr.isPresent()
                || !objectCreationExpr.isEmpty())
                && isParameterUsedInIfStmtConditional(parameter, binaryExpr))
                && hasReturn
                && hasValidReturn;
    }

    private boolean hasReturnTypeAndHasValidSubtype(List<JavaFile> javaFiles, ClassOrInterfaceType baseType, List<ObjectCreationExpr> objectCreationExpr) {
        return this.isOfTypeOrIsSubtype(javaFiles, baseType, objectCreationExpr);
    }

    private boolean isOfTypeOrIsSubtype(List<JavaFile> javaFiles, ClassOrInterfaceType type, List<ObjectCreationExpr> objCreationExpr) {
        if (objCreationExpr.isEmpty()) {
            return false;
        }

        final var objCreationExprTypes = objCreationExpr.stream()
                .map(ObjectCreationExpr::getType)
                .map(NodeWithSimpleName::getNameAsString)
                .toList();
        final var parent = javaFiles.stream()
                .filter(f -> objCreationExprTypes.contains(f.getFileNameWithoutExtension()))
                .map(JavaFile::getCompilationUnit)
                .map(AstHandler::getClassOrInterfaceDeclaration)
                .flatMap(Optional::stream)
                .toList();
        final var isExtendedType = parent.stream()
                .map(ClassOrInterfaceDeclaration::getExtendedTypes)
                .flatMap(NodeList::stream)
                .anyMatch(t -> t.equals(type));
        final var isImplementedType = parent.stream()
                .map(ClassOrInterfaceDeclaration::getImplementedTypes)
                .flatMap(NodeList::stream)
                .anyMatch(t -> t.equals(type));

        return !parent.isEmpty() && (isExtendedType || isImplementedType);
    }

    private boolean isParameterUsedInIfStmtConditional(List<Parameter> parameter, Optional<BinaryExpr> binaryExpr) {
        if (binaryExpr.isEmpty()) {
            return false;
        }
        final var name = AstHandler.getNameExpr(binaryExpr.get())
                .map(NodeWithSimpleName::getNameAsString)
                .orElse("");

        return parameter.stream().anyMatch(param -> param.getNameAsString().equals(name))
                && BinaryExpr.Operator.EQUALS.equals(binaryExpr.get().getOperator());
    }

    @Override
    protected WeiEtAl2014Candidate createCandidate(JavaFile file, MethodDeclaration method, Collection<IfStmt> ifStatements) {

        final var methodReturnType = AstHandler.getMethodReturnClassType(method).orElse(null);
        final var classOrInterface = AstHandler.getClassOrInterfaceDeclaration(file.getCompilationUnit()).orElseThrow(IllegalArgumentException::new);
        final var packageDeclaration = file.getCompilationUnit().getPackageDeclaration().orElseThrow(IllegalArgumentException::new);

        return new WeiEtAl2014FactoryCandidate(file, file.getCompilationUnit(), packageDeclaration, classOrInterface, method, methodReturnType, ifStatements);
    }

}
