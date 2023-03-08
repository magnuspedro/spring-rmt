package br.com.intermediary.intermediaryagent.ws.boundaries;

import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.com.messages.files.FileRepositoryCollections;
import br.com.messages.members.api.intermediary.IntermediaryAgentCoreApi;
import br.com.messages.projects.ProjectsRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import br.com.intermediary.intermediaryagent.managers.members.MembersManager;
import br.com.intermediary.intermediaryagent.managers.projects.ProjectsManager;
import br.com.messages.members.api.intermediary.IntermediaryAgentProjectsApi;
import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.members.candidates.evaluation.EvaluationDTO;
import br.com.messages.projects.Project;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Slf4j
@RestController
@RequestMapping(IntermediaryAgentCoreApi.AGENT_PATH+IntermediaryAgentProjectsApi.ROOT)
@RequiredArgsConstructor
public class IntermediaryBoundary implements Serializable {

	private final FileRepositoryCollections PROJECTS_COLLECTION = FileRepositoryCollections.PROJECTS;

	private final ProjectsManager projectsManager;

	private final MembersManager membersManager;

	private final ProjectsRepository projectsRepository;

	@GetMapping(path = IntermediaryAgentProjectsApi.TEST)
	public Integer test() {

		log.info("Test Projects!");

		return 1;
	}

	@PostMapping(path = IntermediaryAgentProjectsApi.REGISTRATION_WITH_PARAM)
	public String registration(@PathVariable("name") String name, @PathVariable("contentType") String contentType,
							   @NotNull InputStream inputStream) {

		try {
			Project project = new Project(UUID.randomUUID().toString(), name, () -> inputStream,
					new String(Base64.getDecoder().decode(contentType), "UTF-8"));

			projectsRepository.put(PROJECTS_COLLECTION, project);

			this.projectsManager.register((project = new Project(project.getId(), project.getName())));

			return project.getId();
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException();
		}
	}

	@GetMapping(path = IntermediaryAgentProjectsApi.EVALUATE_WITH_PARAM)
	public Collection<RefactoringCandidadeDTO> evaluation(@PathVariable("projectId") String projectId) {

		final Collection<RefactoringCandidadeDTO> result = new ArrayList<>();

		final Collection<RefactoringCandidadeDTO> candidates = this.projectsManager
				.evaluate(membersManager::getNextDetector, projectId).stream().distinct().toList();

		for (RefactoringCandidadeDTO candidate : candidates) {
			final String refactoredProjId = this.projectsManager.refactor(membersManager::getNextDetector, projectId,
					Stream.of(candidate).collect(Collectors.toList()));

			candidate.setEvaluation(new EvaluationDTO(
					this.projectsManager.evaluate(membersManager::getNextMetrics, projectId, refactoredProjId)));

			result.add(candidate);
		}
		
		log.info("Fim avaliação com {} candidatos", result.size());

		return result;
	}

	@PostMapping(path = IntermediaryAgentProjectsApi.APPLY_PATTERNS_WITH_PARAM)
	public StreamingResponseBody applyPatterns(HttpServletResponse response,
											   @PathVariable("projectId") String projectId, List<RefactoringCandidadeDTO> candidates) {

		final String refactoredId = this.projectsManager.refactor(membersManager::getNextDetector, projectId,
				candidates);

		final Project project = this.projectsRepository.get(FileRepositoryCollections.REFACTORED_PROJECTS, refactoredId)
				.orElseThrow(IllegalStateException::new);

		response.setContentType(project.getContentType());

		if (!StringUtils.isEmpty(project.getName())) {
			final String fileName = "attachment; filename="
					.concat(project.getName().replace(".zip", "").concat(".zip"));

			response.setHeader("Content-Disposition", fileName);
		}

		return project::sendContentTo;
	}

}
