package br.com.magnus.metricscalculator.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "rqueue.enabled", havingValue = "false")
public class MetricsSqsConsumer {

    private final MetricsProcessor metricsProcessor;

    @SqsListener("${queue.measure-pattern}")
    public void listener(String id) {
        metricsProcessor.process(id);
    }
}
