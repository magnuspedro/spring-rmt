package br.com.magnus.metricscalculator.metrics;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;


@Component
public class CyclomaticComplexityMetric implements MetricCalculator {
    @Override
    public Map<Metric, BigDecimal> calculate(MetricsResolver original, MetricsResolver refactored) {
        return Map.of(Metric.CYCLOMATIC_COMPLEXITY, ProportionCalculator.calculateInverse(original.getCyclomaticComplexity(), refactored.getCyclomaticComplexity()));
    }
}
