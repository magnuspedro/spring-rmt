package br.com.magnus.detectionandrefactoring.refactor.methods;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.ZafeirisEtAl2016;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.ZafeirisEtAl2016Candidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetectionMethodsManagerZaiferisTest {

    @Mock
    private ZafeirisEtAl2016 zafeirisEtAl2016;

    @Mock
    private Project project;

    @Mock
    private JavaFile javaFile;

    @Mock
    private ZafeirisEtAl2016Candidate refactoringCandidate;

    private DetectionMethodsManagerZaiferis detectionMethodsManager;

    @BeforeEach
    void setUp() {
        detectionMethodsManager = new DetectionMethodsManagerZaiferis(zafeirisEtAl2016);
    }

    @Test
    void refactorWithNoCandidates() {
        when(zafeirisEtAl2016.extractCandidates(any())).thenReturn(List.of());

        detectionMethodsManager.refactor(project);

        verify(zafeirisEtAl2016).extractCandidates(any());
        verify(zafeirisEtAl2016, never()).refactor(any());
    }

    @Test
    void refactorWithGroupedCandidates() {
        when(zafeirisEtAl2016.extractCandidates(any())).thenReturn(List.of(refactoringCandidate));

        detectionMethodsManager.refactor(project);

        verify(zafeirisEtAl2016, atLeastOnce()).extractCandidates(any());
        verify(zafeirisEtAl2016, atLeastOnce()).refactor(any());
    }
}