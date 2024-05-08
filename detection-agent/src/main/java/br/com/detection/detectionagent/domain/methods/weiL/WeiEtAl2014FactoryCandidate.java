package br.com.detection.detectionagent.domain.methods.weiL;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.magnus.config.starter.patterns.DesignPattern;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;

@Getter
@SuperBuilder
public class WeiEtAl2014FactoryCandidate extends WeiEtAl2014Candidate {

	private final ClassOrInterfaceType methodReturnType;

	public WeiEtAl2014FactoryCandidate(JavaFile file, CompilationUnit compilationUnit,
									   PackageDeclaration packageDcl, ClassOrInterfaceDeclaration classDcl, MethodDeclaration methodDcl,
									   ClassOrInterfaceType methodReturnType, Collection<IfStmt> ifStatements) {
		super(file, compilationUnit, packageDcl, classDcl, methodDcl, ifStatements,
				DesignPattern.FACTORY_METHOD);

		this.methodReturnType = methodReturnType;
	}

    @Override
	public boolean equals(Object object) {
		if (object instanceof WeiEtAl2014FactoryCandidate another) {
            return new EqualsBuilder().append(this.getId(), another.getId()).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.getId()).toHashCode();
	}

}
