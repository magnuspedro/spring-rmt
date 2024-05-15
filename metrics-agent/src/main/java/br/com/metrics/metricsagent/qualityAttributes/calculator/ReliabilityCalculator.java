package br.com.metrics.metricsagent.qualityAttributes.calculator;

import br.com.magnus.config.starter.members.metrics.BasicQualityAttributeResult;
import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;
import br.com.metrics.metricsagent.metrics.Metric;
import br.com.metrics.metricsagent.qualityAttributes.QualityAttribute;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class ReliabilityCalculator implements QualityAttributeCalculator {
    private static final BigDecimal METRICS_NUMBER = BigDecimal.valueOf(2);

    @Override
    public QualityAttributeResult calculate(Map<Metric, BigDecimal> metrics) {
        var value = Stream.of(metrics.get(Metric.CYCLOMATIC_COMPLEXITY),
                        metrics.get(Metric.LINES_OF_CODE))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(METRICS_NUMBER, RoundingMode.HALF_EVEN);
        return new BasicQualityAttributeResult(QualityAttribute.RELIABILITY.name(), value);
    }
}
