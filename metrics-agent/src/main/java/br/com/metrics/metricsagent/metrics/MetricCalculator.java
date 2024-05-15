package br.com.metrics.metricsagent.metrics;

import java.math.BigDecimal;
import java.util.Map;

public interface MetricCalculator {

    Map<Metric,BigDecimal> calculate(MetricsResolver original, MetricsResolver refactored);

}
