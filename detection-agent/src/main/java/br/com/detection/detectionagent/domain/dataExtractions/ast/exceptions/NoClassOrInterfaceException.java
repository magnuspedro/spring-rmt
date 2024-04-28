package br.com.detection.detectionagent.domain.dataExtractions.ast.exceptions;

public class NoClassOrInterfaceException extends AstHandlerException {
    public NoClassOrInterfaceException() {
        super("No class or interface found in the compilation unit");
    }
}