package br.com.magnus.metricscalculator.repository;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.file.extractor.FileExtractor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExtractProjects {
    private final FileExtractor fileExtractor;

    @SneakyThrows
    public Path extractProject(String id, String bucket) {
        Assert.notNull(id, "Id cannot be null");
        Assert.notNull(bucket, "Bucket cannot be null");

        var files = fileExtractor.extract(bucket, id);
        var basePath = Files.createTempDirectory("project-").toAbsolutePath().normalize();
        files.forEach(file -> createTempFile(basePath, file));

        return basePath;
    }

    @SneakyThrows
    private void createTempFile(Path basePath, JavaFile javaFile) {
        Assert.notNull(javaFile, "Java file cannot be null");
        Assert.notNull(javaFile.getName(), "Name cannot be null");
        Assert.notNull(javaFile.getPath(), "Path cannot be null");
        Assert.notNull(javaFile.getOriginalClass(), "Original class cannot be null");

        var safePath = resolveInsideBasePath(basePath, javaFile.getPath(), javaFile.getName());
        Files.createDirectories(safePath.getParent());
        Files.writeString(safePath, javaFile.getOriginalClass(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private Path resolveInsideBasePath(Path basePath, String relativePath, String fileName) {
        var normalizedRelativePath = relativePath.replace("\\", "/");
        var normalizedName = fileName.replace("\\", "/");
        var candidate = basePath.resolve(normalizedRelativePath).resolve(normalizedName).normalize();
        if (!candidate.startsWith(basePath)) {
            throw new IllegalArgumentException("Invalid file path");
        }
        return candidate;
    }
}
