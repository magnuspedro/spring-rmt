package br.com.magnus.detectionandrefactoring.consumer.refactor.methods;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.detectionandrefactoring.configuration.RefactoringProperties;
import br.com.magnus.detectionandrefactoring.refactor.methods.DetectionMethodsManagerZaiferis;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.ZafeirisEtAl2016;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.ZafeirisEtAl2016Candidate;
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
    private final RefactoringProperties refactoringProperties = new RefactoringProperties();
    private final Executor zafeirisRefactoringExecutor = Runnable::run;

    @BeforeEach
    void setUp() {
        refactoringProperties.getZafeiris().setParallelism(2);
        detectionMethodsManager = new DetectionMethodsManagerZaiferis(zafeirisEtAl2016, refactoringProperties, zafeirisRefactoringExecutor);
    }

    @Test
    void refactorWithNoCandidates() {
        when(zafeirisEtAl2016.extractCandidates(any())).thenReturn(List.of());

        var result = detectionMethodsManager.refactor(project);

        verify(zafeirisEtAl2016).extractCandidates(any());
        verify(zafeirisEtAl2016, never()).refactor(any());
        assertEquals(List.of(), result);
    }

    @Test
    void refactorWithGroupedCandidates() {
        when(zafeirisEtAl2016.extractCandidates(any())).thenReturn(List.of(refactoringCandidate));
        when(project.getOriginalContent()).thenReturn(List.of(javaFile));
        when(refactoringCandidate.getParentType()).thenReturn("Parent");

        var result = detectionMethodsManager.refactor(project);

        verify(zafeirisEtAl2016, atLeastOnce()).extractCandidates(any());
        verify(zafeirisEtAl2016, atLeastOnce()).refactor(any());
        assertEquals(1, result.size());
        assertEquals(refactoringCandidate, result.getFirst().candidate());
    }
}
