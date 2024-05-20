package br.com.magnus.detection.refactor.methods.weiL.executors;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.detection.refactor.dataExtractions.ast.AbstractSyntaxTree;
import br.com.magnus.detection.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detection.refactor.methods.weiL.LiteralValueExtractor;
import br.com.magnus.detection.refactor.methods.weiL.WeiEtAl2014Candidate;
import br.com.magnus.detection.refactor.methods.weiL.WeiEtAl2014StrategyCandidate;
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
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WeiEtAl2014StrategyExecutor implements WeiEtAl2014Executor {

    @Override
    public void refactor(RefactorFiles refactorFiles) {
        Assert.notNull(refactorFiles, "RefactorFiles cannot be null");
        Assert.notEmpty(refactorFiles.candidates(), "Candidate cannot be null");
        Assert.notEmpty(refactorFiles.files(), "JavaFiles cannot be null");
        final var weiCandidate = (WeiEtAl2014StrategyCandidate) refactorFiles.candidate();
        refactorFiles.addFileChanged(weiCandidate.getFile().getFullName());

        try {
            var path = refactorFiles.files().stream()
                    .filter(f -> AstHandler.doesCompilationUnitsMatch(f.getCompilationUnit(), weiCandidate.getCompilationUnit()))
                    .map(JavaFile::getPath)
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);

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

            var strategyFile = JavaFile.builder()
                    .name("Strategy.java")
                    .path(path)
                    .originalClass(strategyCu.toString())
                    .parsed(AbstractSyntaxTree.parseSingle(strategyCu.toString()))
                    .build();

            refactorFiles.add(strategyFile);
            for (var i = 0; i < weiCandidate.getIfStatements().size(); i++) {
                this.changesIfStmtCandidate(i, refactorFiles, strategyFile, weiCandidate, weiCandidate.getIfStatements().get(i));
            }

            changeBaseClazz(weiCandidate, createdStrategy, refactorFiles);
        } catch (Exception ex) {
            throw new WeiEtAl2014ExecutorException("Error Refactoring Strategy Method", ex);
        }
    }

    private void changesIfStmtCandidate(int idx, RefactorFiles files, JavaFile file, WeiEtAl2014StrategyCandidate candidate, IfStmt ifStmt) {

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

        files.add(JavaFile.builder()
                .name(String.format("%s.java", concreteStrategyClassName))
                .path(file.getPath())
                .originalClass(cu.toString())
                .parsed(AbstractSyntaxTree.parseSingle(cu.toString()))
                .build());
    }

    private String getNameSuffix(int idx, MethodDeclaration methodDeclaration, IfStmt ifStmt) {
        return String.valueOf(ifStmt.getChildNodes()
                .stream()
                .filter(BinaryExpr.class::isInstance)
                .map(BinaryExpr.class::cast)
                .map(m -> LiteralValueExtractor.extractValidLiteralFromNode(m, methodDeclaration))
                .flatMap(Optional::stream)
                .findFirst()
                .orElse(idx));
    }

    private void changeBaseClazz(WeiEtAl2014StrategyCandidate candidate, ClassOrInterfaceDeclaration createdStrategy, RefactorFiles refactorFiles) {
        final var allClasses = refactorFiles.files().stream().map(m -> (CompilationUnit) m.getParsed()).toList();
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
        final var block = new BlockStmt();

        returnStmt.setExpression(methodCall);
        block.addStatement(returnStmt);
        candidateMethod.setBody(block);
        candidateMethod.setParameter(0, new Parameter(new TypeParameter(createdStrategy.getNameAsString()), "strategy"));
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
                .orElseThrow(IllegalArgumentException::new);

    }

    @Override
    public boolean isApplicable(RefactoringCandidate candidate) {
        return candidate instanceof WeiEtAl2014StrategyCandidate
                && DesignPattern.STRATEGY.equals(candidate.getEligiblePattern());
    }
}
