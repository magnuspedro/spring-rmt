package br.com.magnus.metricscalculator.qualityAttributes.calculator;

import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;
import br.com.magnus.metricscalculator.metrics.Metric;
import br.com.magnus.metricscalculator.qualityAttributes.QualityAttribute;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReusabilityCalculatorTest {

    @Test
    @DisplayName("Calculates reusability correctly when all metrics are positive")
    void calculatesReusabilityCorrectlyWhenAllMetricsArePositive() {
        ReusabilityCalculator calculator = new ReusabilityCalculator();
        Map<Metric, BigDecimal> metrics = new HashMap<>();
        metrics.put(Metric.DEPTH_OF_INHERITANCE_TREE, BigDecimal.valueOf(20));
        metrics.put(Metric.LINES_OF_CODE, BigDecimal.valueOf(30));

        QualityAttributeResult result = calculator.calculate(metrics);

        assertEquals(QualityAttribute.REUSABILITY.name(), result.qualityAttributeName());
        assertEquals(BigDecimal.valueOf(25), result.changePercentage());
    }

    @Test
    @DisplayName("Calculates reusability correctly when some metrics are zero")
    void calculatesReusabilityCorrectlyWhenSomeMetricsAreZero() {
        ReusabilityCalculator calculator = new ReusabilityCalculator();
        Map<Metric, BigDecimal> metrics = new HashMap<>();
        metrics.put(Metric.DEPTH_OF_INHERITANCE_TREE, BigDecimal.ZERO);
        metrics.put(Metric.LINES_OF_CODE, BigDecimal.valueOf(30));

        QualityAttributeResult result = calculator.calculate(metrics);

        assertEquals(QualityAttribute.REUSABILITY.name(), result.qualityAttributeName());
        assertEquals(BigDecimal.valueOf(15), result.changePercentage());
    }

    @Test
    @DisplayName("Calculates reusability correctly when all metrics are zero")
    void calculatesReusabilityCorrectlyWhenAllMetricsAreZero() {
        ReusabilityCalculator calculator = new ReusabilityCalculator();
        Map<Metric, BigDecimal> metrics = new HashMap<>();
        metrics.put(Metric.DEPTH_OF_INHERITANCE_TREE, BigDecimal.ZERO);
        metrics.put(Metric.LINES_OF_CODE, BigDecimal.ZERO);

        QualityAttributeResult result = calculator.calculate(metrics);

        assertEquals(QualityAttribute.REUSABILITY.name(), result.qualityAttributeName());
        assertEquals(BigDecimal.ZERO, result.changePercentage());
    }
}