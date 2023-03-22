package br.com.intermediary.intermediaryagent.refactor;

import br.com.intermediary.intermediaryagent.gateway.SendProject;
import br.com.intermediary.intermediaryagent.repository.ProjectRepository;
import br.com.messages.configuration.BucketProperties;
import br.com.messages.projects.Project;
import br.com.messages.repository.S3ProjectRepository;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefactorProjectImpl implements RefactorProject {

    private final S3ProjectRepository s3ProjectRepository;
    private final ProjectRepository projectRepository;
    private final SendProject sendProject;
    private final BucketProperties bucket;

    @Override
    public Project process(Project project) {
        var metadata = new ObjectMetadata();
        metadata.setContentType(project.getContentType());
        metadata.setContentLength(project.getSize());
        Optional.ofNullable(project.getName())
                .ifPresent(p -> metadata.setUserMetadata(Map.of("FileName", p)));


        s3ProjectRepository.upload(bucket.getProjectBucket(), project.getId(), project.getInputStream(), metadata);
        projectRepository.save(project);
        sendProject.send(project);

        return project;
    }
}
