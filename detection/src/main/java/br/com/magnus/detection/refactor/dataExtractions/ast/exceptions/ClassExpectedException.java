package br.com.magnus.detection.refactor.dataExtractions.ast.exceptions;

public class ClassExpectedException extends AstHandlerException {
    public ClassExpectedException() {
        super("Class is expected as a parameter");
    }
}
