package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ExtractionMethodFactory;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AbstractSyntaxTreeExtraction;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Refactoring engine for Cinneide et al. 2000 design patterns.
 * <p>
 * Detects and refactors five design patterns:
 * <ul>
 *   <li>Factory Method - Extract creation logic to a factory method</li>
 *   <li>Singleton - Ensure single instance with lazy initialization</li>
 *   <li>Abstract Factory - Create families of related objects</li>
 *   <li>Strategy - Encaps interchangeable algorithms</li>
 *   <li>Bridge - Separate abstraction from implementation</li>
 * </ul>
 * <p>
 * Based on: "Design Patterns Refactoring using Clone-Based Co-evolution" by Cinneide et al. (2000)
 */
@Component
@RequiredArgsConstructor
public class CinneideEtAl2000 implements AbstractSyntaxTreeExtraction {

    private final ExtractionMethodFactory extractionMethodFactory;
    private final CinneideRefactoringTool cinneideRefactoringTool;

    /**
     * The design patterns supported by this refactoring engine.
     */
    @Getter
    private final Set<DesignPattern> designPatterns = Set.of(
            DesignPattern.FACTORY_METHOD,
            DesignPattern.SINGLETON,
            DesignPattern.ABSTRACT_FACTORY,
            DesignPattern.STRATEGY,
            DesignPattern.BRIDGE
    );

    /**
     * Extracts refactoring candidates for all supported Cinneide patterns.
     *
     * @param javaFiles the Java files to analyze
     * @return list of refactoring candidates
     */
    public List<RefactoringCandidate> extractCandidates(List<JavaFile> javaFiles) {
        extractionMethodFactory.build(this).parseAll(javaFiles);

        var classes = getClassDeclarations(javaFiles);
        var classByName = classes.stream()
                .collect(Collectors.toMap(ClassOrInterfaceDeclaration::getNameAsString, Function.identity(), (left, right) -> left));

        var candidates = new ArrayList<CinneideCandidate>();
        findFactoryMethodCandidate(classes, classByName).ifPresent(candidates::add);
        findSingletonCandidate(classes, classByName).ifPresent(candidates::add);
        findAbstractFactoryCandidate(classes, classByName).ifPresent(candidates::add);
        findStrategyCandidate(classes).ifPresent(candidates::add);
        findBridgeCandidate(classes).ifPresent(candidates::add);

        return candidates.stream()
                .map(RefactoringCandidate.class::cast)
                .toList();
    }

    /**
     * Refactors the provided files according to the Cinneide pattern.
     *
     * @param refactorFiles the files to refactor
     */
    public void refactor(RefactorFiles refactorFiles) {
        var candidate = (CinneideCandidate) refactorFiles.candidate();
        var previousByFile = refactorFiles.files().stream()
                .collect(Collectors.toMap(JavaFile::getFullName, file -> file.getCompilationUnit().toString(), (left, right) -> left));

        switch (candidate.getEligiblePattern()) {
            case FACTORY_METHOD -> cinneideRefactoringTool.applyFactoryMethod(
                    refactorFiles.files(),
                    candidate.getCreatorClass(),
                    candidate.getProductClass(),
                    candidate.getProductInterface(),
                    candidate.getAbstractCreator(),
                    candidate.getCreateMethodName()
            );
            case SINGLETON -> cinneideRefactoringTool.applySingleton(
                    refactorFiles.files(),
                    candidate.getConcreteSingletonClass(),
                    candidate.getNewAbstractSingletonClass()
            );
            case ABSTRACT_FACTORY -> cinneideRefactoringTool.applyAbstractFactory(
                    refactorFiles.files(),
                    candidate.getProductClasses(),
                    candidate.getNewFactoryName(),
                    candidate.getNewAbstractFactoryName()
            );
            case STRATEGY -> cinneideRefactoringTool.applyStrategy(
                    refactorFiles.files(),
                    candidate.getContextClass(),
                    candidate.getStrategyMethods(),
                    candidate.getStrategyClass()
            );
            case BRIDGE -> cinneideRefactoringTool.applyBridge(
                    refactorFiles.files(),
                    candidate.getClientClasses(),
                    candidate.getInterfaceName(),
                    candidate.getBridgeClassName()
            );
            default -> throw new IllegalArgumentException("Unsupported Cinneide pattern: " + candidate.getEligiblePattern());
        }

        refactorFiles.files().forEach(file -> {
            var previous = previousByFile.get(file.getFullName());
            if (previous == null || !previous.equals(file.getCompilationUnit().toString())) {
                refactorFiles.addFileChanged(file.getFullName());
            }
        });
    }

