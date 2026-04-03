package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.transformations;

import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns.AbstractAccessMiniTransformation;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns.AbstractionMiniTransformation;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns.EncapsulateConstructionMiniTransformation;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

import java.util.Set;

public class AbstractFactoryPatternTransformation implements PatternTransformation {

    private final Set<String> productClasses;
    private final String newFactoryName;
    private final String newAbstractFactoryName;

    public AbstractFactoryPatternTransformation(Set<String> productClasses, String newFactoryName, String newAbstractFactoryName) {
        this.productClasses = productClasses;
        this.newFactoryName = newFactoryName;
        this.newAbstractFactoryName = newAbstractFactoryName;
    }

    @Override
    public void apply(CinneideContext context) {
        var firstProduct = productClasses.stream().findFirst().orElseThrow();
        var firstProductFile = context.classFile(firstProduct);
        var factoryCu = new CompilationUnit();
        firstProductFile.getCompilationUnit().getPackageDeclaration().ifPresent(pkg -> factoryCu.setPackageDeclaration(pkg.getNameAsString()));
        factoryCu.addClass(newFactoryName);
        context.addClass(newFactoryName, firstProductFile.getPath(), factoryCu);

        for (var product : productClasses) {
            var productInterface = product + "Interface";
            new AbstractionMiniTransformation(product, productInterface).apply(context);
            context.files().forEach(file -> {
                if (!file.getFileNameWithoutExtension().equals(newFactoryName)) {
                    new AbstractAccessMiniTransformation(file.getFileNameWithoutExtension(), product, productInterface, Set.of()).apply(context);
                }
            });
            new EncapsulateConstructionMiniTransformation(newFactoryName, product, "create" + product).apply(context);
        }

        new SingletonPatternTransformation(newFactoryName, newAbstractFactoryName).apply(context);

        context.files().forEach(file -> file.getCompilationUnit().findAll(ObjectCreationExpr.class).stream()
                .filter(expr -> productClasses.contains(expr.getType().getNameAsString()))
                .forEach(expr -> {
                    var product = expr.getType().getNameAsString();
                    var getInstance = new MethodCallExpr(new com.github.javaparser.ast.expr.NameExpr(newAbstractFactoryName), "getInstance");
                    var create = new MethodCallExpr(getInstance, "create" + product);
                    expr.replace(create);
                }));
    }
}
