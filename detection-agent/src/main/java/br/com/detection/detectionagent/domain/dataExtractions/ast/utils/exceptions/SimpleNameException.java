package br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions;

public class SimpleNameException extends AstHandlerException{
    public SimpleNameException() {
        super("Simple name not found");
    }
}
