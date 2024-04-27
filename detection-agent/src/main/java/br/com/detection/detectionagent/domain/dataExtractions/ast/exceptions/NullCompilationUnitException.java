package br.com.detection.detectionagent.domain.dataExtractions.ast.exceptions;

public class NullCompilationUnitException extends AstHandlerException {
    public NullCompilationUnitException() {
        super("Compilation unit cannot be null");
    }
}
