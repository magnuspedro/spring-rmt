package br.com.magnus.detectionandrefactoring.refactor.methods.weiL;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.patterns.DesignPattern;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.IfStmt;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.List;

@Getter
@SuperBuilder
public class WeiEtAl2014StrategyCandidate extends WeiEtAl2014Candidate {

    private final List<VariableDeclarator> variables;

    public WeiEtAl2014StrategyCandidate(JavaFile file, CompilationUnit compilationUnit,
                                        PackageDeclaration packageDcl, ClassOrInterfaceDeclaration classDcl, MethodDeclaration methodDcl,
                                        Collection<IfStmt> ifStatements, List<VariableDeclarator> variables) {
        super(file, compilationUnit, packageDcl, classDcl, methodDcl, ifStatements, DesignPattern.STRATEGY);
        this.variables = variables;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof WeiEtAl2014StrategyCandidate another) {
            return new EqualsBuilder().append(this.getId(), another.getId()).isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getId()).toHashCode();
    }

}
