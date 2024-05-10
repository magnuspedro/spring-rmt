package br.com.detection.detectionagent.refactor.dataExtractions.ast.exceptions;

public class NullCompilationUnitException extends AstHandlerException {
    public NullCompilationUnitException() {
        super("Compilation unit cannot be null");
    }
}
