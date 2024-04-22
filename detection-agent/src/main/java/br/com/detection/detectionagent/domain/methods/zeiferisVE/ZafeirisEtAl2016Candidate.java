package br.com.detection.detectionagent.domain.methods.zeiferisVE;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.patterns.DesignPattern;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@ToString
@RequiredArgsConstructor
public class ZafeirisEtAl2016Candidate implements RefactoringCandidate {

    @Builder.Default
    private final String id = UUID.randomUUID().toString();

    @Builder.Default
    private final Reference reference = Reference.builder()
            .title("Automated refactoring of super-class method invocations to the Template Method design pattern")
            .year(2016)
            .authors(List.of("E. A. Giakoumakis", "N. A. Diamantidis", "Sotiris H. Poulias", "Vassilis E. Zafeiris"))
            .build();

    private final JavaFile file;

    private final CompilationUnit compilationUnit;

    private final PackageDeclaration packageDcl;

    private final ClassOrInterfaceDeclaration classDcl;

    private final MethodDeclaration overridenMethod;

    private final MethodDeclaration overridingMethod;

    private final SuperExpr superCall;

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
        return DesignPattern.TEMPLATE_METHOD;
    }

    public PackageDeclaration getPackageDeclaration() {
        return packageDcl;
    }

    public ClassOrInterfaceDeclaration getClassDeclaration() {
        return classDcl;
    }

    public FragmentsSplitter toFragment() {
        return new FragmentsSplitter(this.getOverridingMethod());
    }

    public CandidateWithVariables toCandidateWithVariables() {
        return new CandidateWithVariables(this,
                this.toFragment().getVariablesOnBeforeFragmentsMethodCalss());
    }

    public record CandidateWithVariables(ZafeirisEtAl2016Candidate candidate,
                                         List<VariableDeclarationExpr> variables) {
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ZafeirisEtAl2016Candidate another) {
            return new EqualsBuilder().append(id, another.id).isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

}
