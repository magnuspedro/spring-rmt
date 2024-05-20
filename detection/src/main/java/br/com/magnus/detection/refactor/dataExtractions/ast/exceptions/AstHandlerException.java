package br.com.magnus.detection.refactor.dataExtractions.ast.exceptions;

public class AstHandlerException extends RuntimeException {

        public AstHandlerException(String message) {
            super(message);
        }

        public AstHandlerException(String message, Throwable cause) {
            super(message, cause);
        }
}
