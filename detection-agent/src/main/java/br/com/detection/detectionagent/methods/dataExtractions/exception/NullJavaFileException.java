package br.com.detection.detectionagent.methods.dataExtractions.exception;

public class NullJavaFileException extends RuntimeException {
    public NullJavaFileException() {
        super("Java file cannot be null");
    }
}
