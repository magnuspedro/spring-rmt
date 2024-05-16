package br.com.magnus.config.starter.projects;

import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.patterns.DesignPattern;
import lombok.Builder;

import java.util.List;

@Builder
public record CandidateInformation(
        Reference reference,
        String id,
        List<String> filesChanged,
        DesignPattern designPattern
) {
}
