package br.com.detection.detectionagent.domain.methods.cinneide;

import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.patterns.DesignPattern;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.nio.file.Path;

public class Cinneide2000FactoryMethodCandidate extends Cinneide2000Candidate{
	
	private final ClassOrInterfaceType methodReturnType;
	

	public Cinneide2000FactoryMethodCandidate(Reference reference, Path file, CompilationUnit compilationUnit,
			PackageDeclaration packageDcl, ClassOrInterfaceDeclaration classDcl, MethodDeclaration methodDcl,
			ClassOrInterfaceType methodReturnType) {
		super(reference, file, compilationUnit, packageDcl, classDcl, methodDcl, DesignPattern.FACTORY_METHOD);
		
		this.methodReturnType = methodReturnType;
	}
	
	public ClassOrInterfaceType getMethodRetrunType() {
		return methodReturnType;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof Cinneide2000FactoryMethodCandidate) {
			Cinneide2000FactoryMethodCandidate another = (Cinneide2000FactoryMethodCandidate) object;
			return new EqualsBuilder().append(this.getId(), another.getId()).isEquals();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.getId()).toHashCode();
	}

}
