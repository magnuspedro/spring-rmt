package br.com.magnus.config.starter.projects;

import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;
import br.com.magnus.config.starter.patterns.DesignPattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@ToString
public final class CandidateInformation {
    private final Reference reference;
    private final String id;
    @Builder.Default
    private final Set<String> filesChanged = new HashSet<>();
    private final DesignPattern designPattern;
    @Setter
    private List<QualityAttributeResult> metrics;
}