    /**
     * Finds Factory Method pattern candidates.
     * <p>
     * Identifies methods that create and return instances of a specific type.
     *
     * @param classes     all class declarations
     * @param classByName index of classes by name
     * @return optional candidate
     */
    private Optional<CinneideCandidate> findFactoryMethodCandidate(List<ClassOrInterfaceDeclaration> classes,
                                                                   Map<String, ClassOrInterfaceDeclaration> classByName) {
        return classes.stream()
                .filter(classDcl -> !classDcl.isInterface())
                .flatMap(classDcl -> AstHandler.getMethods(classDcl).stream()
                        .filter(method -> !method.isConstructorDeclaration())
                        .filter(method -> method.getType().isClassOrInterfaceType())
                        .filter(method -> hasObjectCreationOfType(method, method.getType().asClassOrInterfaceType().getNameAsString()))
                        .map(method -> Map.entry(classDcl, method)))
                .filter(entry -> classByName.containsKey(entry.getValue().getType().asClassOrInterfaceType().getNameAsString()))
                .map(entry -> {
                    var creator = entry.getKey().getNameAsString();
                    var product = entry.getValue().getType().asClassOrInterfaceType().getNameAsString();
                    return CinneideCandidate.builder()
                            .pkg(getPackageName(entry.getKey()))
                            .className(creator)
                            .eligiblePattern(DesignPattern.FACTORY_METHOD)
                            .creatorClass(creator)
                            .productClass(product)
                            .productInterface(product + "Interface")
                            .abstractCreator("Abstract" + creator)
                            .createMethodName(entry.getValue().getNameAsString())
                            .build();
                })
                .findFirst();
    }

    /**
     * Finds Singleton pattern candidates.
     * <p>
     * Identifies classes that are instantiated via new keyword in the codebase.
     *
     * @param classes     all class declarations
     * @param classByName index of classes by name
     * @return optional candidate
     */
    private Optional<CinneideCandidate> findSingletonCandidate(List<ClassOrInterfaceDeclaration> classes,
                                                               Map<String, ClassOrInterfaceDeclaration> classByName) {
        return classes.stream()
                .filter(classDcl -> !classDcl.isInterface())
                .flatMap(classDcl -> classDcl.findAll(ObjectCreationExpr.class).stream())
                .map(ObjectCreationExpr::getType)
                .map(ClassOrInterfaceType::getNameAsString)
                .filter(classByName::containsKey)
                .findFirst()
                .map(target -> CinneideCandidate.builder()
                        .pkg(getPackageName(classByName.get(target)))
                        .className(target)
                        .eligiblePattern(DesignPattern.SINGLETON)
                        .concreteSingletonClass(target)
                        .newAbstractSingletonClass("Abstract" + target)
                        .build());
    }

    /**
     * Finds Abstract Factory pattern candidates.
     * <p>
     * Identifies classes that create two or more different types of objects.
     *
     * @param classes     all class declarations
     * @param classByName index of classes by name
     * @return optional candidate
     */
    private Optional<CinneideCandidate> findAbstractFactoryCandidate(List<ClassOrInterfaceDeclaration> classes,
                                                                     Map<String, ClassOrInterfaceDeclaration> classByName) {
        return classes.stream()
                .filter(classDcl -> !classDcl.isInterface())
                .map(classDcl -> Map.entry(classDcl,
                        classDcl.findAll(ObjectCreationExpr.class).stream()
                                .map(ObjectCreationExpr::getType)
                                .map(ClassOrInterfaceType::getNameAsString)
                                .filter(classByName::containsKey)
                                .collect(Collectors.toCollection(LinkedHashSet::new))))
                .filter(entry -> entry.getValue().size() >= 2)
                .map(entry -> CinneideCandidate.builder()
                        .pkg(getPackageName(entry.getKey()))
                        .className(entry.getKey().getNameAsString())
                        .eligiblePattern(DesignPattern.ABSTRACT_FACTORY)
                        .productClasses(entry.getValue())
                        .newFactoryName(entry.getKey().getNameAsString() + "Factory")
                        .newAbstractFactoryName("Abstract" + entry.getKey().getNameAsString() + "Factory")
                        .build())
                .findFirst();
    }

