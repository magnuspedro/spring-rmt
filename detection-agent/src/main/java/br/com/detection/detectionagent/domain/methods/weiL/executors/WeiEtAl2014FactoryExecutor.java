package br.com.detection.detectionagent.domain.methods.weiL.executors;

import br.com.detection.detectionagent.domain.dataExtractions.ast.AstHandler;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014Candidate;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014FactoryCandidate;
import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.AbstractSyntaxTree;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.patterns.DesignPattern;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WeiEtAl2014FactoryExecutor implements WeiEtAl2014Executor {

    @Override
    public void refactor(RefactoringCandidate candidate, List<JavaFile> javaFiles) {
        Assert.notNull(candidate, "Candidate cannot be null");
        Assert.notNull(javaFiles, "JavaFiles cannot be null");
        final var weiCandidate = (WeiEtAl2014FactoryCandidate) candidate;

        try {
            for (var ifStmt : weiCandidate.getIfStatements()) {
                this.changesIfStmtCandidate(weiCandidate, ifStmt, javaFiles);
            }

            this.changeBaseClazz(weiCandidate, javaFiles);

        } catch (Exception ex) {
            throw new WeiEtAl2014ExecutorException("Error Refactoring Factory Method", ex);
        }
    }

    private void changesIfStmtCandidate(WeiEtAl2014FactoryCandidate candidate, IfStmt ifStmt, List<JavaFile> javaFiles) {

        final var allClasses = javaFiles
                .stream()
                .map(JavaFile::getCompilationUnit)
                .toList();
        final var baseCu = this.updateBaseCompilationUnit(allClasses, candidate);
        var path = javaFiles.stream()
                .filter(file -> AstHandler.doesCompilationUnitsMatch(file.getCompilationUnit(), baseCu))
                .map(JavaFile::getPath)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        final var classDclr = AstHandler.getClassOrInterfaceDeclaration(baseCu)
                .orElseThrow(IllegalStateException::new);

        final var createdClassName = this.getClassReturnTypeName(ifStmt, javaFiles);
        final var newFactoryClassName = String.format("%sFactory", createdClassName);

        final var cu = new CompilationUnit();
        final var method = new MethodDeclaration();
        final var type = cu.addClass(newFactoryClassName);
        cu.setPackageDeclaration(baseCu.getPackageDeclaration().orElseThrow(IllegalArgumentException::new));
        method.setName(candidate.getMethodDcl().getName());
        method.setType(candidate.getMethodDcl().getType());
        method.setModifiers(EnumSet.of(Modifier.PUBLIC));
        method.setBody((BlockStmt) ifStmt.getThenStmt());
        type.addMember(method);
        type.addExtendedType(classDclr.getNameAsString());

        javaFiles.add(JavaFile.builder()
                .name(String.format("%s.java", newFactoryClassName))
                .originalClass(cu.toString())
                .path(path)
                .parsed(AbstractSyntaxTree.parseSingle(cu.toString()))
                .build());
    }

    private String getClassReturnTypeName(IfStmt ifStmt, List<JavaFile> javaFiles) {
        final var node = AstHandler.getReturnStmt(ifStmt)
                .map(Node::getChildNodes)
                .stream()
                .flatMap(Collection::stream)
                .findFirst();

        if (node.filter(NameExpr.class::isInstance).isPresent()) {

            final var returnName = node.map(NameExpr.class::cast).get().getNameAsString();
            final var varDclr = AstHandler
                    .getVariableDeclarationInNode(ifStmt.getThenStmt(), returnName);
            final var objectCreationExpr = varDclr
                    .map(AstHandler::getObjectCreationExpr)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .orElseThrow(IllegalStateException::new);

            return this.getClassDeclarationName(objectCreationExpr, javaFiles);
        } else if (node.filter(ObjectCreationExpr.class::isInstance).isPresent()) {
            final var objCreationExpr = node
                    .map(ObjectCreationExpr.class::cast)
                    .orElseThrow(IllegalStateException::new);

            return this.getClassDeclarationName(objCreationExpr, javaFiles);
        } else {
            throw new IllegalStateException();
        }
    }

    private String getClassDeclarationName(ObjectCreationExpr objectCreationExpr, List<JavaFile> javaFiles) {
        return javaFiles.stream()
                .filter(f -> f.getFileNameWithoutExtension().equals(objectCreationExpr.getType().getNameAsString()))
                .map(m -> (CompilationUnit) m.getParsed())
                .map(AstHandler::getClassOrInterfaceDeclaration)
                .flatMap(Optional::stream)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    private void changeBaseClazz(WeiEtAl2014Candidate candidate, List<JavaFile> javaFiles) {

        final var allClasses = javaFiles
                .stream()
                .map(JavaFile::getCompilationUnit)
                .toList();
        final var baseCu = this.updateBaseCompilationUnit(allClasses, candidate);
        final var classDclr = AstHandler.getClassOrInterfaceDeclaration(baseCu)
                .orElseThrow(IllegalStateException::new);

        final var candidateMethod = AstHandler.getMethods(baseCu)
                .stream()
                .filter(m -> m.getNameAsString().equals(candidate.getMethodDcl().getNameAsString())
                        && AstHandler.methodsParamsMatch(m, candidate.getMethodDcl()))
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        classDclr.setAbstract(true);
        candidateMethod.setBody(null);
        candidateMethod.setAbstract(true);
        candidateMethod.getParameter(0).remove();
    }

    private CompilationUnit updateBaseCompilationUnit(Collection<CompilationUnit> classes,
                                                      WeiEtAl2014Candidate candidate) {
        return classes.stream()
                .filter(c -> AstHandler.doesCompilationUnitsMatch(c, Optional.of(candidate.getClassDeclaration()),
                        Optional.of(candidate.getPackageDeclaration())))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public boolean isApplicable(RefactoringCandidate candidate) {
        return candidate instanceof WeiEtAl2014FactoryCandidate
                && DesignPattern.FACTORY_METHOD.equals(candidate.getEligiblePattern());
    }

}
