package br.com.magnus.metricscalculator.qualityAttributes;

import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;
import br.com.magnus.metricscalculator.metrics.CKNotifierImpl;
import br.com.magnus.metricscalculator.metrics.MetricCalculator;
import br.com.magnus.metricscalculator.qualityAttributes.calculator.QualityAttributeCalculator;
import com.github.mauricioaniche.ck.CK;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class QualityAttributesProcessorCK implements QualityAttributesProcessor {

    private final List<MetricCalculator> metricCalculators;
    private final List<QualityAttributeCalculator> qualityAttributeCalculators;
    private final CK ck;

    @Override
    public List<QualityAttributeResult> extract(Path originalPath, Path refactoredPath) {
        var original = new CKNotifierImpl();
        var refactored = new CKNotifierImpl();
        ck.calculate(originalPath, original);
        ck.calculate(refactoredPath, refactored);

        return calculateQualityAttributeResult(original, refactored);
    }

    private List<QualityAttributeResult> calculateQualityAttributeResult(CKNotifierImpl original, CKNotifierImpl refactored) {
        var metrics = metricCalculators.stream()
                .map(metric -> metric.calculate(original, refactored))
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return qualityAttributeCalculators.stream()
                .map(calculator -> calculator.calculate(metrics))
                .toList();
    }
}
