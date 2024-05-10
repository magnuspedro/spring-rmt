package br.com.detection.detectionagent.repository;

import br.com.magnus.config.starter.configuration.BucketProperties;
import br.com.magnus.config.starter.file.compressor.FileCompressor;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.config.starter.repository.S3ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectUpdater {

    private final ProjectRepository projectRepository;
    private final S3ProjectRepository s3ProjectRepository;
    private final BucketProperties bucketProperties;

    public void saveProject(Project project) {
        project.setRefactoredBucket(bucketProperties.getRefactoredProjectBucket());
        saveFiles(project);
        projectRepository.save(project);

    }

    private void saveFiles(Project project) {
        if (project.getRefactoringCandidates() == null || project.getRefactoringCandidates().isEmpty()) {
            project.addStatus(ProjectStatus.NO_CANDIDATES);
            return;
        }
        project.addStatus(ProjectStatus.REFACTORED);
        var inputStream = FileCompressor.compress(project.getRefactoredContent());
        s3ProjectRepository.upload(project.getRefactoredBucket(), project.getId(), inputStream, project.getMetadata());
    }
}
