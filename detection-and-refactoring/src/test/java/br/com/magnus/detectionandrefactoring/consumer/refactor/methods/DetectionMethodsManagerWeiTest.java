package br.com.magnus.detectionandrefactoring.consumer.refactor.methods;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.detectionandrefactoring.configuration.RefactoringProperties;
import br.com.magnus.detectionandrefactoring.refactor.methods.DetectionMethodsManagerWei;
import br.com.magnus.detectionandrefactoring.refactor.methods.weiL.WeiEtAl2014;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DetectionMethodsManagerWeiTest {

    @Mock
    private WeiEtAl2014 weiEtAl2014;

    @Mock
    private Project project;

    @Mock
    private JavaFile javaFile;

    @Mock
    private RefactoringCandidate refactoringCandidate;

    private DetectionMethodsManagerWei detectionMethodsManager;
    private final RefactoringProperties refactoringProperties = new RefactoringProperties(2);

    @BeforeEach
    void setUp() {
        detectionMethodsManager = new DetectionMethodsManagerWei(weiEtAl2014, refactoringProperties);
    }

    @Test
    void refactorWithNoCandidates() {
        when(weiEtAl2014.extractCandidates(any())).thenReturn(List.of());

        var result = detectionMethodsManager.refactor(project);

        verify(weiEtAl2014).extractCandidates(any());
        verify(weiEtAl2014, never()).refactor(any());
        assertEquals(List.of(), result);
    }

    @Test
    void refactorWithCandidates() {
        when(weiEtAl2014.extractCandidates(any())).thenReturn(List.of(refactoringCandidate));
        when(project.getOriginalContent()).thenReturn(List.of(javaFile));

        var result = detectionMethodsManager.refactor(project);

        verify(weiEtAl2014).extractCandidates(any());
        verify(weiEtAl2014).refactor(any());
        assertEquals(1, result.size());
        assertEquals(refactoringCandidate, result.getFirst().candidate());
    }

    @Test
    void refactorWithGroupedCandidates() {
        when(weiEtAl2014.extractCandidates(any())).thenReturn(List.of(refactoringCandidate));
        when(project.getOriginalContent()).thenReturn(List.of(javaFile));
        when(refactoringCandidate.getClassName()).thenReturn("ClassName");

        var result = detectionMethodsManager.refactor(project);

        verify(weiEtAl2014).extractCandidates(any());
        verify(weiEtAl2014).refactor(any());
        assertEquals(1, result.size());
    }
}
