package br.com.magnus.config.starter.members;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import lombok.Builder;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;

@Builder
public record RefactorFiles(
        List<JavaFile> files,
        List<RefactoringCandidate> candidates,
        List<String> filesChanged) {

    public static class RefactorFilesBuilder {
        private List<JavaFile> files = new ArrayList<>();
        private List<String> filesChanged = new ArrayList<>();
        private List<RefactoringCandidate> candidates = new ArrayList<>();
    }

    public void addFileChanged(String file) {
        this.filesChanged.add(file);
    }

    public void add(JavaFile javaFile) {
        this.files.add(javaFile);
        this.filesChanged.add(javaFile.getFullName());
    }

    public RefactoringCandidate candidate() {
        return this.candidates.getFirst();
    }
}
