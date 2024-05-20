package br.com.magnus.metricscalculator.consumer;

import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.metricscalculator.extraction.ExtractProjects;
import br.com.magnus.metricscalculator.qualityAttributes.QualityAttributesProcessor;
import br.com.magnus.metricscalculator.repository.ProjectRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsConsumer {

    private final ExtractProjects extractProjects;
    private final QualityAttributesProcessor processor;
    private final ProjectRepository projectRepository;

    @SqsListener("${sqs.measure-pattern}")
    public void listener(String id) {
        log.info("Project consumed id: {}", id);
        var project = projectRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        var bucket = project.getBucket();
        var originalPath = extractProjects.extractProject(id, bucket);

        log.info("Extracting quality attributes extracted");
        project.getCandidatesInformation().forEach(candidate -> {
            var candidatePath = extractProjects.extractProject(candidate.getId(), bucket);
            var metrics = processor.extract(originalPath, candidatePath);
            candidate.setMetrics(metrics);
            log.info("Candidates information: {}", candidate);
        });

        project.addStatus(ProjectStatus.FINISHED);
        projectRepository.save(project);
    }
}
