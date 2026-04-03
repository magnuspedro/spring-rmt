package br.com.magnus.detectionandrefactoring.consumer.refactor.methods.cinneide;

import br.com.magnus.config.starter.patterns.DesignPattern;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ExtractionMethod;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ExtractionMethodFactory;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideEtAl2000;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideRefactoringTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static fixtures.Cinneide.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CinneideEtAl2000Test {

    @Mock
    private ExtractionMethodFactory extractionMethodFactory;
    @Mock
    private ExtractionMethod extractionMethod;

    private CinneideEtAl2000 cinneideEtAl2000;

    @BeforeEach
    void setUp() {
        when(extractionMethodFactory.build(any())).thenReturn(extractionMethod);
        when(extractionMethod.parseAll(any())).thenReturn(List.of());
        cinneideEtAl2000 = new CinneideEtAl2000(extractionMethodFactory, new CinneideRefactoringTool());
    }

    @Test
    @DisplayName("Should extract cinneide candidates for all available pattern transformations")
    void shouldExtractCinneideCandidatesForAllAvailablePatternTransformations() {
        var javaFiles = new ArrayList<>(createFactoryMethodSample());
        javaFiles.addAll(createSingletonSample());
        javaFiles.addAll(createAbstractFactorySample());
        javaFiles.addAll(createStrategySample());
        javaFiles.addAll(createBridgeSample());

        var candidates = cinneideEtAl2000.extractCandidates(javaFiles);
        var patterns = new HashSet<>(candidates.stream().map(candidate -> candidate.getEligiblePattern()).toList());

        assertTrue(patterns.contains(DesignPattern.FACTORY_METHOD));
        assertTrue(patterns.contains(DesignPattern.SINGLETON));
        assertTrue(patterns.contains(DesignPattern.ABSTRACT_FACTORY));
        assertTrue(patterns.contains(DesignPattern.STRATEGY));
        assertTrue(patterns.contains(DesignPattern.BRIDGE));
    }
}
