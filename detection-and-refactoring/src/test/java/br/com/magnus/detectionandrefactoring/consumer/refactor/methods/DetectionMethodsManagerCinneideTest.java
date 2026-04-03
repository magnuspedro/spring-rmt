package br.com.magnus.detectionandrefactoring.consumer.refactor.methods;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.detectionandrefactoring.refactor.methods.DetectionMethodsManagerCinneide;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideEtAl2000;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetectionMethodsManagerCinneideTest {

    @Mock
    private CinneideEtAl2000 cinneideEtAl2000;

    @Mock
    private Project project;

    @Mock
    private JavaFile javaFile;

    @Mock
    private RefactoringCandidate refactoringCandidate;

    private DetectionMethodsManagerCinneide detectionMethodsManager;

    @BeforeEach
    void setUp() {
        detectionMethodsManager = new DetectionMethodsManagerCinneide(cinneideEtAl2000);
    }

    @Test
    void refactorWithNoCandidates() {
        when(cinneideEtAl2000.extractCandidates(any())).thenReturn(List.of());

        detectionMethodsManager.refactor(project);

        verify(cinneideEtAl2000).extractCandidates(any());
        verify(cinneideEtAl2000, never()).refactor(any());
        verify(project, never()).addAllRefactorFiles(any());
    }

    @Test
    void refactorWithCandidates() {
        when(cinneideEtAl2000.extractCandidates(any())).thenReturn(List.of(refactoringCandidate));
        when(project.getOriginalContent()).thenReturn(List.of(javaFile));

        detectionMethodsManager.refactor(project);

        verify(cinneideEtAl2000).extractCandidates(any());
        verify(cinneideEtAl2000, atLeastOnce()).refactor(any());
    }
}
