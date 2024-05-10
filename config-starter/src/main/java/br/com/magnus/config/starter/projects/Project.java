package br.com.magnus.config.starter.projects;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@RedisHash("project")
public class Project {
    private @Id String id;
    private String name;
    private String bucket;
    private String refactoredBucket;
    private List<RefactoringCandidate> refactoringCandidates;
    @Builder.Default
    private Set<ProjectStatus> status = Set.of(ProjectStatus.RECEIVED);

    @JsonIgnore
    private Map<String, String> metadata;
    @JsonIgnore
    private Long size;
    @JsonIgnore
    private String contentType;
    @JsonIgnore
    private byte[] zipContent;
    @JsonIgnore
    private List<JavaFile> originalContent;
    @JsonIgnore
    private List<JavaFile> refactoredContent;


    @JsonIgnore
    public InputStream getZipInputStreamContent() {
        return new ByteArrayInputStream(this.zipContent);
    }

    @JsonIgnore
    public void addStatus(ProjectStatus status) {
        this.status.add(status);
    }
}
