package br.com.detection.detectionagent.domain.methods.weiL;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.patterns.DesignPattern;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@ToString
@SuperBuilder
public abstract class WeiEtAl2014Candidate implements RefactoringCandidate {

    private final String id = UUID.randomUUID().toString();

    private final Reference reference;

    private final JavaFile file;

    @Getter
    private final CompilationUnit compilationUnit;

    private final PackageDeclaration packageDcl;

    private final ClassOrInterfaceDeclaration classDcl;

    @Getter
    private final MethodDeclaration methodDcl;

    @Getter
    private final List<IfStmt> ifStatements;

    private final DesignPattern eligiblePattern;

    public WeiEtAl2014Candidate(JavaFile file, CompilationUnit compilationUnit,
                                PackageDeclaration packageDcl, ClassOrInterfaceDeclaration classDcl, MethodDeclaration methodDcl,
                                Collection<IfStmt> ifStatements, DesignPattern eligiblePattern) {
        this.reference = Reference.builder()
                .title("Automated pattern directed refactoring for complex conditional statements")
                .year(2014)
                .authors(List.of("Liu Wei", "Hu Zhi-gang", "Liu Hong-tao", "Yang Liu"))
                .build();
        this.ifStatements = new ArrayList<>();
        this.file = file;
        this.compilationUnit = compilationUnit;
        this.packageDcl = packageDcl;
        this.classDcl = classDcl;
        this.methodDcl = methodDcl;
        this.ifStatements.addAll(ifStatements);
        this.eligiblePattern = eligiblePattern;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Reference getReference() {
        return reference;
    }

    @Override
    public String getPkg() {
        return this.packageDcl.getNameAsString();
    }

    @Override
    public String getClassName() {
        return this.file.getName();
    }

    @Override
    public DesignPattern getEligiblePattern() {
        return eligiblePattern;
    }

    public PackageDeclaration getPackageDeclaration() {
        return packageDcl;
    }

    public ClassOrInterfaceDeclaration getClassDeclaration() {
        return classDcl;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof WeiEtAl2014Candidate another) {
            return new EqualsBuilder().append(id, another.id).isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

}
