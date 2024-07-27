package br.com.magnus.metricscalculator.consumer;

import br.com.magnus.config.starter.projects.BaseProject;
import br.com.magnus.metricscalculator.qualityAttributes.QualityAttributesProcessor;
import br.com.magnus.metricscalculator.repository.ExtractProjects;
import br.com.magnus.metricscalculator.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsProcessorTest {

    @Mock
    private ExtractProjects extractProjects;
    @Mock
    private QualityAttributesProcessor processor;
    @Mock
    private ProjectRepository projectRepository;
    private MetricsProcessor metricsProcessor;

    @BeforeEach
    public void setup() {
        metricsProcessor = new MetricsProcessor(extractProjects, processor, projectRepository);
    }

    @Test
    @DisplayName("Should test for a project not found")
    public void shouldTestForAProjectNotFound() {
        when(projectRepository.findById("id")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> metricsProcessor.process("id"));

        verify(extractProjects, never()).extractProject(any(), any());
        verify(processor, never()).extract(any(), any());
        verify(projectRepository, never()).save(any());
    }


    @Test
    @DisplayName("Should test a id consumption")
    public void shouldTestAIdConsumption() {
        when(projectRepository.findById("id")).thenReturn(Optional.of(BaseProject.builder().build()));
        when(extractProjects.extractProject(any(), any())).thenReturn(Mockito.mock(Path.class));

        metricsProcessor.process("id");

        verify(projectRepository, atLeastOnce()).save(any(BaseProject.class));
    }

}