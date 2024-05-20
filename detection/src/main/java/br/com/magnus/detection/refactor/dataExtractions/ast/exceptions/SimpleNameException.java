package br.com.magnus.detection.refactor.dataExtractions.ast.exceptions;

public class SimpleNameException extends AstHandlerException{
    public SimpleNameException() {
        super("Simple name not found");
    }
}
