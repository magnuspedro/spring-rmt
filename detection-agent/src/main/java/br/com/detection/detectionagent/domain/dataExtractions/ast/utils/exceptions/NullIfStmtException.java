package br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions;

public class NullIfStmtException extends AstHandlerException {
    public NullIfStmtException() {
        super("IfStmt cannot be null");
    }
}
