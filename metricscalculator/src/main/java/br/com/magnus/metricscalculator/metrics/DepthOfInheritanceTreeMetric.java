package br.com.magnus.metricscalculator.metrics;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class DepthOfInheritanceTreeMetric implements MetricCalculator {
    @Override
    public Map<Metric, BigDecimal> calculate(MetricsResolver original, MetricsResolver refactored) {
        return Map.of(Metric.DEPTH_OF_INHERITANCE_TREE, ProportionCalculator.calculateDirect(original.getDepthOfInheritanceTree(), refactored.getDepthOfInheritanceTree()));
    }
}
