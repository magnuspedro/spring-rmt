package br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions;

public class NullMethodException extends AstHandlerException {
    public NullMethodException() {
        super("Method cannot be null");
    }
}
