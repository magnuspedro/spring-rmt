package br.com.magnus.projectsyncbff.validation;

import lombok.Getter;

import java.util.List;

@Getter
public class UploadValidationException extends RuntimeException {

    private final List<String> errors;

    public UploadValidationException(List<String> errors) {
        super("Upload validation failed");
        this.errors = List.copyOf(errors);
    }
}
