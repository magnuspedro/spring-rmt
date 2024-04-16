package br.com.detection.detectionagent.refactor;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.messages.projects.Project;
import br.com.messages.repository.S3ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExtractFiles {
    private static final String EXTENSION = ".java";
    private final S3ProjectRepository s3ProjectRepository;

    @SneakyThrows
    public List<JavaFile> extract(Project project) {
        Assert.notNull(project, "Project cannot be null");

        ZipEntry zipEntry;
        List<JavaFile> javaFiles = new ArrayList<>();

        var compressedProject = s3ProjectRepository.download(project.getBucket(), project.getId());

        var zipInputStream = new ZipInputStream(compressedProject);
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {

            Optional.of(zipEntry)
                    .filter(f -> f.getName().endsWith(EXTENSION))
                    .flatMap(entry -> createTempFile(entry.getName(), zipInputStream))
                    .ifPresent(file -> javaFiles.add(JavaFile.builder()
                            .name(file.getName())
                            .path(file.getPath())
                            .inputStream(openFile(file))
                            .build()));
        }

        zipInputStream.closeEntry();
        zipInputStream.close();

        return javaFiles;
    }

    private Optional<File> createTempFile(String name, ZipInputStream zipInputStream) {
        var fullName = name.split(EXTENSION);
        try {
            var file = File.createTempFile(fullName[0], EXTENSION);
            FileUtils.copyToFile(zipInputStream, file);
            return Optional.of(file);
        } catch (IOException e) {
            log.warn("Error creating temp file, {}", name, e);
        }
        return Optional.empty();
    }

    @SneakyThrows
    private InputStream openFile(File file) {
        return FileUtils.openInputStream(file);
    }
}
