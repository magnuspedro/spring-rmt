package br.com.magnus.detection.refactor.dataExtractions.ast.exceptions;

public class NullCompilationUnitException extends AstHandlerException {
    public NullCompilationUnitException() {
        super("Compilation unit cannot be null");
    }
}
