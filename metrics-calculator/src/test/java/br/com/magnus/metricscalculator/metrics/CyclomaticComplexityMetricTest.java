package br.com.magnus.metricscalculator.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CyclomaticComplexityMetricTest {

    @Mock
    private MetricsResolver original;

    @Mock
    private MetricsResolver refactored;

    private MetricCalculator metricCalculator;

    @BeforeEach
    void setUp() {
        metricCalculator = new CyclomaticComplexityMetric();
    }

    @Test
    @DisplayName("Calculates cyclomatic complexity correctly")
    void calculatesCyclomaticComplexityCorrectly() {
        when(original.getCyclomaticComplexity()).thenReturn(10);
        when(refactored.getCyclomaticComplexity()).thenReturn(5);

        Map<Metric, BigDecimal> result = metricCalculator.calculate(original, refactored);

        assertEquals(BigInteger.valueOf(100), result.get(Metric.CYCLOMATIC_COMPLEXITY).toBigInteger());
    }

    @Test
    @DisplayName("Handles zero original cyclomatic complexity")
    void handlesZeroOriginalCyclomaticComplexity() {
        when(original.getCyclomaticComplexity()).thenReturn(0);
        when(refactored.getCyclomaticComplexity()).thenReturn(5);

        Map<Metric, BigDecimal> result = metricCalculator.calculate(original, refactored);

        assertEquals(BigInteger.valueOf(-100), result.get(Metric.CYCLOMATIC_COMPLEXITY).toBigInteger());
    }

    @Test
    @DisplayName("Handles zero refactored cyclomatic complexity")
    void handlesZeroRefactoredCyclomaticComplexity() {
        when(original.getCyclomaticComplexity()).thenReturn(10);
        when(refactored.getCyclomaticComplexity()).thenReturn(0);

        var result = assertThrows(ArithmeticException.class, () -> metricCalculator.calculate(original, refactored));

        assertEquals("/ by zero", result.getMessage());
    }
}