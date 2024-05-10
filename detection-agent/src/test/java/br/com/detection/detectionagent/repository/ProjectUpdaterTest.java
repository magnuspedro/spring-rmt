package br.com.detection.detectionagent.repository;

import br.com.magnus.config.starter.configuration.BucketProperties;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.config.starter.repository.S3ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectUpdaterTest {

    private ProjectUpdater projectUpdater;
    @Mock
    private Project project;
    @Mock
    private S3ProjectRepository s3ProjectRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private BucketProperties bucketProperties;

    @BeforeEach
    void setUp() {
        projectUpdater = new ProjectUpdater(projectRepository, s3ProjectRepository, bucketProperties);
    }

    @Test
    void shouldAddNoCandidatesStatusWhenNoRefactoringCandidates() {
        projectUpdater.updateStatus(project);

        verify(project, times(1)).addStatus(ProjectStatus.NO_CANDIDATES);
    }

    @Test
    void shouldAddRefactoredStatusAndUploadWhenRefactoringCandidatesExist() {
        when(project.getRefactoringCandidates()).thenReturn(List.of(mock(RefactoringCandidate.class)));

        projectUpdater.updateStatus(project);

        verify(project, times(1)).addStatus(ProjectStatus.REFACTORED);
        verify(s3ProjectRepository, times(1)).upload(any(), any(), any(), any());
    }
}