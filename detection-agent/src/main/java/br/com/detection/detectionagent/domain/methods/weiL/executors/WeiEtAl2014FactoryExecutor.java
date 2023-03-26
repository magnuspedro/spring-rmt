package br.com.detection.detectionagent.domain.methods.weiL.executors;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014Canditate;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014FactoryCanditate;
import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.ExtractionMethod;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.patterns.DesignPattern;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class WeiEtAl2014FactoryExecutor implements WeiEtAl2014Executor {

    private final AstHandler astHandler = new AstHandler();

    @Override
    public void refactor(RefactoringCandidate candidate, List<JavaFile> dataHandler, ExtractionMethod extractionMethod) {
        final WeiEtAl2014FactoryCanditate weiCandidate = (WeiEtAl2014FactoryCanditate) candidate;

        try {
            for (IfStmt ifStmt : weiCandidate.getIfStatements()) {
                this.changesIfStmtCandidate(weiCandidate, ifStmt, dataHandler, extractionMethod);
            }

            this.changeBaseClazz(weiCandidate, dataHandler, extractionMethod);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void changesIfStmtCandidate(WeiEtAl2014FactoryCanditate candidate, IfStmt ifStmt, List<JavaFile> dataHandler, ExtractionMethod extractionMethod) {

        final Collection<CompilationUnit> allClasses = extractionMethod.parseAll(dataHandler).stream()
                .map(CompilationUnit.class::cast).toList();
        final CompilationUnit baseCu = this.updateBaseCompilationUnit(allClasses, candidate);
        var path = dataHandler.stream()
                .filter(f -> this.astHandler.unitsMatch((CompilationUnit) f.getParsed(), baseCu))
                .map(JavaFile::getPath)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        final Path file = Path.of(path);
        final ClassOrInterfaceDeclaration classDclr = this.astHandler.getClassOrInterfaceDeclaration(baseCu)
                .orElseThrow(IllegalStateException::new);

        final String createdClassName = this.getClassReturnTypeName(classDclr, ifStmt, candidate.getMethodReturnType(),
                dataHandler);
        final String newFactoryClassName = String.format("%sFactory", createdClassName);

        final CompilationUnit cu = new CompilationUnit();
        cu.setPackageDeclaration(baseCu.getPackageDeclaration().orElse(null));
        ClassOrInterfaceDeclaration type = cu.addClass(newFactoryClassName);

        final MethodDeclaration method = new MethodDeclaration();
        method.setName(candidate.getMethodDcl().getName());
        method.setType(candidate.getMethodDcl().getType());
        method.setModifiers(EnumSet.of(Modifier.PUBLIC));
        method.setBody((BlockStmt) ifStmt.getThenStmt());

        type.addMember(method);
        type.addExtendedType(classDclr.getNameAsString());

        writeCanges(cu, file.getParent().resolve(String.format("%s.java", newFactoryClassName)));
    }

    private String getClassReturnTypeName(ClassOrInterfaceDeclaration classDclr, IfStmt ifStmt,
                                          ClassOrInterfaceType returnType, List<JavaFile> dataHandler) {
        final ReturnStmt returnStmt = this.astHandler.getReturnStmt(ifStmt).orElseThrow(IllegalStateException::new);

        final Optional<Node> node = returnStmt.getChildNodes().stream().findFirst();

        if (node.filter(NameExpr.class::isInstance).isPresent()) {

            final String returnName = node.map(NameExpr.class::cast).get().getNameAsString();

            final Optional<VariableDeclarator> varDclr = this.astHandler
                    .getVariableDeclarationInNode(ifStmt.getThenStmt(), returnName);

            final ObjectCreationExpr objectCreationExpr = varDclr.map(this.astHandler::getObjectCreationExpr)
                    .filter(Optional::isPresent).map(Optional::get).orElseThrow(IllegalStateException::new);

            return this.getClassDeclarationName(objectCreationExpr, dataHandler);
        } else if (node.filter(ObjectCreationExpr.class::isInstance).isPresent()) {

            final ObjectCreationExpr objCreationExpr = node.map(ObjectCreationExpr.class::cast)
                    .orElseThrow(IllegalStateException::new);

            return this.getClassDeclarationName(objCreationExpr, dataHandler);
        } else {
            throw new IllegalStateException();
        }
    }

    private String getClassDeclarationName(ObjectCreationExpr objectCreationExpr, List<JavaFile> dataHandler) {

        final Optional<CompilationUnit> cu =
                dataHandler.stream().filter(f -> f.getName().equals(objectCreationExpr.getType().getNameAsString()))
                        .map(m -> (CompilationUnit) m.getParsed())
                        .findFirst();

        final Optional<ClassOrInterfaceDeclaration> declaration = cu.flatMap(this.astHandler::getClassOrInterfaceDeclaration);

        return declaration.map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElseThrow(IllegalStateException::new);
    }

    private void changeBaseClazz(WeiEtAl2014Canditate candidate, List<JavaFile> dataHandler, ExtractionMethod extractionMethod) {
        final Collection<CompilationUnit> allClasses = extractionMethod.parseAll(dataHandler).stream()
                .map(CompilationUnit.class::cast).toList();
        final CompilationUnit baseCu = this.updateBaseCompilationUnit(allClasses, candidate);
        final ClassOrInterfaceDeclaration classDclr = this.astHandler.getClassOrInterfaceDeclaration(baseCu)
                .orElseThrow(IllegalStateException::new);

        final MethodDeclaration candidateMethod = this.astHandler.getMethods(baseCu).stream()
                .filter(m -> m.getNameAsString().equals(candidate.getMethodDcl().getNameAsString())
                        && this.astHandler.methodsParamsMatch(m, candidate.getMethodDcl()))
                .findFirst().orElseThrow(IllegalStateException::new);

        classDclr.setAbstract(true);
        candidateMethod.setBody(null);
        candidateMethod.setAbstract(true);
        candidateMethod.getParameter(0).remove();


        var path = dataHandler.stream()
                .filter(f -> this.astHandler.unitsMatch((CompilationUnit) f.getParsed(), baseCu))
                .map(JavaFile::getPath)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        var file = Path.of(path);
//        writeCanges(baseCu, file);
    }

    private CompilationUnit updateBaseCompilationUnit(Collection<CompilationUnit> allClasses,
                                                      WeiEtAl2014Canditate candidate) {
        return allClasses.stream().filter(c -> this.astHandler.unitsMatch(c, Optional.of(candidate.getClassDeclaration()),
                Optional.of(candidate.getPackageDeclaration()))).findFirst().get();
    }

    @Override
    public boolean isApplicable(RefactoringCandidate candidate) {
        return candidate instanceof WeiEtAl2014FactoryCanditate
                && DesignPattern.FACTORY_METHOD.equals(candidate.getEligiblePattern());
    }

}
