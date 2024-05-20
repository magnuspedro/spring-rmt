package br.com.magnus.detectionandrefactoring.refactor.dataExtractions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExtractionMethodFactory {
    private final List<ExtractionMethod> extractionMethods;

    public ExtractionMethod build(Object object) {
        return this.extractionMethods.stream()
                .filter(f -> f.supports(object))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Extraction Method not found!"));
    }
}
