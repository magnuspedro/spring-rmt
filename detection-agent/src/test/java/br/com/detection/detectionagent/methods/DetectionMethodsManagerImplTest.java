package br.com.detection.detectionagent.methods;

import br.com.detection.detectionagent.refactor.methods.DetectionMethod;
import br.com.detection.detectionagent.refactor.methods.DetectionMethodsManagerImpl;
import br.com.detection.detectionagent.repository.ProjectRepository;
import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.file.extractor.FileExtractor;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetectionMethodsManagerImplTest {

    @Mock
    private ProjectRepository projectsRepository;

    @Mock
    private FileExtractor fileExtractor;

    @Mock
    private DetectionMethod detectionMethod;

    @Mock
    private JavaFile javaFile;

    @Mock
    private RefactoringCandidate refactoringCandidate;

    private DetectionMethodsManagerImpl detectionMethodsManager;

    @BeforeEach
    void setUp() {
        List<DetectionMethod> detectionMethods = List.of(detectionMethod);
        detectionMethodsManager = new DetectionMethodsManagerImpl(projectsRepository, fileExtractor, detectionMethods);
    }

    @Test
    void extractCandidatesReturnsValidCandidates() {
        String projectId = "testProjectId";
        when(projectsRepository.findById(projectId)).thenReturn(Optional.of(Project.builder().build()));
        when(fileExtractor.extract(any())).thenReturn(List.of(javaFile));
        when(detectionMethod.extractCandidates(any())).thenReturn(List.of(refactoringCandidate));

        Project result = detectionMethodsManager.extractCandidates(projectId);

        assertEquals(1, result.getRefactoringCandidates().size());
        assertEquals(refactoringCandidate.getClassName(), result.getRefactoringCandidates().getFirst().getClassName());
    }

    @Test
    void extractCandidatesThrowsExceptionForInvalidProjectId() {
        String invalidProjectId = "invalidProjectId";
        when(projectsRepository.findById(invalidProjectId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> detectionMethodsManager.extractCandidates(invalidProjectId));
    }

    @Test
    void refactorCallsRefactorOnSupportedMethods() {
        List<JavaFile> javaFiles = List.of(javaFile);
        List<RefactoringCandidate> candidates = List.of(refactoringCandidate);
        when(detectionMethod.supports(refactoringCandidate)).thenReturn(true);

        detectionMethodsManager.refactor(javaFiles, candidates);

        verify(detectionMethod).refactor(javaFiles, refactoringCandidate);
    }
}
