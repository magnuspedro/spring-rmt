package br.com.detection.detectionagent.domain.methods.weiL;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.messages.patterns.DesignPattern;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.IfStmt;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.List;

public class WeiEtAl2014StrategyCanditate extends WeiEtAl2014Canditate {

	private final List<VariableDeclarator> variables;

	public WeiEtAl2014StrategyCanditate(JavaFile file, CompilationUnit compilationUnit,
										PackageDeclaration packageDcl, ClassOrInterfaceDeclaration classDcl, MethodDeclaration methodDcl,
										Collection<IfStmt> ifStatements, List<VariableDeclarator> variables) {
		super(file, compilationUnit, packageDcl, classDcl, methodDcl, ifStatements, DesignPattern.STRATEGY);
		this.variables = variables;
	}

	public List<VariableDeclarator> getVariables() {
		return variables;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof WeiEtAl2014StrategyCanditate) {
			WeiEtAl2014StrategyCanditate another = (WeiEtAl2014StrategyCanditate) object;
			return new EqualsBuilder().append(this.getId(), another.getId()).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.getId()).toHashCode();
	}

}
