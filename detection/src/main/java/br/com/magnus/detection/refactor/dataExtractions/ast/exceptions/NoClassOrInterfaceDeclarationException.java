package br.com.magnus.detection.refactor.dataExtractions.ast.exceptions;

public class NoClassOrInterfaceDeclarationException extends AstHandlerException {
    public NoClassOrInterfaceDeclarationException() {
        super("No class or interface declaration found");
    }
}
