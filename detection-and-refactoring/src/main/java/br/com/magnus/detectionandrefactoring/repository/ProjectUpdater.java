package br.com.magnus.detectionandrefactoring.repository;

import br.com.magnus.config.starter.file.compressor.FileCompressor;
import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.projects.CandidateInformation;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.config.starter.repository.S3ProjectRepository;
import com.github.javaparser.ast.CompilationUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * Updates and persists project data after refactoring operations.
 * <p>
 * Handles saving refactored files, updating project status, and uploading
 * results to S3 storage.
 */
@Component
@RequiredArgsConstructor
public class ProjectUpdater {

    /**
     * Repository for project persistence.
     */
    private final ProjectRepository projectRepository;

    /**
     * Repository for S3 file storage.
     */
    private final S3ProjectRepository s3ProjectRepository;

    /**
     * Saves a project after refactoring, including file uploads.
     *
     * @param project the project to save
     */
    public void saveProject(Project project) {
        saveFiles(project);
        projectRepository.save(project.getBaseProject());
    }

    /**
     * Saves refactored files to S3 and updates project status.
     *
     * @param project the project with refactored files
     */
    private void saveFiles(Project project) {
        if (project.getRefactorFiles() == null || project.getRefactorFiles().isEmpty()) {
            project.addStatus(ProjectStatus.NO_CANDIDATES);
            return;
        }
        project.addStatus(ProjectStatus.REFACTORED);
        project.getRefactorFiles().forEach(refactorFiles -> {
            project.addCandidateInformation(CandidateInformation.builder()
                    .id(refactorFiles.candidate().getId())
                    .designPattern(refactorFiles.candidate().getEligiblePattern())
                    .reference(refactorFiles.candidate().getReference())
                    .filesChanged(refactorFiles.filesChanged())
                    .build());
            var inputStream = Optional.ofNullable(project.getZipContent())
                    .map(ignored -> FileCompressor.replaceFiles(project.getZipInputStreamContent(),
                            refactorFiles.files().stream()
                                    .collect(java.util.stream.Collectors.toMap(
                                            file -> file.getFullName(),
                                            file -> file))))
                    .orElseGet(() -> FileCompressor.compress(buildSnapshot(project, refactorFiles.files())));
            s3ProjectRepository.upload(project.getBucket(), refactorFiles.candidate().getId(), inputStream, project.getMetadata());
        });
    }

    /**
     * Builds a snapshot of all project files.
     *
     * @param project         the project
     * @param refactoredFiles files that were refactored
     * @return list of all files
     */
    private List<JavaFile> buildSnapshot(Project project, List<JavaFile> refactoredFiles) {
        var snapshot = new LinkedHashMap<String, JavaFile>();
        Optional.ofNullable(project.getOriginalContent()).orElseGet(List::of)
                .forEach(file -> snapshot.put(file.getFullName(), ensureParsed(file)));
        refactoredFiles.forEach(file -> snapshot.put(file.getFullName(), ensureParsed(file)));
        return List.copyOf(snapshot.values());
    }

    /**
     * Ensures a Java file has a parsed CompilationUnit.
     *
     * @param file the file to check/parse
     * @return file with parsed content
     */
    private JavaFile ensureParsed(JavaFile file) {
        if (file.getCompilationUnit() instanceof CompilationUnit) {
            return file;
        }
        return JavaFile.builder()
                .name(file.getName().toString())
                .path(file.getPath())
                .originalClass(file.getOriginalClass())
                .parsed(br.com.magnus.config.starter.configuration.JavaParserSingleton.getInstance()
                        .parse(file.getOriginalClass())
                        .getResult()
                        .orElseThrow(() -> new IllegalArgumentException("Error parsing JavaFile")))
                .build();
    }
}
