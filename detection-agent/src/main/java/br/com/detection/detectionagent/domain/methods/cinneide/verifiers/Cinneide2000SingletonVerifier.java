package br.com.detection.detectionagent.domain.methods.cinneide.verifiers;

import br.com.detection.detectionagent.domain.methods.cinneide.Cinneide2000Candidate;
import br.com.detection.detectionagent.domain.methods.cinneide.Cinneide2000SingletonCanditate;
import br.com.detection.detectionagent.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.detectors.methods.Reference;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Cinneide2000SingletonVerifier extends Cinneide2000Verifier {

    public static boolean hasOneConstructor(CompilationUnit cu) {
        return cu.findAll(ConstructorDeclaration.class).size() <= 1;
    }

    @Override
    protected Cinneide2000Candidate createCandidate(Reference reference, Path file, CompilationUnit parsedClazz,
                                                    PackageDeclaration pkgDcl, ClassOrInterfaceDeclaration classOrInterface, MethodDeclaration method) {

        final ClassOrInterfaceType methodReturnType = this.astHandler.getReturnTypeClassOrInterfaceDeclaration(method)
                .orElse(null);

        return new Cinneide2000SingletonCanditate(reference, file, parsedClazz, pkgDcl, classOrInterface, method,
                methodReturnType);
    }

    @Override
    protected boolean instancesAreValid(DataHandler dataHandler, CompilationUnit parsedClazz,
                                        ClassOrInterfaceDeclaration classOrInterface) {
        final Collection<ObjectCreationExpr> uniqueInstance = getUnique(getInstance(parsedClazz, dataHandler));
        return !uniqueInstance.isEmpty() && hasOneConstructor(parsedClazz);
    }

    public Collection<ObjectCreationExpr> getUnique(Collection<ObjectCreationExpr> instances) {
        try {
            HashSet<Type> instancesSet = new HashSet<>();
            List<ObjectCreationExpr> repeatedInstances = new ArrayList<>();
            for (ObjectCreationExpr instance : instances) {
                if (!instancesSet.add(instance.getType())) {
                    repeatedInstances.add(instance);
                }
            }
            instances.removeAll(repeatedInstances);
        } catch (Exception e) {
            instances.clear();
        }
        return instances;
    }

    public Collection<ObjectCreationExpr> getInstance(CompilationUnit cu, DataHandler dataHandler) {
        final List<ObjectCreationExpr> instance = new ArrayList<>();

        try {
            cu.findAll(ObjectCreationExpr.class).stream().forEach(i -> {
                Type element = i.getType();
                if (element.isReferenceType() && dataHandler.getParsedFileByName(i.getType().toString()) != null
                        && !i.getType().toString().contains("Exception")) {
                    instance.add(i);
                }
            });
        } catch (Exception e) {
            instance.clear();
        }
        return instance;
    }

    @Override
    protected List<CompilationUnit> validInstances(DataHandler dataHandler, CompilationUnit parsedClazz,
                                                   ClassOrInterfaceDeclaration classOrInterface) {
        final List<ObjectCreationExpr> uniqueInstance = (List<ObjectCreationExpr>) getUnique(
                getInstance(parsedClazz, dataHandler));
        final List<CompilationUnit> clazzes = new ArrayList<>();

        for (ObjectCreationExpr ui : uniqueInstance) {
            clazzes.add((CompilationUnit) dataHandler.getParsedFileByName(ui.getTypeAsString()));

        }

        return clazzes;
    }

}
