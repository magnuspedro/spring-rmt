package br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions;

public class VariableDeclarationExpectedException extends AstHandlerException {
    public VariableDeclarationExpectedException() {
        super("Variable declaration is expected as a parameter");
    }
}
