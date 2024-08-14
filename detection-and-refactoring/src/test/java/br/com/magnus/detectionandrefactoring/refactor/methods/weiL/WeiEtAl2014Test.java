package br.com.magnus.detectionandrefactoring.refactor.methods.weiL;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ExtractionMethod;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ExtractionMethodFactory;
import br.com.magnus.detectionandrefactoring.refactor.methods.weiL.executors.WeiEtAl2014Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeiEtAl2014Test {

    @Mock
    private List<JavaFile> javaFiles;
    @Mock
    private RefactorFiles refactorFiles;
    @Mock
    private ExtractionMethodFactory extractionMethodFactory;
    @Mock
    private ExtractionMethod extractionMethod;
    @Mock
    private WeiEtAl2014Executor executor;

    private WeiEtAl2014 weiEtAl2014;

    @BeforeEach
    void setUp() {
        this.weiEtAl2014 = new WeiEtAl2014(List.of(), extractionMethodFactory, List.of(executor));
    }

    @Test
    void shouldExtractCandidates() {
        when(extractionMethodFactory.build(any())).thenReturn(extractionMethod);

        weiEtAl2014.extractCandidates(javaFiles);

        verify(extractionMethod, times(1)).parseAll(javaFiles);
    }

    @Test
    void shouldRefactorWhenExecutorIsApplicable() {
        when(executor.isApplicable(refactorFiles.candidate())).thenReturn(true);

        weiEtAl2014.refactor(refactorFiles);

        verify(executor, times(1)).refactor(refactorFiles);
    }

    @Test
    void shouldThrowExceptionWhenNoExecutorIsApplicable() {
        when(executor.isApplicable(refactorFiles.candidate())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> weiEtAl2014.refactor(refactorFiles));
    }
}