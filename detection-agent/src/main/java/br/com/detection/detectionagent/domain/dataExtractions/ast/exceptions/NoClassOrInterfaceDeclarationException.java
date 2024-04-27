package br.com.detection.detectionagent.domain.dataExtractions.ast.exceptions;

public class NoClassOrInterfaceDeclarationException extends AstHandlerException {
    public NoClassOrInterfaceDeclarationException() {
        super("No class or interface declaration found");
    }
}
