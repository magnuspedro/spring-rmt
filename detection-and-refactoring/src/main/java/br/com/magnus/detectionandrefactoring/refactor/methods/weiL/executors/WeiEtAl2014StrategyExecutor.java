package br.com.magnus.detectionandrefactoring.refactor.methods.weiL.executors;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AbstractSyntaxTree;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.weiL.LiteralValueExtractor;
import br.com.magnus.detectionandrefactoring.refactor.methods.weiL.WeiEtAl2014Candidate;
import br.com.magnus.detectionandrefactoring.refactor.methods.weiL.WeiEtAl2014StrategyCandidate;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Executor for Wei et al. 2014 Strategy pattern refactoring.
 * <p>
 * Converts conditional logic into Strategy pattern by:
 * <ul>
 *   <li>Creating an abstract Strategy interface</li>
 *   <li>Creating concrete strategy implementations for each branch</li>
 *   <li>Moving creation logic to concrete strategies</li>
 *   <li>Updating base class to use strategy</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class WeiEtAl2014StrategyExecutor implements WeiEtAl2014Executor {

    @Override
    public void refactor(RefactorFiles refactorFiles) {
        Assert.notNull(refactorFiles, "RefactorFiles cannot be null");
        Assert.notEmpty(refactorFiles.candidates(), "Candidate cannot be null");
        Assert.notEmpty(refactorFiles.files(), "JavaFiles cannot be null");

        var weiCandidate = (WeiEtAl2014StrategyCandidate) refactorFiles.candidate();
        refactorFiles.addFileChanged(weiCandidate.getFile().getFullName());

        try {
            var path = findFilePath(refactorFiles, weiCandidate);
            var strategyFile = createAbstractStrategy(weiCandidate, path);
            refactorFiles.add(strategyFile);

            for (var i = 0; i < weiCandidate.getIfStatements().size(); i++) {
                createConcreteStrategy(i, weiCandidate.getIfStatements().get(i), strategyFile, weiCandidate, refactorFiles);
            }

            updateBaseClassWithStrategy(weiCandidate, strategyFile, refactorFiles);
        } catch (Exception ex) {
            throw new WeiEtAl2014ExecutorException("Error Refactoring Strategy Method", ex);
        }
    }

    /**
     * Finds the file path for the candidate.
     *
     * @param refactorFiles the refactor files
     * @param candidate     the candidate
     * @return the file path
     */
    private String findFilePath(RefactorFiles refactorFiles, WeiEtAl2014StrategyCandidate candidate) {
        return refactorFiles.files().stream()
                .filter(f -> AstHandler.doesCompilationUnitsMatch(f.getCompilationUnit(), candidate.getCompilationUnit()))
                .map(JavaFile::getPath)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("File path not found"));
    }

    /**
     * Creates the abstract Strategy class.
     *
     * @param candidate the strategy candidate
     * @param path     the file path
     * @return the strategy JavaFile
     */
    private JavaFile createAbstractStrategy(WeiEtAl2014StrategyCandidate candidate, String path) {
        var strategyCu = new CompilationUnit();
        var strategy = strategyCu.addClass("Strategy");
        strategy.setAbstract(true);

        var strategyMethod = new MethodDeclaration();
        strategyMethod.setName(candidate.getMethodDcl().getName());
        strategyMethod.setType(candidate.getMethodDcl().getType());
        strategyMethod.setModifiers(NodeList.nodeList(Modifier.publicModifier()));
        strategyMethod.setAbstract(true);

        candidate.getVariables().forEach(v -> strategyMethod.addParameter(v.getType(), v.getNameAsString()));
        strategy.addMember(strategyMethod);

        return JavaFile.builder()
                .name("Strategy.java")
                .path(path)
                .originalClass(strategyCu.toString())
                .parsed(AbstractSyntaxTree.parseSingle(strategyCu.toString()))
                .build();
    }

    /**
     * Creates a concrete strategy implementation.
     *
     * @param idx          the index for naming
     * @param ifStmt        the if statement with strategy logic
     * @param strategyFile  the abstract strategy file
     * @param candidate     the strategy candidate
     * @param refactorFiles the refactor files
     */
    private void createConcreteStrategy(int idx, IfStmt ifStmt, JavaFile strategyFile,
                                      WeiEtAl2014StrategyCandidate candidate, RefactorFiles refactorFiles) {
        var concreteStrategyClassName = buildConcreteStrategyName(idx, candidate.getMethodDcl(), ifStmt);

        var cu = new CompilationUnit();
        var concreteStrategy = cu.addClass(concreteStrategyClassName);

        var strategyMethod = buildStrategyMethod(candidate, ifStmt);
        concreteStrategy.addMember(strategyMethod);
        concreteStrategy.addExtendedType("Strategy");

        refactorFiles.add(JavaFile.builder()
                .name(String.format("%s.java", concreteStrategyClassName))
                .path(strategyFile.getPath())
                .originalClass(cu.toString())
                .parsed(AbstractSyntaxTree.parseSingle(cu.toString()))
                .build());
    }

    /**
     * Builds the concrete strategy class name.
     *
     * @param idx              the index
     * @param methodDeclaration the method declaration
     * @param ifStmt           the if statement
     * @return the class name
     */
    private String buildConcreteStrategyName(int idx, MethodDeclaration methodDeclaration, IfStmt ifStmt) {
        var suffix = extractLiteralValue(ifStmt, methodDeclaration)
                .map(String::valueOf)
                .orElse(String.valueOf(idx));
        return "ConcreteStrategy" + suffix;
    }

    /**
     * Extracts a literal value from the if condition for naming.
     *
     * @param ifStmt           the if statement
     * @param methodDeclaration the method declaration
     * @return Optional containing the extracted value
     */
    private Optional<Object> extractLiteralValue(IfStmt ifStmt, MethodDeclaration methodDeclaration) {
        return ifStmt.getChildNodes()
                .stream()
                .filter(BinaryExpr.class::isInstance)
                .map(BinaryExpr.class::cast)
                .findFirst()
                .map(m -> LiteralValueExtractor.extractValidLiteralFromNode(m, methodDeclaration))
                .flatMap(Optional::stream)
                .findFirst();
    }

    /**
     * Builds a strategy method from the if statement.
     *
     * @param candidate the strategy candidate
     * @param ifStmt    the if statement
     * @return the method declaration
     */
    private MethodDeclaration buildStrategyMethod(WeiEtAl2014StrategyCandidate candidate, IfStmt ifStmt) {
        var method = new MethodDeclaration();
        method.setName(candidate.getMethodDcl().getName());
        method.setType(candidate.getMethodDcl().getType());
        method.setModifiers(NodeList.nodeList(Modifier.publicModifier()));
        method.setBody((BlockStmt) ifStmt.getThenStmt());
        candidate.getVariables().forEach(v -> method.addParameter(v.getType(), v.getNameAsString()));
        return method;
    }

    /**
     * Updates the base class to use the strategy.
     *
     * @param candidate     the strategy candidate
     * @param strategyFile  the strategy file containing the class name
     * @param refactorFiles the refactor files
     */
    private void updateBaseClassWithStrategy(WeiEtAl2014StrategyCandidate candidate,
                                             JavaFile strategyFile, RefactorFiles refactorFiles) {
        var baseCu = findBaseCompilationUnit(refactorFiles, candidate);
        var candidateMethod = findMethodInClass(baseCu, candidate.getMethodDcl());

        var strategyMethodCall = buildStrategyMethodCall(candidateMethod, candidate.getVariables());
        var returnStmt = new ReturnStmt(strategyMethodCall);
        var block = new BlockStmt();
        block.addStatement(returnStmt);

        candidateMethod.setBody(block);

        var discriminatorIndex = findDiscriminatorParameterIndex(candidate.getMethodDcl(), candidate.getIfStatements());
        var strategyParam = new Parameter(new ClassOrInterfaceType("Strategy"), "strategy");
        candidateMethod.setParameter(discriminatorIndex, strategyParam);
    }

    /**
     * Finds the base class compilation unit.
     *
     * @param refactorFiles the refactor files
     * @param candidate     the candidate
     * @return the base compilation unit
     */
    private CompilationUnit findBaseCompilationUnit(RefactorFiles refactorFiles, WeiEtAl2014Candidate candidate) {
        var allClasses = refactorFiles.files().stream()
                .map(f -> (CompilationUnit) f.getParsed())
                .toList();

        return allClasses.stream()
                .filter(c -> AstHandler.doesCompilationUnitsMatch(c,
                        Optional.of(candidate.getClassDeclaration()),
                        Optional.of(candidate.getPackageDeclaration())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Base class not found"));
    }

    /**
     * Finds a method in a class by name and parameters.
     *
     * @param cu    the compilation unit
     * @param method the method to find
     * @return the found method
     */
    private MethodDeclaration findMethodInClass(CompilationUnit cu, MethodDeclaration method) {
        return AstHandler.getMethods(cu).stream()
                .filter(m -> m.getNameAsString().equals(method.getNameAsString())
                        && AstHandler.methodsParamsMatch(m, method))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Method not found"));
    }

    /**
     * Builds the strategy method call.
     *
     * @param method    the original method
     * @param variables the variables to convert to field accesses
     * @return the method call expression
     */
    private MethodCallExpr buildStrategyMethodCall(MethodDeclaration method, Collection<VariableDeclarator> variables) {
        var methodCall = new MethodCallExpr();
        methodCall.setName(method.getNameAsString());
        methodCall.setScope(new NameExpr("strategy"));
        methodCall.setArguments(new NodeList<>(variables.stream()
                .map(this::convertToFieldAccess)
                .collect(Collectors.toList())));
        return methodCall;
    }

    /**
     * Converts a variable to a field access expression.
     *
     * @param variable the variable
     * @return the field access expression
     */
    private FieldAccessExpr convertToFieldAccess(VariableDeclarator variable) {
        var fieldAccess = new FieldAccessExpr();
        fieldAccess.setScope(new ThisExpr());
        fieldAccess.setName(new SimpleName(variable.getNameAsString()));
        return fieldAccess;
    }

    /**
     * Finds the index of the discriminator parameter.
     *
     * @param method       the method
     * @param ifStatements the if statements
     * @return the parameter index
     */
    private int findDiscriminatorParameterIndex(MethodDeclaration method, Collection<IfStmt> ifStatements) {
        var params = method.getParameters();
        if (params.isEmpty()) {
            return 0;
        }
        return ifStatements.stream()
                .findFirst()
                .map(ifStmt -> findParameterUsedInCondition(params, ifStmt))
                .orElse(0);
    }

    /**
     * Finds which parameter is used in the if condition.
     *
     * @param params the method parameters
     * @param ifStmt the if statement
     * @return the parameter index
     */
    private int findParameterUsedInCondition(NodeList<?> params, IfStmt ifStmt) {
        var binaryExpr = ifStmt.getChildNodes().stream()
                .filter(BinaryExpr.class::isInstance)
                .map(BinaryExpr.class::cast)
                .findFirst();
        var methodCallExpr = ifStmt.getChildNodes().stream()
                .filter(MethodCallExpr.class::isInstance)
                .map(MethodCallExpr.class::cast)
                .findFirst();

        return IntStream.range(0, params.size())
                .filter(i -> isNameInExpression(params.get(i).getNameAsString(), binaryExpr, methodCallExpr))
                .findFirst()
                .orElse(0);
    }

    /**
     * Checks if a name is used in an expression.
     *
     * @param name         the name to search for
     * @param binaryExpr   optional binary expression
     * @param methodCallExpr optional method call expression
     * @return true if name is found
     */
    private boolean isNameInExpression(String name, Optional<BinaryExpr> binaryExpr,
                                     Optional<MethodCallExpr> methodCallExpr) {
        if (binaryExpr.isPresent()) {
            return binaryExpr.get().stream()
                    .anyMatch(n -> AstHandler.getNameExpr(n)
                            .map(NameExpr::getNameAsString)
                            .map(name::equals)
                            .orElse(false));
        }
        return methodCallExpr
                .map(mc -> mc.stream()
                        .anyMatch(n -> AstHandler.getNameExpr(n)
                                .map(NameExpr::getNameAsString)
                                .map(name::equals)
                                .orElse(false)))
                .orElse(false);
    }

    @Override
    public boolean isApplicable(RefactoringCandidate candidate) {
        return candidate instanceof WeiEtAl2014StrategyCandidate
                && DesignPattern.STRATEGY.equals(candidate.getEligiblePattern());
    }
}
