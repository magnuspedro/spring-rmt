package br.com.detection.detectionagent.domain.methods.zeiferisVE.verifiers;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.ZafeirisEtAl2016Canditate;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.preconditions.ExtractMethodPreconditions;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.preconditions.SiblingPreconditions;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.preconditions.SuperInvocationPreconditions;
import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.ExtractionMethod;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SuperExpr;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ZafeirisEtAl2016Verifier {

    private final AstHandler astHandler = new AstHandler();

    private final SuperInvocationPreconditions superInvocationPreconditions = new SuperInvocationPreconditions();

    private final ExtractMethodPreconditions extractMethodPreconditions = new ExtractMethodPreconditions();

    private final SiblingPreconditions siblingPreconditions = new SiblingPreconditions();


    public List<ZafeirisEtAl2016Canditate> retrieveCandidatesFrom(List<JavaFile> javaFiles, ExtractionMethod extractMethod)
            throws MalformedURLException, FileNotFoundException {

        final List<ZafeirisEtAl2016Canditate> candidates = this.retrieveCandidates(javaFiles, extractMethod);

        for (MethodDeclaration overridenMethod : candidates.stream().map(ZafeirisEtAl2016Canditate::getOverridenMethod)
                .toList()) {

            final Collection<ZafeirisEtAl2016Canditate> canditadesOfSameOverridenMethod = candidates.stream()
                    .filter(c -> c.getOverridenMethod().equals(overridenMethod)).collect(Collectors.toList());

            if (siblingPreconditions.violates(canditadesOfSameOverridenMethod)) {
                candidates.removeAll(candidates.stream().filter(c -> c.getOverridenMethod().equals(overridenMethod))
                        .toList());
            }
        }
        return candidates;
    }

    private List<ZafeirisEtAl2016Canditate> retrieveCandidates(List<JavaFile> javaFiles, ExtractionMethod extractionMethod) {
        final List<ZafeirisEtAl2016Canditate> candidates = new ArrayList<>();

        var cus = extractionMethod.parseAll(javaFiles).stream()
                .map(CompilationUnit.class::cast)
                .toList();

        javaFiles.forEach(file -> {

            final Optional<CompilationUnit> parent = this.astHandler.getParent((CompilationUnit) file.getParsed(), cus);

            this.retrieveCandidate(file, (CompilationUnit) file.getParsed(), parent).ifPresent(candidates::add);
        });

        return candidates;
    }

    private Optional<ZafeirisEtAl2016Canditate> retrieveCandidate(JavaFile file, CompilationUnit cUnit,
                                                                  Optional<CompilationUnit> parent) {

        if (this.violatesClassPreconditions(cUnit, parent)) {
            return Optional.empty();
        }

        final Collection<MethodDeclaration> methods = this.astHandler.getMethods(cUnit);

        for (MethodDeclaration method : methods.stream().filter(m -> !m.isConstructorDeclaration() && !m.isStatic())
                .toList()) {

            final Collection<SuperExpr> superCalls = this.astHandler.getSuperCalls(method);

            if (superInvocationPreconditions.violatesAmountOfSuperCallsOrName(method, superCalls)) {
                continue;
            }

            final SuperExpr superCall = superCalls.stream().findFirst().get();

            final MethodDeclaration overridenMethod = this.astHandler.retrieveOverridenMethod(cUnit, parent.get(),
                    method);

            if (overridenMethod == null) {
                continue;
            }

            if (!this.superInvocationPreconditions.isOverriddenMethodValid(overridenMethod, method)
                    || !extractMethodPreconditions.isValid(overridenMethod, method, superCall)) {
                continue;
            }

            return Optional.of(this.createCandidate(file, cUnit, overridenMethod, method, superCall));

        }
        return Optional.empty();
    }

    private ZafeirisEtAl2016Canditate createCandidate(JavaFile file, CompilationUnit cUnit,
                                                      MethodDeclaration overridenMethod, MethodDeclaration method, SuperExpr superCall) {
        final PackageDeclaration pkgDcl = this.astHandler.getPackageDeclaration(cUnit);

        final ClassOrInterfaceDeclaration classDcl = this.astHandler.getClassOrInterfaceDeclaration(cUnit).get();

        return new ZafeirisEtAl2016Canditate(file, cUnit, pkgDcl, classDcl, overridenMethod, method,
                superCall);
    }

    private boolean violatesClassPreconditions(CompilationUnit cUnit, Optional<CompilationUnit> parent) {
        return !parent.isPresent();

    }

}
