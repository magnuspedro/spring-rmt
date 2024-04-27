package br.com.detection.detectionagent.domain.dataExtractions.ast.exceptions;

public class NullMethodException extends AstHandlerException {
    public NullMethodException() {
        super("Method cannot be null");
    }
}
