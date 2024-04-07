package br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions;

public class NullNameException extends AstHandlerException {
    public NullNameException() {
        super("Name cannot be null");
    }
}
