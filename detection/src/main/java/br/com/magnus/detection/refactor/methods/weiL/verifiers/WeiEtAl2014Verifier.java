package br.com.magnus.detection.refactor.methods.weiL.verifiers;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.detection.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detection.refactor.methods.weiL.RefactoringCandidatesVerifier;
import br.com.magnus.detection.refactor.methods.weiL.WeiEtAl2014Candidate;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.VoidType;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class WeiEtAl2014Verifier implements RefactoringCandidatesVerifier {

    public List<RefactoringCandidate> retrieveCandidatesFrom(List<JavaFile> javaFiles) {
        Assert.notNull(javaFiles, "JavaFiles cannot be null");
        final var candidates = new ArrayList<RefactoringCandidate>();

        javaFiles.forEach(file -> {
            var classOrInterface = AstHandler.getClassOrInterfaceDeclaration(file.getCompilationUnit());

            classOrInterface.ifPresent(classOrInterfaceDeclaration -> {
                if (!classOrInterfaceDeclaration.isInterface()) {
                    for (var method : AstHandler.getMethods(classOrInterfaceDeclaration)) {

                        final var candidate = this.retrieveCandidate(javaFiles, file, method);
                        candidate.ifPresent(candidates::add);
                    }
                }
            });
        });
        return candidates;
    }

    private boolean isMethodInvalid(MethodDeclaration method) {
        return method.getParameters() == null
                || method.getParameters().isEmpty()
                || method.getParameters().size() > 1
                || (method.getType() instanceof VoidType);
    }

    private Optional<WeiEtAl2014Candidate> retrieveCandidate(List<JavaFile> javaFiles, JavaFile file, MethodDeclaration method) {

        if (this.isMethodInvalid(method)) {
            return Optional.empty();
        }

        final var ifStatements = AstHandler.getIfStatements(method);

        if (!this.areIfStmtsValid(javaFiles, file,method, ifStatements)) {
            return Optional.empty();
        }

        return Optional.of(this.createCandidate(file, method, ifStatements));
    }

    protected abstract WeiEtAl2014Candidate createCandidate(JavaFile file, MethodDeclaration method, Collection<IfStmt> ifStatements);

    protected abstract boolean areIfStmtsValid(List<JavaFile> javaFiles, JavaFile file, MethodDeclaration method, Collection<IfStmt> ifStatements);

}
