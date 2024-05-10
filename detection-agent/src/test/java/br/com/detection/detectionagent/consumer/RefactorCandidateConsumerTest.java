package br.com.detection.detectionagent.consumer;

import br.com.detection.detectionagent.gateway.SendProject;
import br.com.detection.detectionagent.refactor.methods.DetectionMethodsManager;
import br.com.detection.detectionagent.repository.ProjectUpdater;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private RefactorCandidateConsumer refactorCandidateConsumer;


    @BeforeEach
    void setUp() {
        refactorCandidateConsumer = new RefactorCandidateConsumer(detectionMethodsManager, projectUpdater, sendProject);
    }

    @Test
    @DisplayName("Should test consumer with null")
    public void shouldTestConsumerWithNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> refactorCandidateConsumer.listener(null));

        verify(detectionMethodsManager, never()).extractCandidates(anyString());
        verify(projectUpdater, never()).saveProject(any());
        verify(sendProject, never()).send(anyString());
        assertEquals("Id cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test consumer with no candidates")
    public void shouldTestConsumerWithNoCandidates() {
        var project = Project.builder().id("id").build();
        project.addStatus(ProjectStatus.NO_CANDIDATES);
        when(detectionMethodsManager.extractCandidates(anyString())).thenReturn(project);

        assertDoesNotThrow(() -> refactorCandidateConsumer.listener("id"));

        verify(detectionMethodsManager, atLeastOnce()).extractCandidates(anyString());
        verify(projectUpdater, atLeastOnce()).saveProject(any());
        verify(sendProject, never()).send(anyString());
    }

    @Test
    @DisplayName("Should test consumer with candidates")
    public void shouldTestConsumerWithCandidates() {
        var project = Project.builder().id("id").build();
        project.addStatus(ProjectStatus.REFACTORED);
        when(detectionMethodsManager.extractCandidates(anyString())).thenReturn(project);

        assertDoesNotThrow(() -> refactorCandidateConsumer.listener("id"));

        verify(detectionMethodsManager, atLeastOnce()).extractCandidates(anyString());
        verify(projectUpdater, atLeastOnce()).saveProject(any());
        verify(sendProject, atLeastOnce()).send(anyString());
    }

}