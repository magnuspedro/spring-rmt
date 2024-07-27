package br.com.magnus.detectionandrefactoring.consumer;

import br.com.magnus.config.starter.message.Message;
import com.github.sonus21.rqueue.annotation.RqueueListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "rqueue.enabled", havingValue = "true")
public class RefactorCandidateRedisConsumer {

    private final ProcessRefactorCandidate processRefactorCandidate;

    @RqueueListener(value = "${queue.detect-pattern}")
    public void listener(Message message) {
        processRefactorCandidate.process(message.id());
    }

}