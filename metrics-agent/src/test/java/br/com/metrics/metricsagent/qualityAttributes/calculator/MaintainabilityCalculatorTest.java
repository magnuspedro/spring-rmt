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

class MaintainabilityCalculatorTest {

    @Test
    @DisplayName("Calculates maintainability correctly when all metrics are positive")
    void calculatesMaintainabilityCorrectlyWhenAllMetricsArePositive() {
        MaintainabilityCalculator calculator = new MaintainabilityCalculator();
        Map<Metric, BigDecimal> metrics = new HashMap<>();
        metrics.put(Metric.DEPTH_OF_INHERITANCE_TREE, BigDecimal.valueOf(10));
        metrics.put(Metric.CYCLOMATIC_COMPLEXITY, BigDecimal.valueOf(20));
        metrics.put(Metric.LINES_OF_CODE, BigDecimal.valueOf(30));

        QualityAttributeResult result = calculator.calculate(metrics);

        assertEquals(QualityAttribute.MAINTAINABILITY.name(), result.qualityAttributeName());
        assertEquals(BigDecimal.valueOf(20), result.changePercentage());
    }

    @Test
    @DisplayName("Calculates maintainability correctly when some metrics are zero")
    void calculatesMaintainabilityCorrectlyWhenSomeMetricsAreZero() {
        MaintainabilityCalculator calculator = new MaintainabilityCalculator();
        Map<Metric, BigDecimal> metrics = new HashMap<>();
        metrics.put(Metric.DEPTH_OF_INHERITANCE_TREE, BigDecimal.ZERO);
        metrics.put(Metric.CYCLOMATIC_COMPLEXITY, BigDecimal.valueOf(20));
        metrics.put(Metric.LINES_OF_CODE, BigDecimal.valueOf(30));

        QualityAttributeResult result = calculator.calculate(metrics);

        assertEquals(QualityAttribute.MAINTAINABILITY.name(), result.qualityAttributeName());
        assertEquals(BigDecimal.valueOf(17), result.changePercentage());
    }

    @Test
    @DisplayName("Calculates maintainability correctly when all metrics are zero")
    void calculatesMaintainabilityCorrectlyWhenAllMetricsAreZero() {
        MaintainabilityCalculator calculator = new MaintainabilityCalculator();
        Map<Metric, BigDecimal> metrics = new HashMap<>();
        metrics.put(Metric.DEPTH_OF_INHERITANCE_TREE, BigDecimal.ZERO);
        metrics.put(Metric.CYCLOMATIC_COMPLEXITY, BigDecimal.ZERO);
        metrics.put(Metric.LINES_OF_CODE, BigDecimal.ZERO);

        QualityAttributeResult result = calculator.calculate(metrics);

        assertEquals(QualityAttribute.MAINTAINABILITY.name(), result.qualityAttributeName());
        assertEquals(BigDecimal.ZERO, result.changePercentage());
    }
}