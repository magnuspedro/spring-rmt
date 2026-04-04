package br.com.magnus.projectsyncbff.refactor;

import lombok.Getter;

import java.util.List;

@Getter
public class SelectionValidationException extends RuntimeException {
    private final List<String> missingCandidateIds;

    public SelectionValidationException(String message, List<String> missingCandidateIds) {
        super(message);
        this.missingCandidateIds = missingCandidateIds;
    }
}
