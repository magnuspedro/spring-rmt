package br.com.intermediary.intermediaryagent.refactor;

import br.com.intermediary.intermediaryagent.gateway.SendProject;
import br.com.intermediary.intermediaryagent.repository.ProjectRepository;
import br.com.magnus.config.starter.configuration.BucketProperties;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.config.starter.repository.S3ProjectRepository;
import io.awspring.cloud.s3.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


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
    public ProjectResults retrieve(String id) {
        var project = projectRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        var status = project.getStatus().stream().toList().getLast();
        return ProjectResults.builder()
                .name(project.getName())
                .candidatesInformation(project.getCandidatesInformation())
                .status(status)
                .build();
    }

    @SneakyThrows
    public ProjectResults retrieveRetryable(String id) {
        var project = projectRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        var status = project.getStatus().stream().toList().getLast();
        if (project.getStatus() != null && (status != ProjectStatus.FINISHED && status != ProjectStatus.NO_CANDIDATES)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Please try uploading the project again. If it doesn't work, contact the support team.");
        }
        return ProjectResults.builder()
                .name(project.getName())
                .candidatesInformation(project.getCandidatesInformation())
                .status(status)
                .build();
    }
}
