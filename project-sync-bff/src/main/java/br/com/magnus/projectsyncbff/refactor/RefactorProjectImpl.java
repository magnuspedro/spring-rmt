package br.com.magnus.projectsyncbff.refactor;

import br.com.magnus.config.starter.configuration.BucketProperties;
import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.file.compressor.FileCompressor;
import br.com.magnus.config.starter.file.extractor.FileExtractor;
import br.com.magnus.config.starter.projects.BaseProject;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.config.starter.repository.S3ProjectRepository;
import br.com.magnus.projectsyncbff.gateway.SendProject;
import br.com.magnus.projectsyncbff.repository.ProjectRepository;
import io.awspring.cloud.s3.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
    public void process(Project project) {
        var projectOpt = projectRepository.findById(project.getId());
        if (checkFroExistingProject(projectOpt)) {
            return;
        }

        var metadata = ObjectMetadata.builder()
                .contentType(project.getContentType())
                .metadata("FileName", project.getName())
                .build();

        project.setMetadata(metadata);
        project.setBucket(bucket.getProjectBucket());
        project.addStatus(ProjectStatus.EVALUATING_CANDIDATES);

        s3ProjectRepository.upload(bucket.getProjectBucket(), project.getId(), project.getZipInputStreamContent(), metadata);
        projectRepository.save(project.getBaseProject());
        sendProject.send(project.getId());
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
        var status = project.getStatus().stream().toList();
        if (!status.contains(ProjectStatus.FINISHED) && !status.contains(ProjectStatus.NO_CANDIDATES)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Please try uploading the project again. If it doesn't work, contact the support team.");
        }
        log.info("Elapsed time: {}", TimeUnit.NANOSECONDS.toSeconds(project.getUpdatedAt() - project.getCreatedAt()));
        return ProjectResults.builder()
                .name(project.getName())
                .candidatesInformation(project.getCandidatesInformation())
                .status(status.getLast())
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

    private boolean checkFroExistingProject(Optional<BaseProject> optProject) {
        if (optProject.isEmpty()) {
            return false;
        }
        var project = optProject.get();
        var projectList = project.getStatus().stream().toList();
        if (projectList.contains(ProjectStatus.FINISHED) || projectList.contains(ProjectStatus.NO_CANDIDATES)) {
            log.info("Project already exists in the database, skipping upload");
            return true;
        }
        projectRepository.deleteById(project.getId());
        return false;
    }
}
