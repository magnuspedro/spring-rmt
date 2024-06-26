package br.com.magnus.metricscalculator.metrics;

import java.math.BigDecimal;
import java.util.Map;

public interface MetricCalculator {

    Map<Metric,BigDecimal> calculate(MetricsResolver original, MetricsResolver refactored);

}
