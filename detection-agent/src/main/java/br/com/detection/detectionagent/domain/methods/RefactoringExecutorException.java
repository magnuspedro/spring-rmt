package br.com.detection.detectionagent.domain.methods;

public class RefactoringExecutorException extends RuntimeException {
    public RefactoringExecutorException(String s) {
        super(s);
    }

    public RefactoringExecutorException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
