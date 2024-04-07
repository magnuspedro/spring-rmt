package br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions;

public class NullCompilationUnitException extends AstHandlerException {
    public NullCompilationUnitException() {
        super("Compilation unit cannot be null");
    }
}
