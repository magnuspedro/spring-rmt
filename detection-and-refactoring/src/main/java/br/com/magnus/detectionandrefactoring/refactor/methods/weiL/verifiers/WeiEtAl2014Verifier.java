package br.com.magnus.detectionandrefactoring.refactor.methods.weiL.verifiers;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.methods.weiL.RefactoringCandidatesVerifier;
import br.com.magnus.detectionandrefactoring.refactor.methods.weiL.WeiEtAl2014Candidate;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
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
        if (this.hasUnsupportedNestedIfStatements(method)) {
            return Optional.empty();
        }

        if (!this.areIfStmtsValid(javaFiles, file,method, ifStatements)) {
            return Optional.empty();
        }

        return Optional.of(this.createCandidate(file, method, ifStatements));
    }

    private boolean hasUnsupportedNestedIfStatements(MethodDeclaration method) {
        return method.getBody()
                .map(body -> body.getStatements().stream()
                        .filter(IfStmt.class::isInstance)
                        .map(IfStmt.class::cast)
                        .anyMatch(this::containsNestedIfOutsideElseIfChain))
                .orElse(false);
    }

    private boolean containsNestedIfOutsideElseIfChain(IfStmt ifStmt) {
        if (this.containsAnyIf(ifStmt.getThenStmt())) {
            return true;
        }

        final var elseStmt = ifStmt.getElseStmt();
        if (elseStmt.isEmpty()) {
            return false;
        }

        final var statement = elseStmt.get();
        if (statement instanceof IfStmt elseIf) {
            return this.containsNestedIfOutsideElseIfChain(elseIf);
        }

        return this.containsAnyIf(statement);
    }

    private boolean containsAnyIf(Statement statement) {
        return statement.getChildNodes().stream()
                .anyMatch(node -> node instanceof IfStmt || node.getChildNodes().stream()
                        .anyMatch(child -> child instanceof IfStmt || this.nodeContainsIf(child)));
    }

    private boolean nodeContainsIf(com.github.javaparser.ast.Node node) {
        if (node instanceof IfStmt) {
            return true;
        }
        return node.getChildNodes().stream().anyMatch(this::nodeContainsIf);
    }

    protected abstract WeiEtAl2014Candidate createCandidate(JavaFile file, MethodDeclaration method, Collection<IfStmt> ifStatements);

    protected abstract boolean areIfStmtsValid(List<JavaFile> javaFiles, JavaFile file, MethodDeclaration method, Collection<IfStmt> ifStatements);

}
