package br.com.magnus.detection.repository;

import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.config.starter.repository.S3ProjectRepository;
import br.com.magnus.detectionandrefactoring.repository.ProjectRepository;
import br.com.magnus.detectionandrefactoring.repository.ProjectUpdater;
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

    @BeforeEach
    void setUp() {
        projectUpdater = new ProjectUpdater(projectRepository, s3ProjectRepository);
    }

    @Test
    void shouldAddNoCandidatesStatusWhenNoRefactoringCandidates() {
        projectUpdater.saveProject(project);

        verify(project, times(1)).addStatus(ProjectStatus.NO_CANDIDATES);
    }

    @Test
    void shouldAddRefactoredStatusAndUploadWhenRefactoringCandidatesExist() {
        when(project.getRefactorFiles()).thenReturn(List.of(RefactorFiles.builder()
                .candidates(List.of(mock(RefactoringCandidate.class)))
                .build()));

        projectUpdater.saveProject(project);

        verify(project, times(1)).addStatus(ProjectStatus.REFACTORED);
        verify(s3ProjectRepository, times(1)).upload(any(), any(), any(), any());
    }
}