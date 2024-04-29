package br.com.detection.detectionagent.refactor;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.repository.S3ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
                    .ifPresent(file -> javaFiles.add(JavaFile.builder()
                            .name(getName(file.getName()))
                            .path(getPath(file.getName()))
                            .originalClass(getString(zipInputStream))
                            .build()));
        }

        zipInputStream.closeEntry();
        zipInputStream.close();

        return javaFiles;
    }

    private String getName(String name) {
        var path = name.split(EXTENSION)[0].split("/");
        return path[path.length - 1] + EXTENSION;
    }

    private String getPath(String name) {
        var path = name.split(File.separator);
        if (path.length == 1) return "";
        path = Arrays.copyOfRange(path, 0, path.length - 1);
        return String.join(File.separator, path) + File.separator;
    }

    @SneakyThrows
    private String getString(ZipInputStream zipInputStream) {
        return new String(zipInputStream.readAllBytes());
    }
}
