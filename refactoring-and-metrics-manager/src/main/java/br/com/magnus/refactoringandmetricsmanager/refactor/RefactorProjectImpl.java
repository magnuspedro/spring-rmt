package br.com.magnus.refactoringandmetricsmanager.refactor;

import br.com.magnus.config.starter.configuration.BucketProperties;
import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.file.compressor.FileCompressor;
import br.com.magnus.config.starter.file.extractor.FileExtractor;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.config.starter.repository.S3ProjectRepository;
import br.com.magnus.refactoringandmetricsmanager.gateway.SendProject;
import br.com.magnus.refactoringandmetricsmanager.repository.ProjectRepository;
import io.awspring.cloud.s3.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefactorProjectImpl implements RefactorProject {

    private final S3ProjectRepository s3ProjectRepository;
    private final ProjectRepository projectRepository;
    private final SendProject sendProject;
    private final BucketProperties bucket;
    private final FileExtractor fileExtractor;

    @Override
    public Project process(Project project) {
        var metadata = ObjectMetadata.builder()
                .contentType(project.getContentType())
                .metadata("FileName", project.getName())
                .build();

        project.setMetadata(metadata);
        project.setBucket(bucket.getProjectBucket());
        project.addStatus(ProjectStatus.EVALUATING_CANDIDATES);

        s3ProjectRepository.upload(bucket.getProjectBucket(), project.getId(), project.getZipInputStreamContent(), metadata);
        projectRepository.save(project);
        sendProject.send(project);

        return project;
    }

    @Override
    public ProjectResults retrieve(String id) {
        var project = projectRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        var status = project.getStatus().stream().toList().getLast();
        return ProjectResults.builder()
                .name(project.getName())
                .candidatesInformation(project.getCandidatesInformation())
                .status(status)
                .build();
    }

    @SneakyThrows
    public ProjectResults retrieveRetryable(String id) {
        var project = projectRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        var status = project.getStatus().stream().toList().getLast();
        if (project.getStatus() != null && (status != ProjectStatus.FINISHED && status != ProjectStatus.NO_CANDIDATES)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Please try uploading the project again. If it doesn't work, contact the support team.");
        }
        return ProjectResults.builder()
                .name(project.getName())
                .candidatesInformation(project.getCandidatesInformation())
                .status(status)
                .build();
    }

    @SneakyThrows
    @Override
    public String downloadProject(String projectId, List<String> candidatesIds) {
        log.info("Downloading project: {}, candidates: {}", projectId, candidatesIds);
        var candidateFiles = new HashMap<String, JavaFile>();
        var project = projectRepository.findById(projectId).orElseThrow(IllegalArgumentException::new);
        var projectZip = s3ProjectRepository.download(project.getBucket(), project.getId());
        project.getCandidatesInformation().stream()
                .filter(candidate -> candidatesIds.contains(candidate.getId()))
                .map(candidate -> fileExtractor.extractRefactoredFiles(project.getBucket(), candidate.getId(), candidate.getFilesChanged().stream().toList()))
                .flatMap(List::stream)
                .forEach(javaFile -> candidateFiles.put(javaFile.getFullName(), javaFile));

        var refactoredProject = FileCompressor.replaceFiles(projectZip, candidateFiles);
        var s3Resource = s3ProjectRepository.upload(bucket.getDownloaderBucket(), project.getId(), refactoredProject, project.getMetadata());

        return s3Resource.getURL().toString();
    }
}
