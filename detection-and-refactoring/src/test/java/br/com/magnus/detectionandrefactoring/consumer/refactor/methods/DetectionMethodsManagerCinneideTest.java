package br.com.magnus.detectionandrefactoring.consumer.refactor.methods;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.detectionandrefactoring.configuration.RefactoringProperties;
import br.com.magnus.detectionandrefactoring.refactor.methods.DetectionMethodsManagerCinneide;
import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideEtAl2000;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private final RefactoringProperties refactoringProperties = new RefactoringProperties();
    private final Executor cinneideRefactoringExecutor = Runnable::run;

    @BeforeEach
    void setUp() {
        refactoringProperties.getCinneide().setParallelism(2);
        detectionMethodsManager = new DetectionMethodsManagerCinneide(cinneideEtAl2000, refactoringProperties, cinneideRefactoringExecutor);
    }

    @Test
    void refactorWithNoCandidates() {
        when(cinneideEtAl2000.extractCandidates(any())).thenReturn(List.of());

        var result = detectionMethodsManager.refactor(project);

        verify(cinneideEtAl2000).extractCandidates(any());
        verify(cinneideEtAl2000, never()).refactor(any());
        assertEquals(List.of(), result);
    }

    @Test
    void refactorWithCandidates() {
        when(cinneideEtAl2000.extractCandidates(any())).thenReturn(List.of(refactoringCandidate));
        when(project.getOriginalContent()).thenReturn(List.of(javaFile));
        when(refactoringCandidate.getClassName()).thenReturn("ClassName");
        doAnswer(invocation -> {
            var refactorFiles = invocation.<RefactorFiles>getArgument(0);
            refactorFiles.addFileChanged("ClassName.java");
            return null;
        }).when(cinneideEtAl2000).refactor(any());

        var result = detectionMethodsManager.refactor(project);

        verify(cinneideEtAl2000).extractCandidates(any());
        verify(cinneideEtAl2000, atLeastOnce()).refactor(any());
        assertEquals(1, result.size());
        assertEquals(refactoringCandidate, result.getFirst().candidate());
    }
}
