package br.com.metrics.metricsagent.ws.boundaries;

import br.com.messages.files.FileRepositoryCollections;
import br.com.messages.members.api.metrics.MetricsAgentApi;
import br.com.messages.members.metrics.QualityAttributeResult;
import br.com.messages.projects.Project;
import br.com.messages.projects.ProjectsReadonlyRepository;
import br.com.metrics.metricsagent.pulse.PulseManager;
import br.com.metrics.metricsagent.qualityAttributes.QualityAttributesProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Collection;

import static br.com.messages.members.api.metrics.MetricsAgentApi.METRICS_PATH;
import static br.com.messages.members.api.metrics.MetricsAgentApi.ROOT;

@RestController
@RequestMapping(METRICS_PATH + ROOT)
@RequiredArgsConstructor
public class MetricsBoundary implements Serializable {

    private final QualityAttributesProcessor processor;

    private final ProjectsReadonlyRepository projectsRepository;

    private final PulseManager pulseManager;

    @GetMapping(MetricsAgentApi.PROJECT_PARAM)
    public Collection<QualityAttributeResult> evaluate(@PathVariable("projectId") String projectId, @PathVariable("refactoredProjectId") String refactoredProjectId) {

        final Project project = this.projectsRepository.get(FileRepositoryCollections.PROJECTS, projectId).orElseThrow(IllegalArgumentException::new);
        final Project refactoredProject = this.projectsRepository.get(FileRepositoryCollections.REFACTORED_PROJECTS, refactoredProjectId).orElseThrow(IllegalArgumentException::new);

        return processor.extract(project, refactoredProject);
    }

    @PostMapping(MetricsAgentApi.FORCE_REGISTRATION)
    public void forceRegistration() {
        this.pulseManager.registerAsMember();
    }

}
