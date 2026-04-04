package br.com.magnus.config.starter.projects;


import io.awspring.cloud.s3.ObjectMetadata;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@RedisHash("project")
public class BaseProject {
    @Id
    private String id;
    private String name;
    private String bucket;
    private ObjectMetadata metadata;
    @Builder.Default
    private List<CandidateInformation> candidatesInformation = new ArrayList<>();
    @Builder.Default
    private Set<ProjectStatus> status = new LinkedHashSet<>();
    @TimeToLive
    @Builder.Default
    private Long timeToLive = 86400L;
    private Long createdAt;
    private Long updatedAt;

    public void addStatus(ProjectStatus status) {
        this.getStatus().add(status);
    }
}
