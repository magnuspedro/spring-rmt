package br.com.detection.detectionagent.refactor.dataExtractions.ast.exceptions;

public class NoPackageDeclarationException extends AstHandlerException {
    public NoPackageDeclarationException() {
        super("Package declaration not be found");
    }
}
