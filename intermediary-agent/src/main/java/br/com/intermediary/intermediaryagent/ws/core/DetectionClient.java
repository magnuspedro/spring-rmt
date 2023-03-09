package br.com.intermediary.intermediaryagent.ws.core;

import br.com.messages.members.api.detectors.DetectionAgentApi;
import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import br.com.messages.members.detectors.methods.Reference;

import java.net.URI;
import java.util.List;

@FeignClient(name = "detection-client", url="${services.detection}")
public interface DetectionClient {

    @GetMapping(path = DetectionAgentApi.DETECTION_PATH + DetectionAgentApi.ROOT + DetectionAgentApi.RETRIEVE_REFERENCES)
    List<Reference> getReferences(URI url);

    @PostMapping(path = DetectionAgentApi.DETECTION_PATH + DetectionAgentApi.ROOT + DetectionAgentApi.REFACTOR + "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    String refactor(URI url, @PathVariable("id") String projectId, List<RefactoringCandidadeDTO> candidates);

    @GetMapping(path = DetectionAgentApi.DETECTION_PATH + DetectionAgentApi.ROOT + DetectionAgentApi.START_DETECTION + "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    List<RefactoringCandidadeDTO> detect(URI url, @PathVariable("id") String projectId);
}