    /**
     * Finds Strategy pattern candidates.
     * <p>
     * Identifies classes with multiple public methods that could be extracted as strategies.
     *
     * @param classes all class declarations
     * @return optional candidate
     */
    private Optional<CinneideCandidate> findStrategyCandidate(List<ClassOrInterfaceDeclaration> classes) {
        return classes.stream()
                .filter(classDcl -> !classDcl.isInterface())
                .map(classDcl -> Map.entry(classDcl,
                        AstHandler.getMethods(classDcl).stream()
                                .filter(MethodDeclaration::isPublic)
                                .filter(method -> !method.isStatic())
                                .filter(method -> !method.isConstructorDeclaration())
                                .map(MethodDeclaration::getNameAsString)
                                .filter(methodName -> !methodName.startsWith("get") && !methodName.startsWith("set"))
                                .collect(Collectors.toCollection(LinkedHashSet::new))))
                .filter(entry -> entry.getValue().size() >= 2)
                .map(entry -> {
                    var contextClass = entry.getKey().getNameAsString();
                    var selectedMethods = entry.getValue().stream().limit(2).collect(Collectors.toCollection(LinkedHashSet::new));
                    return CinneideCandidate.builder()
                            .pkg(getPackageName(entry.getKey()))
                            .className(contextClass)
                            .eligiblePattern(DesignPattern.STRATEGY)
                            .contextClass(contextClass)
                            .strategyMethods(selectedMethods)
                            .strategyClass(contextClass + "Strategy")
                            .build();
                })
                .findFirst();
    }

    /**
     * Finds Bridge pattern candidates.
     * <p>
     * Identifies interfaces with implementations that are directly instantiated by clients.
     *
     * @param classes all class declarations
     * @return optional candidate
     */
    private Optional<CinneideCandidate> findBridgeCandidate(List<ClassOrInterfaceDeclaration> classes) {
        for (var interfaceDcl : classes) {
            if (!interfaceDcl.isInterface()) {
                continue;
            }
            var interfaceName = interfaceDcl.getNameAsString();
            var concreteImplementation = classes.stream()
                    .filter(classDcl -> !classDcl.isInterface())
                    .filter(classDcl -> classDcl.getImplementedTypes().stream().anyMatch(type -> type.getNameAsString().equals(interfaceName)))
                    .map(ClassOrInterfaceDeclaration::getNameAsString)
                    .findFirst()
                    .orElse(null);

            if (concreteImplementation == null) {
                continue;
            }

            var clientClasses = classes.stream()
                    .filter(classDcl -> !classDcl.isInterface())
                    .filter(classDcl -> classDcl.findAll(ObjectCreationExpr.class).stream()
                            .anyMatch(creation -> creation.getType().getNameAsString().equals(concreteImplementation)))
                    .map(ClassOrInterfaceDeclaration::getNameAsString)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (clientClasses.isEmpty()) {
                continue;
            }

            return Optional.of(CinneideCandidate.builder()
                    .pkg(getPackageName(interfaceDcl))
                    .className(interfaceName)
                    .eligiblePattern(DesignPattern.BRIDGE)
                    .interfaceName(interfaceName)
                    .bridgeClassName(interfaceName + "Bridge")
                    .clientClasses(clientClasses)
                    .build());
        }
        return Optional.empty();
    }

    /**
     * Extracts all class declarations from Java files.
     *
     * @param javaFiles the Java files to process
     * @return list of class declarations
     */
    private List<ClassOrInterfaceDeclaration> getClassDeclarations(List<JavaFile> javaFiles) {
        return javaFiles.stream()
                .map(JavaFile::getCompilationUnit)
                .map(AstHandler::getClassOrInterfaceDeclaration)
                .flatMap(Optional::stream)
                .toList();
    }

    /**
     * Checks if a method creates objects of a specific type.
     *
     * @param method  the method to check
     * @param typeName the type name
     * @return true if method creates objects of the type
     */
    private boolean hasObjectCreationOfType(MethodDeclaration method, String typeName) {
        return method.findAll(ObjectCreationExpr.class).stream()
                .anyMatch(objectCreationExpr -> objectCreationExpr.getType().getNameAsString().equals(typeName));
    }

    /**
     * Gets the package name of a class declaration.
     *
     * @param classDcl the class declaration
     * @return the package name or empty string if not found
     */
    private String getPackageName(ClassOrInterfaceDeclaration classDcl) {
        return classDcl.findCompilationUnit()
                .flatMap(compilationUnit -> compilationUnit.getPackageDeclaration().map(packageDeclaration -> packageDeclaration.getNameAsString()))
                .orElse("");
    }
}
