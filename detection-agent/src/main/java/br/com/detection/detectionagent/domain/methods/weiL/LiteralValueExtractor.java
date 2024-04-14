package br.com.detection.detectionagent.domain.methods.weiL;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.AstHandler;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Predicate;

@Component
public class LiteralValueExtractor {

    private final AstHandler astHandler = new AstHandler();

    public Optional<Object> getNodeOtherThan(Node node, Parameter parameter) {
        final Predicate<Node> isAValidChild = (value) -> this.astHandler.getNameExpr(value)
                .map(n -> n.getNameAsString().equals(parameter.getNameAsString()))
                .isEmpty();

        return node.getChildNodes().stream()
                .filter(isAValidChild)
                .filter(LiteralExpr.class::isInstance)
                .map(LiteralExpr.class::cast)
                .map(this::extractLiteralValidValues)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private Optional<Object> extractLiteralValidValues(LiteralExpr expr) {
        if (expr instanceof CharLiteralExpr) {
            return Optional.ofNullable(((CharLiteralExpr) expr).getValue());
        } else if (expr instanceof IntegerLiteralExpr) {
            return Optional.ofNullable(((IntegerLiteralExpr) expr).getValue());
        } else if (expr instanceof LongLiteralExpr) {
            return Optional.ofNullable(((LongLiteralExpr) expr).getValue());
        } else if (expr instanceof LiteralStringValueExpr) {
            return Optional.ofNullable(((LiteralStringValueExpr) expr).getValue());
        }
        return Optional.empty();
    }

}
