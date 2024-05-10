package br.com.detection.detectionagent.refactor.dataExtractions.ast.exceptions;

public class VariableDeclarationExpectedException extends AstHandlerException {
    public VariableDeclarationExpectedException() {
        super("Variable declaration is expected as a parameter");
    }
}
