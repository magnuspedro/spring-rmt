package br.com.magnus.detectionandrefactoring.repository;

import br.com.magnus.config.starter.file.compressor.FileCompressor;
import br.com.magnus.config.starter.projects.CandidateInformation;
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

    public void saveProject(Project project) {
        saveFiles(project);
        projectRepository.save(project);

    }

    private void saveFiles(Project project) {
        if (project.getRefactorFiles() == null || project.getRefactorFiles().isEmpty()) {
            project.addStatus(ProjectStatus.NO_CANDIDATES);
            return;
        }
        project.addStatus(ProjectStatus.REFACTORED);
        project.getRefactorFiles().forEach(refactorFiles -> {
            project.addCandidateInformation(CandidateInformation.builder()
                    .id(refactorFiles.candidate().getId())
                    .designPattern(refactorFiles.candidate().getEligiblePattern())
                    .reference(refactorFiles.candidate().getReference())
                    .filesChanged(refactorFiles.filesChanged())
                    .build());
            var inputStream = FileCompressor.compress(refactorFiles.files());
            s3ProjectRepository.upload(project.getBucket(), refactorFiles.candidate().getId(), inputStream, project.getMetadata());
        });
    }
}
