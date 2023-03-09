package br.com.metrics.metricsagent.pulse;

import br.com.messages.members.Member;
import br.com.messages.members.MemberType;
import br.com.messages.pulses.Pulse;
import br.com.metrics.metricsagent.domain.identity.Identity;
import br.com.metrics.metricsagent.ws.core.PulseClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PulseManagerImpl implements PulseManager {

    private final PulseClient pulseClient;

    private final Pulse pulse = new Pulse(Identity.ID, MemberType.PATTERNS_METRICS_EVALUATOR);

    @Scheduled(fixedRate = 60000)
    public void sendPulse() {
        log.info("Metrics - Sending pulse. Beggining registration...");

        try {
            pulseClient.sendPulse(pulse);
            log.info("Metrics - Pulse accepted.");
        } catch (FeignException.FeignClientException e) {
            if (e.status() == HttpStatus.PRECONDITION_FAILED.value()) {
                registerAsMember();
                return;
            }

            log.error("Metrics - Response not identified - ", e);

        } catch (FeignException.FeignServerException e) {
            log.error("Metrics - Response not identified - ", e);
        }
    }

    public void registerAsMember() {
        final Member member = Identity.getAsMember();

        log.info("Metrics - Membro {} não registrado. Iniciando registro...", member.getMemberId());
        log.info("Member: {}", member);

        try {
            var response = pulseClient.register(member);
            log.info("Metrics - Registration Response: {}", response);
            log.info("Metrics - Registration Response Entity: {}", response.getBody());
        } catch (Exception e) {

            log.error("Metrics - Failed while registering - ", e);
        }
    }
}
