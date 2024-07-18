package br.com.magnus.config.starter.projects;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import io.awspring.cloud.s3.ObjectMetadata;
import lombok.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseProject {
    private Long size;
    private String contentType;
    private byte[] zipContent;
    private List<JavaFile> originalContent;
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

    public InputStream getZipInputStreamContent() {
        return new ByteArrayInputStream(this.zipContent);
    }

    public void addStatus(ProjectStatus status) {
        super.getStatus().add(status);
    }

    public void addRefactorFiles(RefactorFiles refactorFiles) {
        if (this.refactorFiles == null)
            this.refactorFiles = new ArrayList<>();
        this.refactorFiles.add(refactorFiles);
    }

    public void addAllRefactorFiles(List<RefactorFiles> refactorFiles) {
        if (this.refactorFiles == null)
            this.refactorFiles = new ArrayList<>();
        this.refactorFiles.addAll(refactorFiles);
    }

    public void addCandidateInformation(CandidateInformation candidateInformation) {
        if (super.getCandidatesInformation() == null)
            super.setCandidatesInformation(new ArrayList<>());
        super.getCandidatesInformation().add(candidateInformation);
    }

}
