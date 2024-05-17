package br.com.metrics.metricsagent.qualityAttributes.calculator;

import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;
import br.com.metrics.metricsagent.metrics.Metric;
import br.com.metrics.metricsagent.qualityAttributes.QualityAttribute;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReliabilityCalculatorTest {

    @Test
    @DisplayName("Calculates reliability correctly when all metrics are positive")
    void calculatesReliabilityCorrectlyWhenAllMetricsArePositive() {
        ReliabilityCalculator calculator = new ReliabilityCalculator();
        Map<Metric, BigDecimal> metrics = new HashMap<>();
        metrics.put(Metric.CYCLOMATIC_COMPLEXITY, BigDecimal.valueOf(20));
        metrics.put(Metric.LINES_OF_CODE, BigDecimal.valueOf(30));

        QualityAttributeResult result = calculator.calculate(metrics);

        assertEquals(QualityAttribute.RELIABILITY.name(), result.qualityAttributeName());
        assertEquals(BigDecimal.valueOf(25), result.changePercentage());
    }

    @Test
    @DisplayName("Calculates reliability correctly when some metrics are zero")
    void calculatesReliabilityCorrectlyWhenSomeMetricsAreZero() {
        ReliabilityCalculator calculator = new ReliabilityCalculator();
        Map<Metric, BigDecimal> metrics = new HashMap<>();
        metrics.put(Metric.CYCLOMATIC_COMPLEXITY, BigDecimal.ZERO);
        metrics.put(Metric.LINES_OF_CODE, BigDecimal.valueOf(30));

        QualityAttributeResult result = calculator.calculate(metrics);

        assertEquals(QualityAttribute.RELIABILITY.name(), result.qualityAttributeName());
        assertEquals(BigDecimal.valueOf(15), result.changePercentage());
    }

    @Test
    @DisplayName("Calculates reliability correctly when all metrics are zero")
    void calculatesReliabilityCorrectlyWhenAllMetricsAreZero() {
        ReliabilityCalculator calculator = new ReliabilityCalculator();
        Map<Metric, BigDecimal> metrics = new HashMap<>();
        metrics.put(Metric.CYCLOMATIC_COMPLEXITY, BigDecimal.ZERO);
        metrics.put(Metric.LINES_OF_CODE, BigDecimal.ZERO);

        QualityAttributeResult result = calculator.calculate(metrics);

        assertEquals(QualityAttribute.RELIABILITY.name(), result.qualityAttributeName());
        assertEquals(BigDecimal.ZERO, result.changePercentage());
    }
}