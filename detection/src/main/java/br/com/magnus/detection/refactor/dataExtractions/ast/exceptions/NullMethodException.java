package br.com.magnus.detection.refactor.dataExtractions.ast.exceptions;

public class NullMethodException extends AstHandlerException {
    public NullMethodException() {
        super("Method cannot be null");
    }
}
