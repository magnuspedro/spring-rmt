package br.com.magnus.detectionandrefactoring.refactor.methods.weiL.executors;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AbstractSyntaxTree;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.weiL.WeiEtAl2014Candidate;
import br.com.magnus.detectionandrefactoring.refactor.methods.weiL.WeiEtAl2014FactoryCandidate;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Executor for Wei et al. 2014 Factory Method pattern refactoring.
 * <p>
 * Converts conditional object creation into factory methods by:
 * <ul>
 *   <li>Creating factory classes for each product type</li>
 *   <li>Making the base class abstract</li>
 *   <li>Moving creation logic to factory methods</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class WeiEtAl2014FactoryExecutor implements WeiEtAl2014Executor {

    @Override
    public void refactor(RefactorFiles refactorFiles) {
        Assert.notNull(refactorFiles, "RefactorFiles cannot be null");
        Assert.notEmpty(refactorFiles.candidates(), "Candidate cannot be null");
        Assert.notEmpty(refactorFiles.files(), "JavaFiles cannot be null");

        var weiCandidate = (WeiEtAl2014FactoryCandidate) refactorFiles.candidate();
        refactorFiles.addFileChanged(weiCandidate.getFile().getFullName());

        try {
            for (var ifStmt : weiCandidate.getIfStatements()) {
                refactorIfStatement(weiCandidate, ifStmt, refactorFiles);
            }
            makeBaseClassAbstract(weiCandidate, refactorFiles);
        } catch (Exception ex) {
            throw new WeiEtAl2014ExecutorException("Error Refactoring Factory Method", ex);
        }
    }

    /**
     * Refactors a single if-statement into a factory method.
     *
     * @param candidate     the factory candidate
     * @param ifStmt        the if statement to refactor
     * @param refactorFiles the refactor files container
     */
    private void refactorIfStatement(WeiEtAl2014FactoryCandidate candidate, IfStmt ifStmt, RefactorFiles refactorFiles) {
        var baseCu = findBaseCompilationUnit(refactorFiles, candidate);
        var classDecl = AstHandler.getClassOrInterfaceDeclaration(baseCu)
                .orElseThrow(() -> new IllegalStateException("Base class not found"));

        var createdClassName = extractCreatedClassName(ifStmt, refactorFiles);
        var factoryClassName = String.format("%sFactory", createdClassName);

        var factoryCu = createFactoryClass(factoryClassName, classDecl.getNameAsString(),
                candidate.getMethodDcl(), ifStmt, baseCu);

        addFactoryToFile(refactorFiles, factoryClassName, factoryCu, baseCu);
    }

    /**
     * Finds the base class compilation unit.
     *
     * @param refactorFiles the refactor files
     * @param candidate     the candidate
     * @return the base compilation unit
     */
    private CompilationUnit findBaseCompilationUnit(RefactorFiles refactorFiles, WeiEtAl2014FactoryCandidate candidate) {
        var allClasses = refactorFiles.files()
                .stream()
                .map(JavaFile::getCompilationUnit)
                .toList();

        return allClasses.stream()
                .filter(c -> AstHandler.doesCompilationUnitsMatch(c,
                        Optional.of(candidate.getClassDeclaration()),
                        Optional.of(candidate.getPackageDeclaration())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Base class not found"));
    }

    /**
     * Extracts the name of the class being created in the if-statement.
     *
     * @param ifStmt        the if statement
     * @param refactorFiles the refactor files
     * @return the class name
     */
    private String extractCreatedClassName(IfStmt ifStmt, RefactorFiles refactorFiles) {
        var returnNode = AstHandler.getReturnStmt(ifStmt)
                .map(Node::getChildNodes)
                .stream()
                .flatMap(Collection::stream)
                .findFirst();

        if (returnNode.filter(NameExpr.class::isInstance).isPresent()) {
            return extractClassNameFromVariable(returnNode.map(NameExpr.class::cast).get(), ifStmt, refactorFiles);
        }
        if (returnNode.filter(ObjectCreationExpr.class::isInstance).isPresent()) {
            var objCreationExpr = returnNode.map(ObjectCreationExpr.class::cast)
                    .orElseThrow(() -> new IllegalStateException("Object creation not found"));
            return extractClassNameFromObjectCreation(objCreationExpr, refactorFiles);
        }
        throw new IllegalStateException("Cannot determine created class name");
    }

    /**
     * Extracts class name from a variable that holds the created object.
     *
     * @param nameExpr      the name expression
     * @param ifStmt        the if statement
     * @param refactorFiles the refactor files
     * @return the class name
     */
    private String extractClassNameFromVariable(NameExpr nameExpr, IfStmt ifStmt, RefactorFiles refactorFiles) {
        var returnName = nameExpr.getNameAsString();
        var varDclr = AstHandler.getVariableDeclarationInNode(ifStmt.getThenStmt(), returnName);
        var objectCreationExpr = varDclr
                .flatMap(AstHandler::getObjectCreationExpr)
                .orElseThrow(() -> new IllegalStateException("Object creation not found"));

        return extractClassNameFromObjectCreation(objectCreationExpr, refactorFiles);
    }

    /**
     * Extracts class name from an object creation expression.
     *
     * @param objectCreationExpr the object creation expression
     * @param refactorFiles      the refactor files
     * @return the class name
     */
    private String extractClassNameFromObjectCreation(ObjectCreationExpr objectCreationExpr, RefactorFiles refactorFiles) {
        return refactorFiles.files().stream()
                .filter(f -> f.getFileNameWithoutExtension().equals(objectCreationExpr.getType().getNameAsString()))
                .map(f -> (CompilationUnit) f.getParsed())
                .map(AstHandler::getClassOrInterfaceDeclaration)
                .flatMap(Optional::stream)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Class declaration not found"));
    }

    /**
     * Creates a new factory class.
     *
     * @param factoryClassName  the factory class name
     * @param baseClassName     the base class name
     * @param methodDecl        the method to copy
     * @param ifStmt            the if statement with creation logic
     * @param baseCu            the base compilation unit
     * @return the new factory compilation unit
     */
    private CompilationUnit createFactoryClass(String factoryClassName, String baseClassName,
                                                MethodDeclaration methodDecl, IfStmt ifStmt, CompilationUnit baseCu) {
        var cu = new CompilationUnit();
        var factoryMethod = new MethodDeclaration();
        var factoryType = cu.addClass(factoryClassName);

        cu.setPackageDeclaration(baseCu.getPackageDeclaration()
                .orElseThrow(() -> new IllegalArgumentException("Package not found")));

        factoryMethod.setName(methodDecl.getName());
        factoryMethod.setType(methodDecl.getType());
        factoryMethod.setModifiers(NodeList.nodeList(Modifier.publicModifier()));
        factoryMethod.setBody((BlockStmt) ifStmt.getThenStmt());

        factoryType.addMember(factoryMethod);
        factoryType.addExtendedType(baseClassName);

        return cu;
    }

    /**
     * Adds the factory class to the refactor files.
     *
     * @param refactorFiles   the refactor files
     * @param factoryClassName the factory class name
     * @param factoryCu       the factory compilation unit
     * @param baseCu          the base compilation unit for path
     */
    private void addFactoryToFile(RefactorFiles refactorFiles, String factoryClassName,
                                 CompilationUnit factoryCu, CompilationUnit baseCu) {
        var path = refactorFiles.files().stream()
                .filter(f -> AstHandler.doesCompilationUnitsMatch(f.getCompilationUnit(), baseCu))
                .map(JavaFile::getPath)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Path not found"));

        refactorFiles.add(JavaFile.builder()
                .name(String.format("%s.java", factoryClassName))
                .originalClass(factoryCu.toString())
                .path(path)
                .parsed(AbstractSyntaxTree.parseSingle(factoryCu.toString()))
                .build());
    }

    /**
     * Makes the base class abstract and removes factory method body.
     *
     * @param candidate     the factory candidate
     * @param refactorFiles the refactor files
     */
    private void makeBaseClassAbstract(WeiEtAl2014FactoryCandidate candidate, RefactorFiles refactorFiles) {
        var baseCu = findBaseCompilationUnit(refactorFiles, candidate);
        var classDecl = AstHandler.getClassOrInterfaceDeclaration(baseCu)
                .orElseThrow(() -> new IllegalStateException("Base class not found"));

        var factoryMethod = findMethodInClass(baseCu, candidate.getMethodDcl());

        classDecl.setAbstract(true);
        factoryMethod.setBody(null);
        factoryMethod.setAbstract(true);
        factoryMethod.getParameters().clear();
    }

    /**
     * Finds a method in a class by name and parameters.
     *
     * @param cu         the compilation unit
     * @param methodDecl the method to find
     * @return the found method
     */
    private MethodDeclaration findMethodInClass(CompilationUnit cu, MethodDeclaration methodDecl) {
        return AstHandler.getMethods(cu)
                .stream()
                .filter(m -> m.getNameAsString().equals(methodDecl.getNameAsString())
                        && AstHandler.methodsParamsMatch(m, methodDecl))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Method not found"));
    }

    /**
     * Finds the index of the discriminator parameter.
     *
     * @param method       the factory method
     * @param ifStatements the if statements to analyze
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
        return candidate instanceof WeiEtAl2014FactoryCandidate
                && DesignPattern.FACTORY_METHOD.equals(candidate.getEligiblePattern());
    }
}
