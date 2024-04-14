package br.com.detection.detectionagent.domain.methods.weiL.verifiers;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.detectionagent.domain.methods.RefactoringCandidatesVerifier;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014Candidate;
import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.ExtractionMethod;
import br.com.messages.members.candidates.RefactoringCandidate;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.VoidType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class WeiEtAl2014Verifier implements RefactoringCandidatesVerifier {

    protected final AstHandler astHandler = new AstHandler();

    public List<RefactoringCandidate> retrieveCandidatesFrom(List<JavaFile> javaFiles, ExtractionMethod extractionMethod) {
        final List<RefactoringCandidate> candidates = new ArrayList<>();
        extractionMethod.parseAll(javaFiles);

        javaFiles.forEach(file -> {
            var cu = (CompilationUnit) file.getParsed();
            var classOrInterface = this.astHandler.getClassOrInterfaceDeclaration(cu);

            classOrInterface.ifPresent(classOrInterfaceDeclaration -> {
                if (!classOrInterfaceDeclaration.isInterface()) {
                    for (MethodDeclaration method : this.astHandler.getMethods(cu)) {

                        final Optional<WeiEtAl2014Candidate> candidate = this.retrieveCandidate(javaFiles, file, cu,
                                classOrInterface.get(), method);
                        candidate.ifPresent(candidates::add);
                    }
                }
            });
        });
        return candidates;
    }

    private boolean isMethodInvalid(MethodDeclaration method) {
        return method.getParameters() == null || method.getParameters().isEmpty() || method.getParameters().size() > 1
                || (method.getType() instanceof VoidType);
    }

    private Optional<WeiEtAl2014Candidate> retrieveCandidate(List<JavaFile> javaFiles, JavaFile file,
                                                             CompilationUnit parsedClazz, ClassOrInterfaceDeclaration classOrInterface, MethodDeclaration method) {

        if (this.isMethodInvalid(method)) {
            return Optional.empty();
        }

        final Collection<IfStmt> ifStatements = this.astHandler.getIfStatements(method);

        if (!this.ifStmtsAreValid(javaFiles, parsedClazz, classOrInterface, method, ifStatements)) {
            return Optional.empty();
        }

        final PackageDeclaration pkgDcl = this.astHandler.getPackageDeclaration(parsedClazz);

        return Optional
                .of(this.createCandidate(file, parsedClazz, pkgDcl, classOrInterface, method, ifStatements));
    }

    protected abstract WeiEtAl2014Candidate createCandidate(JavaFile file, CompilationUnit parsedClazz,
                                                            PackageDeclaration pkgDcl, ClassOrInterfaceDeclaration classOrInterface, MethodDeclaration method,
                                                            Collection<IfStmt> ifStatements);

    protected abstract boolean ifStmtsAreValid(List<JavaFile> javaFiles, CompilationUnit parsedClazz,
                                               ClassOrInterfaceDeclaration classOrInterface, MethodDeclaration method, Collection<IfStmt> ifStatements);

}
