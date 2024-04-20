package br.com.detection.detectionagent.domain.methods.weiL.executors;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014Candidate;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014FactoryCandidate;
import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.ExtractionMethod;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.patterns.DesignPattern;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WeiEtAl2014FactoryExecutor implements WeiEtAl2014Executor {

    @Override
    public void refactor(RefactoringCandidate candidate, List<JavaFile> dataHandler, ExtractionMethod extractionMethod) {
        final WeiEtAl2014FactoryCandidate weiCandidate = (WeiEtAl2014FactoryCandidate) candidate;

        try {
            for (var ifStmt : weiCandidate.getIfStatements()) {
                this.changesIfStmtCandidate(weiCandidate, ifStmt, dataHandler, extractionMethod);
            }

            this.changeBaseClazz(weiCandidate, dataHandler, extractionMethod);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void changesIfStmtCandidate(WeiEtAl2014FactoryCandidate candidate, IfStmt ifStmt, List<JavaFile> dataHandler, ExtractionMethod extractionMethod) {

        final var allClasses = extractionMethod.parseAll(dataHandler)
                .stream()
                .map(CompilationUnit.class::cast)
                .toList();
        final var baseCu = this.updateBaseCompilationUnit(allClasses, candidate);
        var path = dataHandler.stream()
                .filter(f -> AstHandler.doesCompilationUnitsMatch((CompilationUnit) f.getParsed(), baseCu))
                .map(JavaFile::getPath)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        final var file = Path.of(path);
        final var classDclr = AstHandler.getClassOrInterfaceDeclaration(baseCu)
                .orElseThrow(IllegalStateException::new);

        final var createdClassName = this.getClassReturnTypeName(classDclr, ifStmt, candidate.getMethodReturnType(),
                dataHandler);
        final var newFactoryClassName = String.format("%sFactory", createdClassName);

        final var cu = new CompilationUnit();
        cu.setPackageDeclaration(baseCu.getPackageDeclaration().orElse(null));
        var type = cu.addClass(newFactoryClassName);

        final var method = new MethodDeclaration();
        method.setName(candidate.getMethodDcl().getName());
        method.setType(candidate.getMethodDcl().getType());
        method.setModifiers(EnumSet.of(Modifier.PUBLIC));
        method.setBody((BlockStmt) ifStmt.getThenStmt());

        type.addMember(method);
        type.addExtendedType(classDclr.getNameAsString());

        writeChanges(cu, file.getParent().resolve(String.format("%s.java", newFactoryClassName)));
    }

    private String getClassReturnTypeName(ClassOrInterfaceDeclaration classDclr, IfStmt ifStmt,
                                          ClassOrInterfaceType returnType, List<JavaFile> dataHandler) {
        final var returnStmt = AstHandler.getReturnStmt(ifStmt).orElseThrow(IllegalStateException::new);
        final var node = returnStmt.getChildNodes().stream().findFirst();

        if (node.filter(NameExpr.class::isInstance).isPresent()) {

            final var returnName = node.map(NameExpr.class::cast).get().getNameAsString();
            final var varDclr = AstHandler
                    .getVariableDeclarationInNode(ifStmt.getThenStmt(), returnName);
            final var objectCreationExpr = varDclr
                    .map(AstHandler::getObjectCreationExpr)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .orElseThrow(IllegalStateException::new);

            return this.getClassDeclarationName(objectCreationExpr, dataHandler);
        } else if (node.filter(ObjectCreationExpr.class::isInstance).isPresent()) {
            final var objCreationExpr = node
                    .map(ObjectCreationExpr.class::cast)
                    .orElseThrow(IllegalStateException::new);

            return this.getClassDeclarationName(objCreationExpr, dataHandler);
        } else {
            throw new IllegalStateException();
        }
    }

    private String getClassDeclarationName(ObjectCreationExpr objectCreationExpr, List<JavaFile> dataHandler) {

        final var cu = dataHandler.stream()
                .filter(f -> f.getName().equals(objectCreationExpr.getType().getNameAsString()))
                .map(m -> (CompilationUnit) m.getParsed())
                .findFirst();

        final var declaration = cu.flatMap(AstHandler::getClassOrInterfaceDeclaration);

        return declaration.map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElseThrow(IllegalStateException::new);
    }

    private void changeBaseClazz(WeiEtAl2014Candidate candidate, List<JavaFile> dataHandler, ExtractionMethod extractionMethod) {

        final var allClasses = extractionMethod.parseAll(dataHandler)
                .stream()
                .map(CompilationUnit.class::cast)
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


        var path = dataHandler.stream()
                .filter(f -> AstHandler.doesCompilationUnitsMatch((CompilationUnit) f.getParsed(), baseCu))
                .map(JavaFile::getPath)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

        var file = Path.of(path);
//        writeCanges(baseCu, file);
    }

    private CompilationUnit updateBaseCompilationUnit(Collection<CompilationUnit> allClasses,
                                                      WeiEtAl2014Candidate candidate) {
        return allClasses.stream()
                .filter(c -> AstHandler.doesCompilationUnitsMatch(c, Optional.of(candidate.getClassDeclaration()),
                        Optional.of(candidate.getPackageDeclaration())))
                .findFirst()
                .get();
    }

    @Override
    public boolean isApplicable(RefactoringCandidate candidate) {
        return candidate instanceof WeiEtAl2014FactoryCandidate
                && DesignPattern.FACTORY_METHOD.equals(candidate.getEligiblePattern());
    }

}
