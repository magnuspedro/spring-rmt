package br.com.magnus.config.starter.projects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Data
@Builder
@RedisHash("project")
public class Project {
    private @Id String id;
    private String name;
    private String bucket;
    private List<ProjectStatus> status;

    @JsonIgnore
    private Map<String, String> metadata;
    @JsonIgnore
    private Long size;
    @JsonIgnore
    private String contentType;
    @JsonIgnore
    private byte[] content;


    @JsonIgnore
    public InputStream getContentInputStream() {
        return new ByteArrayInputStream(this.content);
    }
}
