package br.com.intermediary.intermediaryagent.managers.projects;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import br.com.intermediary.intermediaryagent.ws.core.DetectionClient;
import br.com.intermediary.intermediaryagent.ws.core.MetricClient;
import br.com.messages.members.Member;
import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.members.metrics.QualityAttributeResultDTO;
import br.com.messages.projects.Project;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectsManagerImpl implements ProjectsManager {

    private final ProjectsPool projectsPool;

    private final DetectionClient detectionClient;

    private final MetricClient metricClient;

    @Override
    public void register(Project project) {
        projectsPool.register(project);
    }

    @Override
    public Collection<Project> getProjects() {
        return projectsPool.getAll();
    }

    @Override
    public List<RefactoringCandidadeDTO> evaluate(Supplier<Member> detector, String projectId) {
        checkId(projectId);
        var uri = URI.create("http://%s:%s".formatted(detector.get().getHost(), detector.get().getPort()));

        return detectionClient.detect(uri, projectId);
    }

    @Override
    public String refactor(Supplier<Member> detector, String projectId, List<RefactoringCandidadeDTO> candidates) {
        checkId(projectId);
        var uri = URI.create("http://%s:%s".formatted(detector.get().getHost(), detector.get().getPort()));

        return detectionClient.refactor(uri, projectId, candidates);
    }

    @Override
    public List<QualityAttributeResultDTO> evaluate(Supplier<Member> metrics, String projectId, String refactoredProjId) {
        checkId(projectId);
        checkId(refactoredProjId);
        var uri = URI.create("http://%s:%s".formatted(metrics.get().getHost(), metrics.get().getPort()));

        return metricClient.evaluate(uri, projectId, refactoredProjId);
    }

    @Override
    public Optional<Project> getProjectById(String id) {
        return projectsPool.getById(id);
    }

    private void checkId(String id) {
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException();
        }
    }
}
