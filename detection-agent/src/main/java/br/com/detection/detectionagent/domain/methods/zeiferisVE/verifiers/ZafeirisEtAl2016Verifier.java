package br.com.detection.detectionagent.domain.methods.zeiferisVE.verifiers;

import br.com.detection.detectionagent.domain.dataExtractions.ast.AstHandler;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.ZafeirisEtAl2016Candidate;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.preconditions.ExtractMethodPreconditions;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.preconditions.SiblingPreconditions;
import br.com.detection.detectionagent.domain.methods.zeiferisVE.preconditions.SuperInvocationPreconditions;
import br.com.magnus.config.starter.file.JavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SuperExpr;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ZafeirisEtAl2016Verifier {

    private final SuperInvocationPreconditions superInvocationPreconditions;
    private final ExtractMethodPreconditions extractMethodPreconditions;
    private final SiblingPreconditions siblingPreconditions;


    public List<ZafeirisEtAl2016Candidate> retrieveCandidatesFrom(List<JavaFile> javaFiles) {

        final var candidates = this.retrieveCandidates(javaFiles);
        var candidatesWithOverriddenMethods = candidates.stream()
                .map(ZafeirisEtAl2016Candidate::getOverriddenMethod)
                .toList();

        for (var overriddenMethod : candidatesWithOverriddenMethods) {
            final var candidateWithSameOverriddenMethod = candidates.stream()
                    .filter(c -> c.getOverriddenMethod().equals(overriddenMethod))
                    .toList();

            if (siblingPreconditions.violates(candidateWithSameOverriddenMethod)) {
                candidates.removeAll(candidates.stream()
                        .filter(c -> c.getOverriddenMethod().equals(overriddenMethod))
                        .toList()
                );
            }
        }
        return candidates;
    }

    private List<ZafeirisEtAl2016Candidate> retrieveCandidates(List<JavaFile> javaFiles) {
        final var candidates = new ArrayList<ZafeirisEtAl2016Candidate>();

        var cus = javaFiles.stream()
                .map(JavaFile::getCompilationUnit)
                .toList();

        javaFiles.forEach(file -> {
            final var parent = AstHandler.getParent(file.getCompilationUnit(), cus).orElse(null);

            this.retrieveCandidate(file, parent).ifPresent(candidates::add);
        });

        return candidates;
    }

    private Optional<ZafeirisEtAl2016Candidate> retrieveCandidate(JavaFile file, CompilationUnit parent) {

        if (this.violatesClassPreconditions(parent)) {
            return Optional.empty();
        }

        final var methods = AstHandler.getMethods(file.getCompilationUnit());
        final var nonStaticOrConstructorMethodsList = methods.stream()
                .filter(m -> !m.isConstructorDeclaration() && !m.isStatic())
                .toList();

        for (var method : nonStaticOrConstructorMethodsList) {
            final var superCalls = AstHandler.getSuperCalls(method);

            if (superInvocationPreconditions.violatesAmountOfSuperCallsOrName(method, superCalls)) {
                continue;
            }

            final var overriddenMethod = AstHandler.retrieveOverriddenMethod(parent, method);

            if (overriddenMethod == null) {
                continue;
            }

            final var superCall = superCalls.stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Super call should exists in method"));

            if (!this.superInvocationPreconditions.isOverriddenMethodValid(overriddenMethod, method)
                    || !extractMethodPreconditions.isValid(overriddenMethod, method)) {
                continue;
            }

            return Optional.of(this.createCandidate(file, overriddenMethod, method, superCall));

        }
        return Optional.empty();
    }

    private ZafeirisEtAl2016Candidate createCandidate(JavaFile file, MethodDeclaration overriddenMethod, MethodDeclaration method, SuperExpr superCall) {
        final var pkgDcl = AstHandler.getPackageDeclaration(file.getCompilationUnit());

        final var classDcl = AstHandler.getClassOrInterfaceDeclaration(file.getCompilationUnit())
                .orElseThrow(() -> new IllegalArgumentException("Could not find class declaration for candidate"));

        return ZafeirisEtAl2016Candidate.builder()
                .file(file)
                .compilationUnit(file.getCompilationUnit())
                .packageDcl(pkgDcl)
                .classDcl(classDcl)
                .overriddenMethod(overriddenMethod)
                .overridingMethod(method)
                .superCall(superCall)
                .build();
    }

    private boolean violatesClassPreconditions(CompilationUnit parent) {
        return parent == null;

    }

}
