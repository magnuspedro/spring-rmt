package br.com.detection.detectionagent.ws.boundaries;

import br.com.detection.detectionagent.methods.DetectionMethodsManager;
import br.com.detection.detectionagent.pulse.PulseManager;
import br.com.messages.members.api.detectors.DetectionAgentApi;
import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static br.com.messages.members.api.detectors.DetectionAgentApi.DETECTION_PATH;
import static br.com.messages.members.api.detectors.DetectionAgentApi.ROOT;

@RestController
@RequestMapping(DETECTION_PATH + ROOT)
@RequiredArgsConstructor
public class DetectorBoundary implements Serializable {

    private final DetectionMethodsManager detectionMethodsManager;

    private final PulseManager pulseManager;


    @GetMapping(DetectionAgentApi.START_DETECTION_WITH_PARAMS)
    public List<RefactoringCandidate> requestEvaluation(@PathVariable("projectId") String projectId) {
        return detectionMethodsManager.extractCandidates(projectId);
    }

    @PostMapping(DetectionAgentApi.REFACTOR_WITH_PARAMS)
    public String applyPatterns(@PathVariable("projectId") String projectId, Collection<RefactoringCandidadeDTO> eligiblePatterns) {
        final String refactoredProjectId = detectionMethodsManager.refactor(projectId, eligiblePatterns);
        return refactoredProjectId;
    }

    @GetMapping(DetectionAgentApi.RETRIEVE_REFERENCES)
    public JsonArray getReferences() {
        final JsonArrayBuilder builder = Json.createArrayBuilder();

        this.detectionMethodsManager.getReferences().stream().map(JsonUtils::toJson).forEach(builder::add);

        return builder.build();
    }

    @PostMapping(DetectionAgentApi.FORCE_REGISTRATION)
    public void forceRegistration() {
        this.pulseManager.registerAsMember();
    }

}
