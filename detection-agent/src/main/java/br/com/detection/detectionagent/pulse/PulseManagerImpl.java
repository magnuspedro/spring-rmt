package br.com.detection.detectionagent.pulse;

import br.com.detection.detectionagent.domain.identity.Identity;
import br.com.detection.detectionagent.ws.core.PulseClient;
import br.com.messages.members.Member;
import br.com.messages.members.MemberType;
import br.com.messages.pulses.Pulse;
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
    private static final long serialVersionUID = 1L;

    private final Pulse pulse = new Pulse(Identity.ID, MemberType.PATTERNS_SPOTS_DETECTOR);

    @Scheduled(fixedDelay = 60000)
    public void sendPulse() {
        log.info("Detector - Sending pulse. Beggining registration...");

        try {
            pulseClient.sendPulse(pulse);
            log.info("Detector - Pulse accepted.");
        } catch (FeignException.FeignClientException e) {
            if (e.status() == HttpStatus.PRECONDITION_FAILED.value()) {
                registerAsMember();
                return;
            }
            log.info("Detector - Client Error Response not identified - ", e);
        }
        catch(FeignException.FeignServerException e){
            log.info("Detector - Server Error Response not identified - ", e);
        }
    }

    public void registerAsMember() {
        final Member member = Identity.getAsMember();

        log.info("Detector - Membro {} não registrado. Iniciando registro...", member.getMemberId());
        log.info("Member: {}", member);

        try {
            var response = pulseClient.register(member);
            log.info("Detector - Registration Response: {}", response);
            log.info("Detector - Registration Response Entity: {}", response.getBody());
            log.info("Detector - Registration Succeeded!");
        } catch (Exception e) {
            log.error("Detector - Failed while registering - ", e);
        }
    }
}
