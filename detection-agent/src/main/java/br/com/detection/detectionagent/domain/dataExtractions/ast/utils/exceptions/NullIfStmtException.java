package br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions;

public class NullIfStmtException extends RuntimeException {
    public NullIfStmtException() {
        super("IfStmt cannot be null");
    }
}
