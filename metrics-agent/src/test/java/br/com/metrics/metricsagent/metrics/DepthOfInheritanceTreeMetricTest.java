package br.com.metrics.metricsagent.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepthOfInheritanceTreeMetricTest {

    @Mock
    private MetricsResolver original;

    @Mock
    private MetricsResolver refactored;

    private MetricCalculator metricCalculator;

    @BeforeEach
    void setUp() {
        metricCalculator = new DepthOfInheritanceTreeMetric();
    }

    @Test
    @DisplayName("Calculates depth of inheritance tree correctly")
    void calculatesDepthOfInheritanceTreeCorrectly() {
        when(original.getDepthOfInheritanceTree()).thenReturn(3);
        when(refactored.getDepthOfInheritanceTree()).thenReturn(2);

        var result = metricCalculator.calculate(original, refactored);

        assertEquals(-33, result.get(Metric.DEPTH_OF_INHERITANCE_TREE).intValue());
    }

    @Test
    @DisplayName("Handles zero original depth of inheritance tree")
    void handlesZeroOriginalDepthOfInheritanceTree() {
        when(original.getDepthOfInheritanceTree()).thenReturn(0);
        when(refactored.getDepthOfInheritanceTree()).thenReturn(2);

        var result = assertThrows(ArithmeticException.class,
                () -> metricCalculator.calculate(original, refactored));

        assertEquals("/ by zero", result.getMessage());
    }

    @Test
    @DisplayName("Handles zero refactored depth of inheritance tree")
    void handlesZeroRefactoredDepthOfInheritanceTree() {
        when(original.getDepthOfInheritanceTree()).thenReturn(3);
        when(refactored.getDepthOfInheritanceTree()).thenReturn(0);

        var result = metricCalculator.calculate(original, refactored);

        assertEquals(-100, result.get(Metric.DEPTH_OF_INHERITANCE_TREE).intValue());
    }
}