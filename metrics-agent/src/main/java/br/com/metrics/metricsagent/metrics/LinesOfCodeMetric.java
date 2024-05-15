package br.com.metrics.metricsagent.metrics;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class LinesOfCodeMetric implements MetricCalculator {
    @Override
    public Map<Metric, BigDecimal> calculate(MetricsResolver original, MetricsResolver refactored) {
        return Map.of(Metric.LINES_OF_CODE, ProportionCalculator.calculateInverse(original.getLinesOfCode(), refactored.getLinesOfCode()));
    }
}
