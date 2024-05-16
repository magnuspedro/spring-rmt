package br.com.magnus.config.starter.projects;


import br.com.magnus.config.starter.members.RefactorFiles;
import io.awspring.cloud.s3.ObjectMetadata;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("project")
public abstract class BaseProject {
    @Id
    private String id;
    private String name;
    private String bucket;
    private ObjectMetadata metadata;
    private List<CandidateInformation> candidatesInformation;
    private Set<ProjectStatus> status;
}
