package br.com.magnus.projectsyncbff.controller;

import br.com.magnus.projectsyncbff.refactor.SelectionValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(SelectionValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleSelectionValidationException(SelectionValidationException exception) {
        return Map.of(
                "message", exception.getMessage(),
                "missingCandidateIds", exception.getMissingCandidateIds()
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleResponseStatusException(ResponseStatusException exception) {
        return Map.of("message", exception.getReason());
    }
}
