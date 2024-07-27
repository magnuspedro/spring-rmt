package br.com.magnus.config.starter.projects;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import io.awspring.cloud.s3.ObjectMetadata;
import lombok.*;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

@Getter
@Setter
@Builder
@ToString
public class Project {
    private Long size;
    private String contentType;
    private byte[] zipContent;
    private List<JavaFile> originalContent;
    private List<RefactorFiles> refactorFiles;
    private BaseProject baseProject;

    public String getId(){
        return Optional.ofNullable(baseProject)
                .map(BaseProject::getId)
                .orElse(null);
    }

    public String getBucket(){
        return Optional.ofNullable(baseProject)
                .map(BaseProject::getBucket)
                .orElse(null);
    }

    public String getName(){
        return Optional.ofNullable(baseProject)
                .map(BaseProject::getName)
                .orElse(null);
    }

    public ObjectMetadata getMetadata(){
        return Optional.ofNullable(baseProject)
                .map(BaseProject::getMetadata)
                .orElse(null);
    }

    public List<CandidateInformation> getCandidatesInformation(){
        return Optional.ofNullable(baseProject)
                .map(BaseProject::getCandidatesInformation)
                .orElse(null);
    }


    public Set<ProjectStatus> getStatus(){
        return Optional.ofNullable(baseProject)
                .map(BaseProject::getStatus)
                .orElse(null);
    }

    public void setName(String name){
        Optional.ofNullable(baseProject)
                .ifPresent(bp -> bp.setName(name));
    }

    public void setMetadata(ObjectMetadata metadata){
        Optional.ofNullable(baseProject)
                .ifPresent(bp -> bp.setMetadata(metadata));
    }

    public void setBucket(String bucket){
        Optional.ofNullable(baseProject)
                .ifPresent(bp -> bp.setBucket(bucket));
    }

    public void setId(String id){
        Optional.ofNullable(baseProject)
                .ifPresent(bp -> bp.setId(id));
    }

    public InputStream getZipInputStreamContent() {
        return new ByteArrayInputStream(this.zipContent);
    }

    public void addStatus(ProjectStatus status) {
        this.baseProject.getStatus().add(status);
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
        Assert.notNull(baseProject, "Base Project cannot be null");
        if (this.baseProject.getCandidatesInformation() == null)
            this.baseProject.setCandidatesInformation(new ArrayList<>());
        this.baseProject.getCandidatesInformation().add(candidateInformation);
    }

}
