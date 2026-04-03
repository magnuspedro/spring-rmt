package br.com.magnus.detectionandrefactoring.consumer.repository;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.config.starter.repository.S3ProjectRepository;
import br.com.magnus.detectionandrefactoring.repository.ProjectRepository;
import br.com.magnus.detectionandrefactoring.repository.ProjectUpdater;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectUpdaterTest {

    private ProjectUpdater projectUpdater;
    @Mock
    private Project project;
    @Mock
    private S3ProjectRepository s3ProjectRepository;
    @Mock
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        projectUpdater = new ProjectUpdater(projectRepository, s3ProjectRepository);
    }

    @Test
    void shouldAddNoCandidatesStatusWhenNoRefactoringCandidates() {
        projectUpdater.saveProject(project);

        verify(project, times(1)).addStatus(ProjectStatus.NO_CANDIDATES);
    }

    @Test
    void shouldAddRefactoredStatusAndUploadWhenRefactoringCandidatesExist() {
        when(project.getRefactorFiles()).thenReturn(List.of(RefactorFiles.builder()
                .candidates(List.of(mock(RefactoringCandidate.class)))
                .build()));

        projectUpdater.saveProject(project);

        verify(project, times(1)).addStatus(ProjectStatus.REFACTORED);
        verify(s3ProjectRepository, times(1)).upload(any(), any(), any(), any());
    }

    @Test
    @SneakyThrows
    void shouldUploadFullProjectSnapshotForCandidate() {
        var candidate = mock(RefactoringCandidate.class);
        when(candidate.getId()).thenReturn("candidate-1");
        var changedFile = javaFile("src/main/java/example/Changed.java", "class Changed { int value() { return 2; } }");
        var refactorFiles = RefactorFiles.builder()
                .candidates(List.of(candidate))
                .files(List.of(changedFile))
                .build();
        var uploadedZip = new AtomicReference<byte[]>();

        when(project.getRefactorFiles()).thenReturn(List.of(refactorFiles));
        when(project.getZipContent()).thenReturn(new byte[]{1});
        when(project.getZipInputStreamContent()).thenReturn(zipInputStream(Map.of(
                "src/main/java/example/OriginalOnly.java", "class OriginalOnly {}",
                "src/main/java/example/Changed.java", "class Changed { int value() { return 1; } }"
        )));
        doAnswer(invocation -> {
            uploadedZip.set(invocation.<InputStream>getArgument(2).readAllBytes());
            return null;
        }).when(s3ProjectRepository).upload(any(), eq("candidate-1"), any(), any());

        projectUpdater.saveProject(project);

        verify(s3ProjectRepository).upload(any(), eq("candidate-1"), any(), any());
        var files = unzip(new ByteArrayInputStream(uploadedZip.get()));
        assertThat(files.keySet())
                .containsExactlyInAnyOrder(
                        "src/main/java/example/OriginalOnly.java",
                        "src/main/java/example/Changed.java");
        assertThat(files.get("src/main/java/example/Changed.java"))
                .contains("return 2;");
        assertThat(files.get("src/main/java/example/OriginalOnly.java"))
                .isEqualTo("class OriginalOnly {}");
    }

    @Test
    @SneakyThrows
    void shouldFallbackToOriginalContentWhenZipContentIsUnavailable() {
        var candidate = mock(RefactoringCandidate.class);
        when(candidate.getId()).thenReturn("candidate-2");
        var originalFile = javaFile("src/main/java/example/OriginalOnly.java", "class OriginalOnly {}");
        var changedFile = javaFile("src/main/java/example/Changed.java", "class Changed { int value() { return 2; } }");
        var refactorFiles = RefactorFiles.builder()
                .candidates(List.of(candidate))
                .files(List.of(changedFile))
                .build();
        var uploadedZip = new AtomicReference<byte[]>();

        when(project.getRefactorFiles()).thenReturn(List.of(refactorFiles));
        when(project.getOriginalContent()).thenReturn(List.of(
                originalFile,
                javaFile("src/main/java/example/Changed.java", "class Changed { int value() { return 1; } }")));
        doAnswer(invocation -> {
            uploadedZip.set(invocation.<InputStream>getArgument(2).readAllBytes());
            return null;
        }).when(s3ProjectRepository).upload(any(), eq("candidate-2"), any(), any());

        projectUpdater.saveProject(project);

        verify(s3ProjectRepository).upload(any(), eq("candidate-2"), any(), any());
        var files = unzip(new ByteArrayInputStream(uploadedZip.get()));
        assertThat(files.get("src/main/java/example/Changed.java"))
                .contains("return 2;");
        assertThat(files.get("src/main/java/example/OriginalOnly.java"))
                .contains("class OriginalOnly");
    }

    private JavaFile javaFile(String fullName, String content) {
        var lastSlash = fullName.lastIndexOf('/') + 1;
        return JavaFile.builder()
                .name(fullName.substring(lastSlash))
                .path(fullName.substring(0, lastSlash))
                .originalClass(content)
                .parsed(br.com.magnus.config.starter.configuration.JavaParserSingleton.getInstance()
                        .parse(content)
                        .getResult()
                        .orElseThrow())
                .build();
    }

    private InputStream zipInputStream(Map<String, String> files) throws Exception {
        var bos = new ByteArrayOutputStream();
        try (var zos = new ZipOutputStream(bos)) {
            for (var entry : files.entrySet()) {
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                zos.write(entry.getValue().getBytes());
                zos.closeEntry();
            }
        }
        return new ByteArrayInputStream(bos.toByteArray());
    }

    private Map<String, String> unzip(InputStream inputStream) throws Exception {
        var files = new java.util.LinkedHashMap<String, String>();
        try (var zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                files.put(zipEntry.getName(), new String(zipInputStream.readAllBytes()));
            }
        }
        return files;
    }
}
