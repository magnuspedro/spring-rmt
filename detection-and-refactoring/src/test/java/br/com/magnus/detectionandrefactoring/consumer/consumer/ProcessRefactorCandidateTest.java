package br.com.magnus.detectionandrefactoring.consumer.consumer;

import br.com.magnus.config.starter.file.extractor.FileExtractor;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.projects.BaseProject;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.detectionandrefactoring.consumer.ProcessRefactorCandidate;
import br.com.magnus.detectionandrefactoring.consumer.RefactorCandidateConsumer;
import br.com.magnus.detectionandrefactoring.configuration.RefactoringProperties;
import br.com.magnus.detectionandrefactoring.gateway.SendProject;
import br.com.magnus.detectionandrefactoring.refactor.methods.DetectionMethodsManager;
import br.com.magnus.detectionandrefactoring.repository.ProjectRepository;
import br.com.magnus.detectionandrefactoring.repository.ProjectUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.ArgumentMatchers.assertArg;

@ExtendWith(MockitoExtension.class)
class ProcessRefactorCandidateTest {

    @Mock
    private DetectionMethodsManager detectionMethodsManager;
    @Mock
    private ProjectUpdater projectUpdater;
    @Mock
    private SendProject sendProject;
    @Mock
    private ProjectRepository projectsRepository;
    @Mock
    private FileExtractor fileExtractor;
    private ProcessRefactorCandidate processRefactorCandidate;
    private final Executor detectionMethodsManagerExecutor = Runnable::run;
    private final RefactoringProperties refactoringProperties = new RefactoringProperties();

    @BeforeEach
    void setUp() {
        List<DetectionMethodsManager> detectionMethodsManagerList = List.of(detectionMethodsManager);
        processRefactorCandidate = new ProcessRefactorCandidate(
                detectionMethodsManagerList,
                projectUpdater,
                sendProject,
                projectsRepository,
                fileExtractor,
                detectionMethodsManagerExecutor,
                refactoringProperties);
    }

    @Test
    @DisplayName("Should test consumer with null")
    public void shouldTestConsumerWithNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> processRefactorCandidate.process(null));

        verify(detectionMethodsManager, never()).refactor(any());
        verify(projectUpdater, never()).saveProject(any());
        verify(sendProject, never()).send(anyString());
        assertEquals("Id cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test consumer with no candidates")
    public void shouldTestConsumerWithNoCandidates() {
        var project = Project.builder()
                .baseProject(BaseProject.builder()
                        .id("id")
                        .build())
                .build();
        project.addStatus(ProjectStatus.NO_CANDIDATES);
        when(projectsRepository.findById(anyString())).thenReturn(Optional.of(project.getBaseProject()));
        when(fileExtractor.extract(project.getBaseProject())).thenReturn(List.of());
        when(detectionMethodsManager.refactor(any())).thenReturn(List.of());

        assertDoesNotThrow(() -> processRefactorCandidate.process("id"));

        verify(detectionMethodsManager, atLeastOnce()).refactor(any());
        verify(projectUpdater, atLeastOnce()).saveProject(any());
        verify(sendProject, never()).send(anyString());
    }

    @Test
    @DisplayName("Should test consumer with candidates")
    public void shouldTestConsumerWithCandidates() {
        var project = Project.builder()
                .baseProject(BaseProject.builder()
                        .id("id")
                        .build())
                .build();
        project.addStatus(ProjectStatus.REFACTORED);
        when(projectsRepository.findById(anyString())).thenReturn(Optional.of(project.getBaseProject()));
        when(fileExtractor.extract(project.getBaseProject())).thenReturn(List.of());
        var refactorFiles = RefactorFiles.builder().build();
        when(detectionMethodsManager.refactor(any())).thenReturn(List.of(refactorFiles));

        assertDoesNotThrow(() -> processRefactorCandidate.process("id"));

        verify(detectionMethodsManager, atLeastOnce()).refactor(any());
        verify(projectUpdater, atLeastOnce()).saveProject(assertArg(savedProject ->
                assertTrue(savedProject.getRefactorFiles().contains(refactorFiles))));
        verify(sendProject, atLeastOnce()).send(anyString());
    }


}
