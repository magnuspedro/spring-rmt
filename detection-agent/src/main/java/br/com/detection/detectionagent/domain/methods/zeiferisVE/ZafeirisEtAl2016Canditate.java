package br.com.detection.detectionagent.domain.methods.zeiferisVE;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.patterns.DesignPattern;
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

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Builder
@ToString
@RequiredArgsConstructor
public class ZafeirisEtAl2016Canditate implements RefactoringCandidate {

    @Builder.Default
    private final String id = UUID.randomUUID().toString();

    @Builder.Default
    private final Reference reference = Reference.builder()
            .title("Automated refactoring of super-class method invocations to the Template Method design pattern")
            .year(2016)
            .authors(List.of("E. A. Giakoumakis", "N. A. Diamantidis", "Sotiris H. Poulias", "Vassilis E. Zafeiris"))
            .build();

    private final JavaFile file;

    @Getter
    private final CompilationUnit compilationUnit;

    private final PackageDeclaration packageDcl;

    private final ClassOrInterfaceDeclaration classDcl;

    @Getter
    private final MethodDeclaration overridenMethod;

    @Getter
    private final MethodDeclaration overridingMethod;

    @Getter
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
        return new FragmentsSplitter(this.getOverridingMethod(), this.getSuperCall());
    }

    public CandidateWithVariables toCandidateWithVariables() {
        return new CandidateWithVariables(this,
                this.toFragment().getBeforeVariablesUsedInSpecificNodeAndBeforeFragments());
    }

    public record CandidateWithVariables(ZafeirisEtAl2016Canditate candidate,
                                         Collection<VariableDeclarationExpr> variables) {
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ZafeirisEtAl2016Canditate another) {
            return new EqualsBuilder().append(id, another.id).isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

}
