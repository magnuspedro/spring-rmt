package br.com.metrics.metricsagent.extraction;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.file.extractor.FileExtractor;
import br.com.magnus.config.starter.projects.Project;
import br.com.metrics.metricsagent.qualityAttributes.QualityAttributesProcessor;
import br.com.metrics.metricsagent.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ExtractProjects {

    private final ProjectRepository projectRepository;
    private final FileExtractor fileExtractor;

    @SneakyThrows
    public Map<String, Path> extractProject(String id) {
        var project = projectRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        var originalProject = fileExtractor.extract(project.getBucket(), project.getId());
        var refactoredProject = fileExtractor.extract(project.getRefactoredBucket(), project.getId());
        var originalBasePath = Files.createTempDirectory("original-" + project.getId()).toAbsolutePath();
        var refactoredBasePath = Files.createTempDirectory("refactored-" + project.getId()).toAbsolutePath();

        originalProject.forEach(file -> createTempFile(originalBasePath.toString(), file));
        refactoredProject.forEach(file -> createTempFile(refactoredBasePath.toString(), file));

        return Map.of("original", originalBasePath, "refactored", refactoredBasePath);
    }


    @SneakyThrows
    private void createTempFile(String basePath, JavaFile javaFile) {
        var file = new File(basePath + "/" + javaFile.getFullName());
        file.getParentFile().mkdirs();
        file.createNewFile();
        var fileWriter = new FileWriter(file);
        fileWriter.write(javaFile.getOriginalClass());
        fileWriter.close();
    }
}
