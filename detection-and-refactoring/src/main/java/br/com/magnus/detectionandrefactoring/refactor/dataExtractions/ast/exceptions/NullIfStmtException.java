package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.exceptions;

public class NullIfStmtException extends AstHandlerException {
    public NullIfStmtException() {
        super("IfStmt cannot be null");
    }
}
