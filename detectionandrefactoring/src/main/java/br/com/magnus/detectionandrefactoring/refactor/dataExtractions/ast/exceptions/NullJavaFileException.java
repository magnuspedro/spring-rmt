package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.exceptions;

public class NullJavaFileException extends RuntimeException {
    public NullJavaFileException() {
        super("Java file cannot be null");
    }
}
