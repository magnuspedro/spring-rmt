package br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions;

public class NullNodeException extends AstHandlerException {
    public NullNodeException() {
        super("Node cannot be null");
    }
}
