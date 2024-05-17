package br.com.metrics.metricsagent.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinesOfCodeMetricTest {

    @Mock
    private MetricsResolver original;

    @Mock
    private MetricsResolver refactored;

    private MetricCalculator metricCalculator;

    @BeforeEach
    void setUp() {
        metricCalculator = new LinesOfCodeMetric();
    }

    @Test
    @DisplayName("Calculates lines of code correctly when refactored code has fewer lines")
    void calculatesLinesOfCodeCorrectlyWhenRefactoredHasFewerLines() {
        when(original.getLinesOfCode()).thenReturn(100);
        when(refactored.getLinesOfCode()).thenReturn(50);

        BigDecimal result = metricCalculator.calculate(original, refactored).get(Metric.LINES_OF_CODE);

        assertEquals(BigInteger.valueOf(100), result.toBigInteger());
    }

    @Test
    @DisplayName("Calculates lines of code correctly when refactored code has more lines")
    void calculatesLinesOfCodeCorrectlyWhenRefactoredHasMoreLines() {
        when(original.getLinesOfCode()).thenReturn(50);
        when(refactored.getLinesOfCode()).thenReturn(100);

        BigDecimal result = metricCalculator.calculate(original, refactored).get(Metric.LINES_OF_CODE);

        assertEquals(BigInteger.valueOf(-50), result.toBigInteger());
    }

    @Test
    @DisplayName("Handles zero lines in original code")
    void handlesZeroLinesInOriginalCode() {
        when(original.getLinesOfCode()).thenReturn(0);
        when(refactored.getLinesOfCode()).thenReturn(50);

        BigDecimal result = metricCalculator.calculate(original, refactored).get(Metric.LINES_OF_CODE);

        assertEquals(BigInteger.valueOf(-100), result.toBigInteger());
    }

    @Test
    @DisplayName("Handles zero lines in refactored code")
    void handlesZeroLinesInRefactoredCode() {
        when(original.getLinesOfCode()).thenReturn(50);
        when(refactored.getLinesOfCode()).thenReturn(0);

        var result = assertThrows(ArithmeticException.class, () -> metricCalculator.calculate(original, refactored));

        assertEquals("/ by zero", result.getMessage());
    }
}