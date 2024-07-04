package br.com.magnus.detectionandrefactoring.refactor.methods.weiL;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.patterns.DesignPattern;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class WeiEtAl2014FactoryCandidate extends WeiEtAl2014Candidate {

	private final ClassOrInterfaceType methodReturnType;

	public WeiEtAl2014FactoryCandidate(JavaFile file, CompilationUnit compilationUnit,
									   PackageDeclaration packageDcl, ClassOrInterfaceDeclaration classDcl, MethodDeclaration methodDcl,
									   ClassOrInterfaceType methodReturnType, Collection<IfStmt> ifStatements) {
		super(file, compilationUnit, packageDcl, classDcl, methodDcl, ifStatements,
				DesignPattern.FACTORY_METHOD);

		this.methodReturnType = methodReturnType;
	}
}
