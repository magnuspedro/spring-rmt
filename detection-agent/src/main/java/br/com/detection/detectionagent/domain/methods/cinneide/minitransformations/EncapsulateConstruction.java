package br.com.detection.detectionagent.domain.methods.cinneide.minitransformations;

import br.com.detection.detectionagent.methods.dataExtractions.forks.DataHandler;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.nio.file.Path;

public class EncapsulateConstruction extends MinitransformationUitls {

	public void makeEncapsulateConstruction(String creator, String product, String createP, DataHandler dataHandler) {
		final CompilationUnit creatorCu = (CompilationUnit) dataHandler.getParsedFileByName(creator);
		final CompilationUnit productCu = (CompilationUnit) dataHandler.getParsedFileByName(product);
		final Path file = dataHandler.getFile(creatorCu);

		final ClassOrInterfaceDeclaration creatorClass = creatorCu.findFirst(ClassOrInterfaceDeclaration.class).get();

		productCu.findAll(ConstructorDeclaration.class).forEach(c -> {
			MethodDeclaration m = makeAbstract(c, createP);
			creatorClass.addMember(m);
		});

		creatorCu.findAll(FieldDeclaration.class).forEach(e -> {
			if (e.getElementType().toString().equals(product)
					&& !e.getVariable(0).getInitializer().get().getChildNodes().get(0).toString().equals(createP)) {
				e.getVariable(0).setInitializer(createP);
			}
		});
		this.writeChanges(creatorCu, file);
	}
}
