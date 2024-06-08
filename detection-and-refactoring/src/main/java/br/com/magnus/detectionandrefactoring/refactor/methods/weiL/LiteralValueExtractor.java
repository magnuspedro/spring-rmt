package br.com.magnus.detectionandrefactoring.refactor.methods.weiL;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;

import java.util.Optional;

public class LiteralValueExtractor {

    public static Optional<Object> extractValidLiteralFromNode(BinaryExpr node, MethodDeclaration method) {
        var variableName = AstHandler.getNameExpr(node)
                .map(NameExpr::getNameAsString)
                .orElse("");
        var validVariable = method.getParameters().stream()
                .filter(parameter -> parameter.getNameAsString().equals(variableName))
                .toList();

        if (validVariable.isEmpty()) {
            return Optional.empty();
        }

        return node.getChildNodes().stream()
                .filter(LiteralExpr.class::isInstance)
                .map(LiteralExpr.class::cast)
                .map(LiteralValueExtractor::extractLiteralValidValues)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Optional<Object> extractLiteralValidValues(LiteralExpr expr) {
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
