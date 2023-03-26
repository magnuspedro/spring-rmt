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
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@ToString
public class ZafeirisEtAl2016Canditate implements RefactoringCandidate {

    private final String id = UUID.randomUUID().toString();

    private final Reference reference;

    private final JavaFile file;

    private final CompilationUnit compilationUnit;

    private final PackageDeclaration packageDcl;

    private final ClassOrInterfaceDeclaration classDcl;

    private final MethodDeclaration overridenMethod;

    private final MethodDeclaration overridingMethod;

    private final SuperExpr superCall;

    public ZafeirisEtAl2016Canditate(JavaFile file, CompilationUnit compilationUnit,
                                     PackageDeclaration packageDcl, ClassOrInterfaceDeclaration classDcl, MethodDeclaration overridenMethod,
                                     MethodDeclaration overridingMethod, SuperExpr superCall) {
        this.reference = Reference.builder()
                .title("Automated refactoring of super-class method invocations to the Template Method design pattern")
                .year(2016)
                .authors(List.of("E. A. Giakoumakis", "N. A. Diamantidis", "Sotiris H. Poulias", "Vassilis E. Zafeiris"))
                .build();
        this.file = file;
        this.compilationUnit = compilationUnit;
        this.packageDcl = packageDcl;
        this.classDcl = classDcl;
        this.overridenMethod = overridenMethod;
        this.overridingMethod = overridingMethod;
        this.superCall = superCall;
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
        return DesignPattern.TEMPLATE_METHOD;
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public MethodDeclaration getOverridingMethod() {
        return overridingMethod;
    }

    public MethodDeclaration getOverridenMethod() {
        return overridenMethod;
    }

    public PackageDeclaration getPackageDeclaration() {
        return packageDcl;
    }

    public ClassOrInterfaceDeclaration getClassDeclaration() {
        return classDcl;
    }

    public SuperExpr getSuperCall() {
        return superCall;
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
