package br.com.detection.detectionagent.domain.methods.weiL.executors;

import br.com.detection.detectionagent.domain.dataExtractions.ast.AstHandler;
import br.com.detection.detectionagent.domain.methods.weiL.LiteralValueExtractor;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014Candidate;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014StrategyCandidate;
import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.ExtractionMethod;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.patterns.DesignPattern;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.TypeParameter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WeiEtAl2014StrategyExecutor implements WeiEtAl2014Executor {

    @Override
    public void refactor(RefactoringCandidate candidate, List<JavaFile> dataHandler, ExtractionMethod extractionMethod) {
        final WeiEtAl2014StrategyCandidate weiCandidate = (WeiEtAl2014StrategyCandidate) candidate;
        extractionMethod.parseAll(dataHandler);

        try {
            var path = dataHandler.stream()
                    .filter(f -> AstHandler.doesCompilationUnitsMatch((CompilationUnit) f.getParsed(), weiCandidate.getCompilationUnit()))
                    .map(JavaFile::getPath)
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
            final var file = Path.of(path);

            final var method = new MethodDeclaration();
            method.setName(weiCandidate.getMethodDcl().getName());
            method.setType(weiCandidate.getMethodDcl().getType());
            method.setModifiers(EnumSet.of(Modifier.PUBLIC));
            method.setAbstract(true);

            weiCandidate.getVariables().forEach(v -> method.addParameter(v.getType(), v.getNameAsString()));

            final var strategyCu = new CompilationUnit();
            final var createdStrategy = strategyCu.addClass("Strategy");
            createdStrategy.addMember(method);
            createdStrategy.setAbstract(true);

            final var strategyFile = file.getParent().resolve("Strategy.java");

            writeChanges(strategyCu, strategyFile);

            int i = 1;
            for (var ifStmt : weiCandidate.getIfStatements()) {
                this.changesIfStmtCandidate(i++, file, weiCandidate, ifStmt, dataHandler);
            }

            changeBaseClazz(weiCandidate, createdStrategy, dataHandler);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private void changesIfStmtCandidate(int idx, Path file, WeiEtAl2014StrategyCandidate candidate, IfStmt ifStmt,
                                        List<JavaFile> dataHandler) throws IOException {

        final var concreteStrategyClassName = "ConcreteStrategy"
                .concat(this.getNameSuffix(idx, candidate.getMethodDcl(), ifStmt));

        final var cu = new CompilationUnit();
        final var type = cu.addClass(concreteStrategyClassName);

        final var method = new MethodDeclaration();
        method.setName(candidate.getMethodDcl().getName());
        method.setType(candidate.getMethodDcl().getType());
        method.setModifiers(EnumSet.of(Modifier.PUBLIC));
        method.setBody((BlockStmt) ifStmt.getThenStmt());
        candidate.getVariables().forEach(v -> method.addParameter(v.getType(), v.getNameAsString()));

        type.addMember(method);
        type.addExtendedType("Strategy");

        writeChanges(cu, file.getParent().resolve(String.format("%s.java", concreteStrategyClassName)));
    }

    private String getNameSuffix(int idx, MethodDeclaration method, IfStmt ifStmt) {

        final var parameter = method.getParameters()
                .stream()
                .findFirst()
                .get();

        final var binaryExpr = ifStmt.getChildNodes()
                .stream()
                .filter(BinaryExpr.class::isInstance)
                .map(BinaryExpr.class::cast)
                .findFirst();

        final var methodCallExpr = ifStmt.getChildNodes()
                .stream()
                .filter(MethodCallExpr.class::isInstance)
                .map(MethodCallExpr.class::cast)
                .findFirst();

        if (binaryExpr.isPresent()) {
            return new LiteralValueExtractor()
                    .getNodeOtherThan(binaryExpr.get(), parameter)
                    .map(Object::toString)
                    .orElse(String.valueOf(idx));
        } else if (methodCallExpr.isPresent()) {
            return new LiteralValueExtractor()
                    .getNodeOtherThan(methodCallExpr.get(), parameter)
                    .map(Object::toString)
                    .orElse(String.valueOf(idx));
        }

        throw new NotImplementedException("Conditional not covered yet");
    }

    private void changeBaseClazz(WeiEtAl2014StrategyCandidate candidate, ClassOrInterfaceDeclaration createdStrategy,
                                 List<JavaFile> dataHandler) {
        final var allClasses = dataHandler.stream().map(m -> (CompilationUnit) m.getParsed()).toList();
        final var baseCu = this.updateBaseCompilationUnit(allClasses, candidate);

        final var candidateMethod = AstHandler.getMethods(baseCu).stream()
                .filter(m -> m.getNameAsString().equals(candidate.getMethodDcl().getNameAsString())
                        && AstHandler.methodsParamsMatch(m, candidate.getMethodDcl()))
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        final var methodCall = new MethodCallExpr();
        methodCall.setName(candidateMethod.getNameAsString());
        methodCall.setScope(new NameExpr("strategy"));
        methodCall.setArguments(new NodeList<>(candidate.getVariables()
                .stream()
                .map(this::parseVariableToFieldAccess).collect(Collectors.toList()))
        );

        final var returnStmt = new ReturnStmt();
        returnStmt.setExpression(methodCall);

        final var block = new BlockStmt();
        block.addStatement(returnStmt);
        candidateMethod.setBody(block);
        candidateMethod.setParameter(0,
                new Parameter(new TypeParameter(createdStrategy.getNameAsString()), "strategy"));

        var path = dataHandler.stream()
                .filter(f -> AstHandler.doesCompilationUnitsMatch((CompilationUnit) f.getParsed(), baseCu))
                .map(JavaFile::getPath)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

        final Path file = Path.of(path);
//        writeCanges(baseCu, file);
    }

    private FieldAccessExpr parseVariableToFieldAccess(VariableDeclarator var) {
        final var fieldAccess = new FieldAccessExpr();
        fieldAccess.setScope(new ThisExpr());
        fieldAccess.setName(new SimpleName(var.getNameAsString()));
        return fieldAccess;
    }

    private CompilationUnit updateBaseCompilationUnit(Collection<CompilationUnit> allClasses,
                                                      WeiEtAl2014Candidate candidate) {
        return allClasses.stream()
                .filter(c -> AstHandler.doesCompilationUnitsMatch(c, Optional.of(candidate.getClassDeclaration()),
                        Optional.of(candidate.getPackageDeclaration())))
                .findFirst()
                .get();
    }

    @Override
    public boolean isApplicable(RefactoringCandidate candidate) {
        return candidate instanceof WeiEtAl2014StrategyCandidate
                && DesignPattern.STRATEGY.equals(candidate.getEligiblePattern());
    }

}
