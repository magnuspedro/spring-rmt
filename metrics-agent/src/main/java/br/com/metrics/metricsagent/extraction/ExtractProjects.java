package br.com.metrics.metricsagent.extraction;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.file.extractor.FileExtractor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class ExtractProjects {


    private final FileExtractor fileExtractor;

    @SneakyThrows
    public Path extractProject(String id, String bucket) {
        var files = fileExtractor.extract(bucket, id);
        var basePath = Files.createTempDirectory(id).toAbsolutePath();
        files.forEach(file -> createTempFile(basePath.toString(), file));

        return basePath;
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
