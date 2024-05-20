package br.com.magnus.detection.refactor.dataExtractions.ast.exceptions;

public class NullJavaFileException extends RuntimeException {
    public NullJavaFileException() {
        super("Java file cannot be null");
    }
}
