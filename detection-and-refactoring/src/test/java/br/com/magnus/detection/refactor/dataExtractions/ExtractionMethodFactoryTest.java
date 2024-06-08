package br.com.magnus.detection.refactor.dataExtractions;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ExtractionMethod;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ExtractionMethodFactory;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AbstractSyntaxTreeExtraction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtractionMethodFactoryTest {

    private ExtractionMethodFactory extractionMethodFactory;

    @Mock
    private ExtractionMethod extractionMethod;

    @BeforeEach
    void setUp() {
        this.extractionMethodFactory = new ExtractionMethodFactory(List.of(extractionMethod));
    }

    @Test
    @DisplayName("Should test build for parameter null")
    public void shouldTestBuildForParameterNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> this.extractionMethodFactory.build(null));

        assertEquals("Extraction Method not found!", result.getMessage());
    }

    @Test
    @DisplayName("Should test build")
    public void shouldTestBuild() {
        var abstractSyntaxTreeDependent = new AbstractSyntaxTreeExtraction() {};
        when(this.extractionMethod.supports(abstractSyntaxTreeDependent))
                .thenReturn(true);

        var result = this.extractionMethodFactory.build(abstractSyntaxTreeDependent);

        assertNotNull(result);
    }
}