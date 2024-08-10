package br.com.magnus.config.starter.file.extractor;

import br.com.magnus.config.starter.projects.BaseProject;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.repository.S3ProjectRepository;
import br.com.magnus.config.starter.file.JavaFile;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@RequiredArgsConstructor
public class S3FileExtractor implements FileExtractor {
    private static final String EXTENSION = ".java";
    private static final String SEPARATOR = FileSystems.getDefault().getSeparator();
    private final S3ProjectRepository s3ProjectRepository;

    @Override
    public List<JavaFile> extract(BaseProject project) {
        Assert.notNull(project, "Project cannot be null");
        return extract(project.getBucket(), project.getId());
    }

    @SneakyThrows
    public List<JavaFile> extract(String bucket, String id) {
        Assert.notNull(bucket, "Bucket cannot be null");
        Assert.notNull(id, "Id cannot be null");

        ZipEntry zipEntry;
        var javaFiles = new ArrayList<JavaFile>();
        var compressedProject = s3ProjectRepository.download(bucket, id);
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

    @Override
    @SneakyThrows
    public List<JavaFile> extractRefactoredFiles(String bucket, String id, List<String> candidateFiles) {
        Assert.notNull(bucket, "Bucket cannot be null");
        Assert.notNull(id, "Id cannot be null");
        Assert.notNull(candidateFiles, "Candidate files cannot be null");

        ZipEntry zipEntry;
        var javaFiles = new ArrayList<JavaFile>();
        var compressedProject = s3ProjectRepository.download(bucket, id);
        var zipInputStream = new ZipInputStream(compressedProject);

        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            Optional.of(zipEntry)
                    .filter(f -> candidateFiles.contains(f.getName()))
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
        var path = name.split("\\" + EXTENSION)[0].split(SEPARATOR);
        return path[path.length - 1] + EXTENSION;
    }

    private String getPath(String name) {
        var path = name.split(SEPARATOR);
        if (path.length == 1) return "";
        path = Arrays.copyOfRange(path, 0, path.length - 1);
        return String.join(File.separator, path) + File.separator;
    }

    @SneakyThrows
    private String getString(ZipInputStream zipInputStream) {
        return new String(zipInputStream.readAllBytes());
    }
}
