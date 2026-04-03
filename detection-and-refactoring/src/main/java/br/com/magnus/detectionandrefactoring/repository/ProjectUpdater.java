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
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProjectUpdater {

    private final ProjectRepository projectRepository;
    private final S3ProjectRepository s3ProjectRepository;

    public void saveProject(Project project) {
        saveFiles(project);
        projectRepository.save(project.getBaseProject());

    }

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
                    .<java.io.InputStream>map(ignored -> FileCompressor.replaceFiles(project.getZipInputStreamContent(), refactorFiles.files().stream()
                            .collect(Collectors.toMap(file -> file.getFullName(), file -> file))))
                    .orElseGet(() -> FileCompressor.compress(buildSnapshot(project, refactorFiles.files())));
            s3ProjectRepository.upload(project.getBucket(), refactorFiles.candidate().getId(), inputStream, project.getMetadata());
        });
    }

    private List<JavaFile> buildSnapshot(Project project, List<JavaFile> refactoredFiles) {
        var snapshot = new LinkedHashMap<String, JavaFile>();
        Optional.ofNullable(project.getOriginalContent()).orElseGet(List::of)
                .forEach(file -> snapshot.put(file.getFullName(), ensureParsed(file)));
        refactoredFiles.forEach(file -> snapshot.put(file.getFullName(), ensureParsed(file)));
        return List.copyOf(snapshot.values());
    }

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
