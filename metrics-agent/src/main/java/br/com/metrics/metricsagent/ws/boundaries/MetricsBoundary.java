package br.com.metrics.metricsagent.ws.boundaries;

import br.com.messages.members.RestPatterns;
import br.com.messages.members.api.metrics.MetricsAgentApi;
import br.com.messages.members.metrics.QualityAttributeResult;
import br.com.messages.projects.Project;
import br.com.metrics.metricsagent.domain.files.FileRepositoryCollections;
import br.com.metrics.metricsagent.project.ProjectsReadonlyRepository;
import br.com.metrics.metricsagent.pulse.PulseManager;
import br.com.metrics.metricsagent.qualityAttributes.QualityAttributesProcessor;
import jakarta.ws.rs.*;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Collection;

@Path(MetricsAgentApi.ROOT)
@Produces(RestPatterns.PRODUCES_JSON)
@Consumes(RestPatterns.CONSUMES_JSON)
@RequiredArgsConstructor
public class MetricsBoundary implements Serializable {

	private static final long serialVersionUID = 1L;

	private final QualityAttributesProcessor processor;

	private final ProjectsReadonlyRepository projectsRepository;
	
	private final PulseManager pulseManager;

	@GET
	@Path(MetricsAgentApi.PROJECT_PARAM)
	public Collection<QualityAttributeResult> evaluate(@PathParam("projectId") String projectId,
			@PathParam("refactoredProjectId") String refactoredProjectId) {

		final Project project = this.projectsRepository.get(FileRepositoryCollections.PROJECTS, projectId)
				.orElseThrow(IllegalArgumentException::new);

		final Project refactoredProject = this.projectsRepository
				.get(FileRepositoryCollections.REFACTORED_PROJECTS, refactoredProjectId)
				.orElseThrow(IllegalArgumentException::new);

		return processor.extract(project, refactoredProject);
	}
	
	@POST
	@Path(MetricsAgentApi.FORCE_REGISTRATION)
	public void forceRegistration() {
		this.pulseManager.registerAsMember();
	}

}
