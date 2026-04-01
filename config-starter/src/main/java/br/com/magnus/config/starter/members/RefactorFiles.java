package br.com.magnus.config.starter.members;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import lombok.Builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
public class RefactorFiles {

    @Builder.Default
    private List<JavaFile> files = new ArrayList<>();
    @Builder.Default
    private List<RefactoringCandidate> candidates = new ArrayList<>();
    @Builder.Default
    private Set<String> filesChanged = new HashSet<>();

    public List<JavaFile> files() {
        return this.files;
    }

    public List<RefactoringCandidate> candidates() {
        return this.candidates;
    }

    public Set<String> filesChanged() {
        return this.filesChanged;
    }

    public RefactoringCandidate candidate() {
        return this.candidates.getFirst();
    }

    public void addFileChanged(String file) {
        this.filesChanged.add(file);
    }

    public void add(JavaFile javaFile) {
        this.files.add(javaFile);
        this.filesChanged.add(javaFile.getFullName());
    }
}
