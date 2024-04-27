package br.com.detection.detectionagent.domain.dataExtractions.ast.exceptions;

public class MethodCallExpectedException extends AstHandlerException {
    public MethodCallExpectedException() {
        super("Method is expected as a parameter");
    }
}
