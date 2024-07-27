package br.com.magnus.config.starter.file.extractor;

import br.com.magnus.config.starter.projects.BaseProject;
import br.com.magnus.config.starter.file.JavaFile;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.util.List;

public interface FileExtractor {
   List<JavaFile> extract(BaseProject project);

    List<JavaFile> extract(String bucket, String id);

    @SneakyThrows
    List<JavaFile> extractRefactoredFiles(String bucket, String id, List<String> candidateFiles);
}
