package br.com.detection.detectionagent.ws.boundaries;

import br.com.detection.detectionagent.methods.DetectionMethodsManager;
import br.com.detection.detectionagent.pulse.PulseManager;
import br.com.messages.members.RestPatterns;
import br.com.messages.members.api.detectors.DetectionAgentApi;
import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.utils.JsonUtils;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.ws.rs.*;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Collection;

@Path(DetectionAgentApi.ROOT)
@Produces(RestPatterns.PRODUCES_JSON)
@Consumes(RestPatterns.CONSUMES_JSON)
@RequiredArgsConstructor
public class DetectorBoundary implements Serializable {

	private static final long serialVersionUID = 1L;

	private final DetectionMethodsManager detectionMethodsManager;
	
	private final PulseManager pulseManager;

	/**
	 * End point that receives a HTTP GET request. 
	 * It calls the detectionMethodsManager.
	 * @param projectId
	 * @return Returns a JsonArray with the refactoring candidates. 
	 */
	@GET
	@Path(DetectionAgentApi.START_DETECTION_WITH_PARAMS)
	public JsonArray requestEvaluation(@PathParam("projectId") String projectId) {
		final JsonArrayBuilder builder = Json.createArrayBuilder();

		detectionMethodsManager.extractCandidates(projectId).stream().map(JsonUtils::toJson).forEach(builder::add);

		return builder.build();
	}

	@POST
	@Path(DetectionAgentApi.REFACTOR_WITH_PARAMS)
	public String applyPatterns(@PathParam("projectId") String projectId, Collection<RefactoringCandidadeDTO> eligiblePatterns) {
		final String refactoredProjectId = detectionMethodsManager.refactor(projectId, eligiblePatterns);
		return refactoredProjectId;
	}

	@GET
	@Path(DetectionAgentApi.RETRIEVE_REFERENCES)
	public JsonArray getReferences() {
		final JsonArrayBuilder builder = Json.createArrayBuilder();

		this.detectionMethodsManager.getReferences().stream().map(JsonUtils::toJson).forEach(builder::add);

		return builder.build();
	}
	
	@POST
	@Path(DetectionAgentApi.FORCE_REGISTRATION)
	public void forceRegistration() {
		this.pulseManager.registerAsMember();
	}

}
