package br.com.metrics.metricsagent.consumer;

import br.com.metrics.metricsagent.extraction.ExtractProjects;
import br.com.metrics.metricsagent.qualityAttributes.QualityAttributesProcessor;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsConsumer {

    private final ExtractProjects extractProjects;
    private final QualityAttributesProcessor processor;

    @SqsListener("${sqs.measure-pattern}")
    public void listener(String id) {
        log.info("Project consumed id: {}", id);
        var paths = extractProjects.extractProject(id);

        var quality = processor.extract(paths.get("original"), paths.get("refactored"));
        log.info("Quality attributes extracted: {}", quality);
    }

}
