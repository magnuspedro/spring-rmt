package br.com.detection.detectionagent.domain.methods.weiL.verifiers;

import br.com.detection.detectionagent.domain.dataExtractions.ast.AstHandler;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014Candidate;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014StrategyCandidate;
import br.com.magnus.config.starter.file.JavaFile;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.IfStmt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Component
public class WeiEtAl2014StrategyVerifier extends WeiEtAl2014Verifier {

    protected boolean areIfStmtsValid(List<JavaFile> dataHandler, JavaFile file, MethodDeclaration method, Collection<IfStmt> ifStatements) {
        return !ifStatements.isEmpty()
                && ifStatements.stream()
                .allMatch(s -> isIfStmtValid(file, method, s));
    }

    private boolean isIfStmtValid(JavaFile file, MethodDeclaration method, IfStmt ifStmt) {
        if (AstHandler.getReturnStmt(ifStmt).isEmpty()) {
            return false;
        }

        final var binaryExpr = ifStmt.getChildNodes()
                .stream()
                .filter(BinaryExpr.class::isInstance)
                .map(BinaryExpr.class::cast)
                .findFirst();

        final var variables = this.classVariablesUsedInItsBody(file, ifStmt);

        return binaryExpr.isPresent()
                && this.isParameterUsedInIfStmtConditional(method.getParameters(), binaryExpr.get())
                && variables.size() == 1 && this.usesNoMethodInnerVariables(method, ifStmt);
    }

    private boolean usesNoMethodInnerVariables(MethodDeclaration method, IfStmt ifStmt) {
        return method.getChildNodes()
                .stream()
                .filter(c -> !(c instanceof IfStmt))
                .flatMap(c -> AstHandler.getVariableDeclarations(c).stream())
                .noneMatch(v -> ifStmt.getThenStmt()
                        .getChildNodes()
                        .stream()
                        .anyMatch(c -> AstHandler.doesNodeUsesVar(c, v))
                );
    }

    private Collection<VariableDeclarator> classVariablesUsedInItsBody(JavaFile file, IfStmt ifStmt) {
        final var classOrInterface = AstHandler.getClassOrInterfaceDeclaration(file.getCompilationUnit())
                .orElseThrow(IllegalArgumentException::new);

        return AstHandler.getDeclaredFields(classOrInterface)
                .stream()
                .flatMap(f -> f.getVariables().stream())
                .filter(var -> AstHandler.doesNodeUsesVar(ifStmt, var))
                .toList();
    }

    private boolean isParameterUsedInIfStmtConditional(List<Parameter> parameter, BinaryExpr binaryExpr) {
        return binaryExpr.stream()
                .anyMatch(expr -> AstHandler.getNameExpr(expr)
                        .map(NodeWithSimpleName::getNameAsString)
                        .stream()
                        .anyMatch(name -> parameter.stream()
                                .map(Parameter::getNameAsString)
                                .anyMatch(name::equals)))
                && isAnEqualsExpression(binaryExpr);
    }

    private boolean isAnEqualsExpression(BinaryExpr binaryExpr) {
        return binaryExpr.getOperator().equals(BinaryExpr.Operator.EQUALS);
    }

    @Override
    protected WeiEtAl2014Candidate createCandidate(JavaFile file, MethodDeclaration method, Collection<IfStmt> ifStatements) {

        final var variables = new ArrayList<VariableDeclarator>();
        final var classOrInterface = AstHandler.getClassOrInterfaceDeclaration(file.getCompilationUnit()).orElseThrow(IllegalArgumentException::new);
        final var packageDeclaration = file.getCompilationUnit().getPackageDeclaration().orElseThrow(IllegalArgumentException::new);
        final Function<VariableDeclarator, Boolean> isVariableRegistered = (var) -> variables.stream()
                .anyMatch(v -> v.getNameAsString().equals(var.getNameAsString()));

        ifStatements.stream()
                .flatMap(s -> this.classVariablesUsedInItsBody(file, s).stream())
                .forEach(v -> {
                    if (!isVariableRegistered.apply(v)) {
                        variables.add(v);
                    }
                });

        return new WeiEtAl2014StrategyCandidate(file, file.getCompilationUnit(), packageDeclaration, classOrInterface, method, ifStatements, variables);
    }
}
