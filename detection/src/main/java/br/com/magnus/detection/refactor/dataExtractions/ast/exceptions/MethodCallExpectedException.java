package br.com.magnus.detection.refactor.dataExtractions.ast.exceptions;

public class MethodCallExpectedException extends AstHandlerException {
    public MethodCallExpectedException() {
        super("Method is expected as a parameter");
    }
}
