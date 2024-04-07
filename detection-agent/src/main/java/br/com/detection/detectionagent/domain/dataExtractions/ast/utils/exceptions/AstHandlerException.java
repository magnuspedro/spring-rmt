package br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions;

public class AstHandlerException extends RuntimeException {

        public AstHandlerException(String message) {
            super(message);
        }

        public AstHandlerException(String message, Throwable cause) {
            super(message, cause);
        }
}
