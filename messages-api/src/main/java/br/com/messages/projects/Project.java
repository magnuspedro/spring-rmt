package br.com.messages.projects;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.redis.core.RedisHash;

import java.io.InputStream;
import java.util.List;

@Getter
@Builder
@ToString
@RedisHash("project")
public class Project {
    private @Id String id;
    private String name;
    private String bucket;
    private List<ProjectStatus> status;

    @Transient
    private Long size;
    @Transient
    private String contentType;
    @Transient
    private InputStream inputStream;
}
