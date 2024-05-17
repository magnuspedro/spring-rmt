package br.com.intermediary.intermediaryagent.refactor;

import br.com.intermediary.intermediaryagent.gateway.SendProject;
import br.com.intermediary.intermediaryagent.repository.ProjectRepository;
import br.com.magnus.config.starter.configuration.BucketProperties;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.config.starter.repository.S3ProjectRepository;
import io.awspring.cloud.s3.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RefactorProjectImpl implements RefactorProject {

    private final S3ProjectRepository s3ProjectRepository;
    private final ProjectRepository projectRepository;
    private final SendProject sendProject;
    private final BucketProperties bucket;

    @Override
    public Project process(Project project) {
        var metadata = ObjectMetadata.builder()
                .contentType(project.getContentType())
                .metadata("FileName", project.getName())
                .build();

        project.setMetadata(metadata);
        project.setBucket(bucket.getProjectBucket());
        project.addStatus(ProjectStatus.EVALUATING_CANDIDATES);

        s3ProjectRepository.upload(bucket.getProjectBucket(), project.getId(), project.getZipInputStreamContent(), metadata);
        projectRepository.save(project);
        sendProject.send(project);

        return project;
    }

    @Override
    public ResponseEntity<ProjectResults> retrieve(String id) {
        var project = projectRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        var status = project.getStatus().stream().toList().getLast();
        if (status == ProjectStatus.FINISHED) {
            return ResponseEntity
                    .status(200)
                    .body(ProjectResults.builder()
                            .name(project.getName())
                            .candidatesInformation(project.getCandidatesInformation())
                            .status(status)
                            .build());
        }
        if (status == ProjectStatus.NO_CANDIDATES) {
            return ResponseEntity
                    .status(200)
                    .body(ProjectResults.builder()
                            .name(project.getName())
                            .candidatesInformation(project.getCandidatesInformation())
                            .status(status)
                            .build());
        }
        return ResponseEntity
                .status(200)
                .body(ProjectResults.builder()
                        .name(project.getName())
                        .status(status)
                        .build());
    }
}
