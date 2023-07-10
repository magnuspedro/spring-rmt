package br.com.intermediary.intermediaryagent.refactor;

import br.com.intermediary.intermediaryagent.gateway.SendProject;
import br.com.intermediary.intermediaryagent.repository.ProjectRepository;
import br.com.messages.configuration.BucketProperties;
import br.com.messages.projects.Project;
import br.com.messages.repository.S3ProjectRepository;
import io.awspring.cloud.s3.ObjectMetadata;
import lombok.RequiredArgsConstructor;
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

        project.setMetadata(metadata.getMetadata());
        project.setBucket(bucket.getProjectBucket());


        s3ProjectRepository.upload(bucket.getProjectBucket(), project.getId(), project.getContentInputStream(), metadata);
        projectRepository.save(project);
        sendProject.send(project);

        return project;
    }
}
