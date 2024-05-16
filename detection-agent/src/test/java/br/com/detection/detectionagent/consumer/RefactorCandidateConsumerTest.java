package br.com.detection.detectionagent.consumer;

import br.com.detection.detectionagent.gateway.SendProject;
import br.com.detection.detectionagent.refactor.methods.DetectionMethodsManager;
import br.com.detection.detectionagent.repository.ProjectRepository;
import br.com.detection.detectionagent.repository.ProjectUpdater;
import br.com.magnus.config.starter.file.extractor.FileExtractor;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefactorCandidateConsumerTest {

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
    private RefactorCandidateConsumer refactorCandidateConsumer;

    @BeforeEach
    void setUp() {
        List<DetectionMethodsManager> detectionMethodsManagerList = List.of(detectionMethodsManager);
        refactorCandidateConsumer = new RefactorCandidateConsumer(detectionMethodsManagerList, projectUpdater, sendProject, projectsRepository, fileExtractor);
    }

    @Test
    @DisplayName("Should test consumer with null")
    public void shouldTestConsumerWithNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> refactorCandidateConsumer.listener(null));

        verify(detectionMethodsManager, never()).refactor(any());
        verify(projectUpdater, never()).saveProject(any());
        verify(sendProject, never()).send(anyString());
        assertEquals("Id cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test consumer with no candidates")
    public void shouldTestConsumerWithNoCandidates() {
        var project = Project.builder().id("id").build();
        project.addStatus(ProjectStatus.NO_CANDIDATES);
        when(projectsRepository.findById(anyString())).thenReturn(java.util.Optional.of(project));

        assertDoesNotThrow(() -> refactorCandidateConsumer.listener("id"));

        verify(detectionMethodsManager, atLeastOnce()).refactor(any());
        verify(projectUpdater, atLeastOnce()).saveProject(any());
        verify(sendProject, never()).send(anyString());
    }

    @Test
    @DisplayName("Should test consumer with candidates")
    public void shouldTestConsumerWithCandidates() {
        var project = Project.builder().id("id").build();
        project.addStatus(ProjectStatus.REFACTORED);
        when(projectsRepository.findById(anyString())).thenReturn(java.util.Optional.of(project));

        assertDoesNotThrow(() -> refactorCandidateConsumer.listener("id"));

        verify(detectionMethodsManager, atLeastOnce()).refactor(any());
        verify(projectUpdater, atLeastOnce()).saveProject(any());
        verify(sendProject, atLeastOnce()).send(anyString());
    }

}