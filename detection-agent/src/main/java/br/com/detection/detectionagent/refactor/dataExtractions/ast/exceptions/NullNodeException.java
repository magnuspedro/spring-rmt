package br.com.detection.detectionagent.refactor.dataExtractions.ast.exceptions;

public class NullNodeException extends AstHandlerException {
    public NullNodeException() {
        super("Node cannot be null");
    }
}
