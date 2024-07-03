package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.exceptions;

public class VariableNotFoundException extends RuntimeException {
    public VariableNotFoundException() {
        super("Variable not found in the AST.");
    }
}
