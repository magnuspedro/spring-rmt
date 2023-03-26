package br.com.detection.detectionagent.domain.methods.weiL;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.messages.patterns.DesignPattern;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;

public class WeiEtAl2014FactoryCanditate extends WeiEtAl2014Canditate {

	private final ClassOrInterfaceType methodReturnType;

	public WeiEtAl2014FactoryCanditate(JavaFile file, CompilationUnit compilationUnit,
									   PackageDeclaration packageDcl, ClassOrInterfaceDeclaration classDcl, MethodDeclaration methodDcl,
									   ClassOrInterfaceType methodReturnType, Collection<IfStmt> ifStatements) {
		super(file, compilationUnit, packageDcl, classDcl, methodDcl, ifStatements,
				DesignPattern.FACTORY_METHOD);

		this.methodReturnType = methodReturnType;
	}

	public ClassOrInterfaceType getMethodReturnType() {
		return methodReturnType;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof WeiEtAl2014FactoryCanditate) {
			WeiEtAl2014FactoryCanditate another = (WeiEtAl2014FactoryCanditate) object;
			return new EqualsBuilder().append(this.getId(), another.getId()).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.getId()).toHashCode();
	}

}
