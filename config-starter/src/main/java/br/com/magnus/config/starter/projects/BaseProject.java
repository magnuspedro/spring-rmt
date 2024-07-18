package br.com.magnus.config.starter.projects;


import io.awspring.cloud.s3.ObjectMetadata;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
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
