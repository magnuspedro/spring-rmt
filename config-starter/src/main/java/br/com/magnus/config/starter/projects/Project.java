package br.com.magnus.config.starter.projects;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.awspring.cloud.s3.ObjectMetadata;
import lombok.*;
import org.springframework.data.annotation.Transient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseProject {
    @Transient
    @JsonIgnore
    private Long size;
    @Transient
    @JsonIgnore
    private String contentType;
    @Transient
    @JsonIgnore
    private byte[] zipContent;
    @Transient
    @JsonIgnore
    private List<JavaFile> originalContent;
    @Transient
    @JsonIgnore
    private List<RefactorFiles> refactorFiles;

    @Builder
    public Project(Long size, String contentType, byte[] zipContent, List<JavaFile> originalContent, String id, String name, String bucket, ObjectMetadata objectMetadata, List<RefactorFiles> refactorFiles) {
        super(id, name, bucket, objectMetadata, new ArrayList<>(), new HashSet<>(Set.of(ProjectStatus.RECEIVED)));

        this.size = size;
        this.contentType = contentType;
        this.zipContent = zipContent;
        this.originalContent = originalContent;
        this.refactorFiles = refactorFiles;
    }

    @Transient
    @JsonIgnore
    public InputStream getZipInputStreamContent() {
        return new ByteArrayInputStream(this.zipContent);
    }

    @Transient
    @JsonIgnore
    public void addStatus(ProjectStatus status) {
        super.getStatus().add(status);
    }

    @Transient
    @JsonIgnore
    public void addCandidateInformation(CandidateInformation candidateInformation) {
        if (super.getCandidatesInformation() == null)
            super.setCandidatesInformation(new ArrayList<>());
        super.getCandidatesInformation().add(candidateInformation);
    }

}
