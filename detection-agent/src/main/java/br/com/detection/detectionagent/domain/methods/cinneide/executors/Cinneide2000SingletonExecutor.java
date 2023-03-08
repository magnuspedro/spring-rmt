package br.com.detection.detectionagent.domain.methods.cinneide.executors;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.detectionagent.domain.methods.cinneide.Cinneide2000SingletonCanditate;
import br.com.detection.detectionagent.domain.methods.cinneide.minitransformations.PartialAbstraction;
import br.com.detection.detectionagent.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.patterns.DesignPattern;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public class Cinneide2000SingletonExecutor implements Cinneide2000Executor {

    private final AstHandler astHandler = new AstHandler();
    private final PartialAbstraction pa = new PartialAbstraction();

    @Override
    public boolean isApplicable(RefactoringCandidate candidate) {
        return candidate instanceof Cinneide2000SingletonCanditate
                && DesignPattern.SINGLETON.equals(candidate.getEligiblePattern());
    }

    @Override
    public void refactor(RefactoringCandidate candidate, DataHandler dataHandler) {

        final Cinneide2000SingletonCanditate cinneidCandidate = (Cinneide2000SingletonCanditate) candidate;
        final String typeName = cinneidCandidate.getClassDeclaration().getNameAsString();
        pa.makePartialAbstraction(typeName, dataHandler);

        final Collection<CompilationUnit> allClasses = pa.getParsedClasses(dataHandler);
        final CompilationUnit baseCu = pa.updateBaseCompilationUnit(allClasses, cinneidCandidate);

        final Path file = dataHandler.getFile(baseCu);
        final Optional<ClassOrInterfaceDeclaration> clazz = baseCu.findFirst(ClassOrInterfaceDeclaration.class);
        final String className = candidate.getClassName().split("\\.", 0)[0];

        clazz.ifPresent(c -> {

            c.addField(className, "instance", Modifier.PRIVATE).addModifier(Modifier.STATIC);

            if ((c.getConstructors().size() > 0)) {
                c.getConstructors().forEach(ct -> ct.setPublic(false).setProtected(true));
            } else {
                c.addConstructor(Modifier.PROTECTED);
            }

            c.addMethod("getInstance").addModifier(Modifier.PUBLIC).addModifier(Modifier.STATIC)
                    .setBody(createSingletonBlock(className));

        });

        pa.writeChanges(baseCu, file);

    }

    private BlockStmt createSingletonBlock(String className) {
        BlockStmt blockStmt = new BlockStmt();
        IfStmt ifStmt = new IfStmt();
        Expression expression = JavaParser.parseExpression("instance == null");

        ifStmt.setCondition(expression);
        ifStmt.setThenStmt(JavaParser.parseStatement(String.format("instance = new %s();", className)));

        blockStmt.addStatement(ifStmt);
        blockStmt.addStatement(JavaParser.parseStatement("return instance;"));

        return blockStmt;
    }
}
