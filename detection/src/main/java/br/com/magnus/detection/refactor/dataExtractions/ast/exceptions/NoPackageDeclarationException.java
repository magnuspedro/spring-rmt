package br.com.magnus.detection.refactor.dataExtractions.ast.exceptions;

public class NoPackageDeclarationException extends AstHandlerException {
    public NoPackageDeclarationException() {
        super("Package declaration not be found");
    }
}
