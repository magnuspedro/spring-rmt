package br.com.magnus.config.starter.extractor;

import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.file.JavaFile;

import java.util.List;

public interface FileExtractor {
   List<JavaFile> extract(Project project);

    List<JavaFile> extract(String bucket, String id);
}
