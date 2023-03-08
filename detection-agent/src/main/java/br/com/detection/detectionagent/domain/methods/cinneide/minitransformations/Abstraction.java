package br.com.detection.detectionagent.domain.methods.cinneide.minitransformations;

import br.com.detection.detectionagent.methods.dataExtractions.forks.DataHandler;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.nio.file.Path;

public class Abstraction extends MinitransformationUitls{

	public void makeAbstraction(String clazz, DataHandler dataHandler) {
		final CompilationUnit cu = new CompilationUnit();
		final String className = clazz;
		final CompilationUnit baseCu = (CompilationUnit) dataHandler.getParsedFileByName(className);
		final Path file = dataHandler.getFile(baseCu);
		final ClassOrInterfaceDeclaration newInterface = cu.addClass(className);
		final String newInterfaceName = String.format("%sInf", className);
		
		newInterface.setInterface(true);
		
		baseCu.findFirst(ClassOrInterfaceDeclaration.class).
		ifPresent(c -> c.addImplementedType(newInterfaceName));
		this.writeChanges(cu, file.getParent().resolve(String.format("%s.java",newInterfaceName)));
		this.writeChanges(baseCu, file);
		
	}
}
