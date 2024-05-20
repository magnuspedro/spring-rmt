package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.exceptions;

public class NullMethodException extends AstHandlerException {
    public NullMethodException() {
        super("Method cannot be null");
    }
}
