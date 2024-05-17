package br.com.metrics.metricsagent.qualityAttributes;

import br.com.metrics.metricsagent.metrics.MetricCalculator;
import br.com.metrics.metricsagent.qualityAttributes.calculator.QualityAttributeCalculator;
import com.github.mauricioaniche.ck.CK;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QualityAttributesProcessorCKTest {

    @Mock
    private List<MetricCalculator> metricCalculators;

    @Mock
    private List<QualityAttributeCalculator> qualityAttributeCalculators;

    @Mock
    private CK ck;

    @Mock
    private Path originalPath;

    @Mock
    private Path refactoredPath;

    private QualityAttributesProcessorCK processor;

    @BeforeEach
    void setUp() {
        processor = new QualityAttributesProcessorCK(metricCalculators, qualityAttributeCalculators, ck);
    }

    @Test
    @DisplayName("Extracts quality attributes correctly when both original and refactored paths are valid")
    void extractsQualityAttributesCorrectlyWhenPathsAreValid() {
        processor.extract(originalPath, refactoredPath);

        verify(ck, times(1)).calculate(eq(originalPath), any());
        verify(ck, times(1)).calculate(eq(refactoredPath), any());
    }
}