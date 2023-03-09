package br.com.metrics.metricsagent.ws.core;

import br.com.messages.members.Member;
import br.com.messages.members.api.intermediary.IntermediaryAgentCoreApi;
import br.com.messages.members.api.intermediary.IntermediaryAgentPulsesApi;
import br.com.messages.pulses.Pulse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "pulse-client", url = "${services.intermediary}")
public interface PulseClient {

    @PostMapping(path = IntermediaryAgentCoreApi.AGENT_PATH + IntermediaryAgentPulsesApi.ROOT + IntermediaryAgentPulsesApi.RENEW, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> sendPulse(Pulse pulse);

    @PostMapping(path = IntermediaryAgentCoreApi.AGENT_PATH + IntermediaryAgentPulsesApi.ROOT + IntermediaryAgentPulsesApi.REGISTRATION, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> register(Member member);
}
