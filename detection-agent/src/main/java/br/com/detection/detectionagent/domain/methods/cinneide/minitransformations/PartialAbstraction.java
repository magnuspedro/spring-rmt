package br.com.detection.detectionagent.domain.methods.cinneide.minitransformations;

import br.com.detection.detectionagent.methods.dataExtractions.forks.DataHandler;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.nio.file.Path;

public class PartialAbstraction extends MinitransformationUitls {

    public void makePartialAbstraction(String clazz, DataHandler dataHandler) {
        final CompilationUnit cu = new CompilationUnit();
        final CompilationUnit baseCu = (CompilationUnit) dataHandler.getParsedFileByName(clazz);
        final Path file = dataHandler.getFile(baseCu);
        String newClassName = clazz;
        final String abstractClassName = String.format("Abstract%s", newClassName);
        ClassOrInterfaceDeclaration newClass = cu.addClass(abstractClassName);
        newClass.setAbstract(true);

        baseCu.findAll(FieldDeclaration.class).forEach(f -> {
            newClass.addPrivateField(f.getElementType().toString(), f.getVariable(0).toString());
            f.remove();
        });

        baseCu.findAll(MethodDeclaration.class).forEach(m -> {
                MethodDeclaration newMethod = newClass.addMethod(m.getNameAsString());
                if (m.isAbstract()) {
                    newMethod.setAbstract(true).setType(m.getType());
                } else {
                    newMethod.setBody(m.getBody().get()).setType(m.getType());
                    m.remove();
                }
        });

        baseCu.findAll(ClassOrInterfaceDeclaration.class).forEach(c -> c.addExtendedType(newClass.getNameAsString()));

        this.writeChanges(baseCu, file);
        this.writeChanges(cu, file.getParent().resolve(String.format("%s.java", abstractClassName)));
    }

}
