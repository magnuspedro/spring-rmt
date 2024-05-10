package br.com.detection.detectionagent.refactor.dataExtractions.ast.exceptions;

public class ClassExpectedException extends AstHandlerException {
    public ClassExpectedException() {
        super("Class is expected as a parameter");
    }
}
