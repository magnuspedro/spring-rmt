package br.com.magnus.config.starter.members;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import lombok.Builder;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
public record RefactorFiles(
        List<JavaFile> files,
        List<RefactoringCandidate> candidates,
        Set<String> filesChanged) {

    public static class RefactorFilesBuilder {
        private List<JavaFile> files = new ArrayList<>();
        private Set<String> filesChanged = new HashSet<>();
        private List<RefactoringCandidate> candidates = new ArrayList<>();
    }

    public void addFileChanged(String file) {
        this.filesChanged.add(file);
    }

    public void add(JavaFile javaFile) {
        this.files.add(javaFile);
        this.filesChanged.add(javaFile.getFullName());
    }

    public Set<String> filesChanged(){
        return this.filesChanged;
    }

    public RefactoringCandidate candidate() {
        return this.candidates.getFirst();
    }
}
