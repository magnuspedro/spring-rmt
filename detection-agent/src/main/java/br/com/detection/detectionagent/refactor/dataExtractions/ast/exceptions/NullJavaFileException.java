package br.com.detection.detectionagent.refactor.dataExtractions.ast.exceptions;

public class NullJavaFileException extends RuntimeException {
    public NullJavaFileException() {
        super("Java file cannot be null");
    }
}
