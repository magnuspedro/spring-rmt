package br.com.magnus.detection.refactor.dataExtractions.ast.exceptions;

public class VariableDeclarationExpectedException extends AstHandlerException {
    public VariableDeclarationExpectedException() {
        super("Variable declaration is expected as a parameter");
    }
}
