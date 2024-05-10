package br.com.detection.detectionagent.refactor.dataExtractions.ast.exceptions;

public class NullIfStmtException extends AstHandlerException {
    public NullIfStmtException() {
        super("IfStmt cannot be null");
    }
}
