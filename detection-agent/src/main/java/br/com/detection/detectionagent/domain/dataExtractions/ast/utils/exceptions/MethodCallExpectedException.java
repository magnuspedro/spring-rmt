package br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions;

public class MethodCallExpectedException extends AstHandlerException {
    public MethodCallExpectedException() {
        super("Method is expected as a parameter");
    }
}