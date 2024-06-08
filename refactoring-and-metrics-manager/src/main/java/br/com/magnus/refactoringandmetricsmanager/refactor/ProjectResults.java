package br.com.magnus.refactoringandmetricsmanager.refactor;

import br.com.magnus.config.starter.projects.CandidateInformation;
import br.com.magnus.config.starter.projects.ProjectStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record ProjectResults(String name, ProjectStatus status, List<CandidateInformation> candidatesInformation) {
}
