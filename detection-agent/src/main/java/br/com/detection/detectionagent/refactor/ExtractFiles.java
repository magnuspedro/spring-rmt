package br.com.detection.detectionagent.refactor;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.messages.projects.Project;
import br.com.messages.repository.S3ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExtractFiles {

    private static final String EXTENSION = ".class";
    private final S3ProjectRepository s3ProjectRepository;

    @SneakyThrows
    public List<JavaFile> extract(Project project) {
        ZipEntry zipEntry;
        List<JavaFile> javaFiles = new ArrayList<>();

        var compressedProject = s3ProjectRepository.download(project.getBucket(), project.getId());

        var zipInputStream = new ZipInputStream(compressedProject);
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            Optional.of(zipEntry)
                    .filter(f -> f.getName().endsWith(EXTENSION))
                    .map(entry -> {
                        var file = new File(entry.getName());
                        return javaFiles.add(JavaFile.builder()
                                .name(file.getName())
                                .path(file.getPath())
                                .inputStream(zipInputStream)
                                .build());
                    });
        }

        return javaFiles;
    }
}
