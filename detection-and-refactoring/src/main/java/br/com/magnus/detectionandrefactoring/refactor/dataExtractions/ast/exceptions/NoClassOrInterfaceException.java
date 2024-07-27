package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.exceptions;

public class NoClassOrInterfaceException extends AstHandlerException {
    public NoClassOrInterfaceException() {
        super("No class or interface found in the compilation unit");
    }
}
