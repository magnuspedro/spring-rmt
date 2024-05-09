package br.com.detection.detectionagent.domain.methods.weiL;

import br.com.detection.detectionagent.domain.methods.weiL.executors.WeiEtAl2014Executor;
import br.com.detection.detectionagent.methods.dataExtractions.ExtractionMethod;
import br.com.detection.detectionagent.methods.dataExtractions.ExtractionMethodFactory;
import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
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
    private RefactoringCandidate candidate;
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
        when(executor.isApplicable(candidate)).thenReturn(true);

        weiEtAl2014.refactor(javaFiles, candidate);

        verify(executor, times(1)).refactor(candidate, javaFiles);
    }

    @Test
    void shouldThrowExceptionWhenNoExecutorIsApplicable() {
        when(executor.isApplicable(candidate)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> weiEtAl2014.refactor(javaFiles, candidate));
    }
}