package br.com.magnus.metricscalculator.consumer;

import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.metricscalculator.qualityAttributes.QualityAttributesProcessor;
import br.com.magnus.metricscalculator.repository.ExtractProjects;
import br.com.magnus.metricscalculator.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsProcessor {

    private final ExtractProjects extractProjects;
    private final QualityAttributesProcessor processor;
    private final ProjectRepository projectRepository;

    public void process(String id) {
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
        project.setUpdatedAt(System.nanoTime());
        projectRepository.save(project);
    }
}
