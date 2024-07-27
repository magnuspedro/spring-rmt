package br.com.magnus.metricscalculator.consumer;

import br.com.magnus.config.starter.message.Message;
import com.github.sonus21.rqueue.annotation.RqueueListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "rqueue.enabled", havingValue = "true")
public class MetricsRedisConsumer {

    private final MetricsProcessor metricsProcessor;

    @RqueueListener("${queue.measure-pattern}")
    public void listener(Message message) {
        metricsProcessor.process(message.id());
    }
}
