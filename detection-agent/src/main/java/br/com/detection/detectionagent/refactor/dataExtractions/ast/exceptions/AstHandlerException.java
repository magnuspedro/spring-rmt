package br.com.detection.detectionagent.refactor.dataExtractions.ast.exceptions;

public class AstHandlerException extends RuntimeException {

        public AstHandlerException(String message) {
            super(message);
        }

        public AstHandlerException(String message, Throwable cause) {
            super(message, cause);
        }
}
