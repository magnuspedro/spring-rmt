package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.exceptions;

public class ClassExpectedException extends AstHandlerException {
    public ClassExpectedException() {
        super("Class is expected as a parameter");
    }
}
