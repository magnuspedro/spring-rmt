package br.com.detection.detectionagent.domain.dataExtractions.ast.exceptions;

public class ClassExpectedException extends AstHandlerException {
    public ClassExpectedException() {
        super("Class is expected as a parameter");
    }
}
