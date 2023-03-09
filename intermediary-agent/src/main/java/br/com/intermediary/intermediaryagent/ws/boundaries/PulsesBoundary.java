package br.com.intermediary.intermediaryagent.ws.boundaries;

import java.io.Serializable;

import br.com.messages.members.api.intermediary.IntermediaryAgentCoreApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import br.com.intermediary.intermediaryagent.managers.members.MembersManager;
import br.com.intermediary.intermediaryagent.managers.members.exceptions.pulses.PulseException;
import br.com.messages.members.Member;
import br.com.messages.members.api.intermediary.IntermediaryAgentPulsesApi;
import br.com.messages.pulses.Pulse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping(IntermediaryAgentCoreApi.AGENT_PATH+IntermediaryAgentPulsesApi.ROOT)
@RequiredArgsConstructor
public class PulsesBoundary implements Serializable {

	private static final String APPLICATION_JSON= "application/json";

	private final MembersManager membersManager;

	@GetMapping(path = IntermediaryAgentPulsesApi.TEST, consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
	public Integer test() {

		log.info("Test!");

		return 1;
	}

	@PostMapping(path = IntermediaryAgentPulsesApi.RENEW, consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
	public ResponseEntity renew(@RequestBody  Pulse pulse) {

		try {
			log.info("Recebido pulso {}.", pulse);
			
			membersManager.renewAvailability(pulse);

			return ResponseEntity.ok().build();
		} catch (PulseException e) {
			return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(e.getMessage());
		}
	}

	@PostMapping(path = IntermediaryAgentPulsesApi.REGISTRATION, consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
	public ResponseEntity register(@RequestBody Member member) {

		try {
			log.info("Membro {} registrado.", member);
			
			membersManager.register(member);

			return ResponseEntity.ok().build();
		} catch (PulseException e) {
			return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(e.getMessage());
		}
	}

}
