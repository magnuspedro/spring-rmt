package br.com.magnus.config.starter.extractor;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.file.extractor.FileExtractor;
import br.com.magnus.config.starter.file.extractor.S3FileExtractor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.repository.S3ProjectRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class S3FileExtractorTest {

    private FileExtractor fileExtractor;

    @Mock
    private S3ProjectRepository s3ProjectRepository;

    private static final String clazz = """
                    public class Test {
                        public void test() {
                            System.out.println("Test");
                        }
                    }
            """;

    @BeforeEach
    void setUp() {
        this.fileExtractor = new S3FileExtractor(s3ProjectRepository);
    }

    @Test
    @DisplayName("Should test extract for null param")
    public void shouldTestExtractForNullParam() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> this.fileExtractor.extract(null));

        assertEquals("Project cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test extract for project without class")
    public void shouldTestExtractForProjectWithoutClass() {
        var zip = createZipFile("test.c", "");
        var project = Project.builder()
                .bucket("bucket")
                .id("id")
                .build();
        when(this.s3ProjectRepository.download(project.getBucket(), project.getId()))
                .thenReturn(zip);

        var result = this.fileExtractor.extract(project);

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Should test extract for project with class")
    public void shouldTestExtractForProjectWithClass() throws IOException {
        var zip = createZipFile("test/Test.java", clazz);
        var project = Project.builder()
                .bucket("bucket")
                .id("id")
                .build();
        when(this.s3ProjectRepository.download(project.getBucket(), project.getId()))
                .thenReturn(zip);

        var result = this.fileExtractor.extract(project);

        assertEquals(1, result.size());
        assertEquals("Test.java", result.getFirst().getName());
        assertEquals("test/", result.getFirst().getPath());
    }

    @Test
    @DisplayName("Should test extract with bucket null")
    public void shouldTestExtractWithBucketNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> this.fileExtractor.extract(null, "id"));

        assertEquals("Bucket cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test extract with id null")
    public void shouldTestExtractWithIdNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> this.fileExtractor.extract("bucket", null));

        assertEquals("Id cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test extract")
    public void shouldTestExtract() {
        var zip = createZipFile("test/Test.java", clazz);
        var id = "id";
        var bucket = "bucket";
        when(this.s3ProjectRepository.download(bucket, id))
                .thenReturn(zip);

        var result = this.fileExtractor.extract(bucket, id);

        assertEquals(1, result.size());
        assertEquals("Test.java", result.getFirst().getName());
        assertEquals("test/", result.getFirst().getPath());
    }

    @Test
    @DisplayName("Should test extract with a file with java in the name")
    public void shouldTestExtractWithAFileWithJavaInTheName() {
        var zip = createZipFile("test/Test_java.java", clazz);
        var id = "id";
        var bucket = "bucket";
        when(this.s3ProjectRepository.download(bucket, id))
                .thenReturn(zip);

        var result = this.fileExtractor.extract(bucket, id);

        assertEquals(1, result.size());
        assertEquals("Test_java.java", result.getFirst().getName());
        assertEquals("test/", result.getFirst().getPath());
    }

    @Test
    @DisplayName("Should test extract refactored files with bucket null")
    public void shouldTestExtractRefactoredFilesWithBucketNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> this.fileExtractor.extractRefactoredFiles(null, "id", List.of()));

        assertEquals("Bucket cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test extract refactored files with id null")
    public void shouldTestExtractRefactoredFilesWithIdNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> this.fileExtractor.extractRefactoredFiles("bucket", null, List.of()));

        assertEquals("Id cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test extract refactored files with candidate files null")
    public void shouldTestExtractRefactoredFilesWithCandidateFilesNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> this.fileExtractor.extractRefactoredFiles("bucket", "id", null));

        assertEquals("Candidate files cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test extract refactored files with success")
    void shouldTestExtractedFilesWithSuccess() {
        var candidateFiles = Arrays.asList("File1.java", "File2.java");
        var bucket = "bucket";
        var id = "id";
        var project = createZipFile("File1.java", clazz);
        when(s3ProjectRepository.download(bucket, id)).thenReturn(project);

        var result = fileExtractor.extractRefactoredFiles(bucket, id, candidateFiles);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("File1.java", result.getFirst().getName());
        assertEquals("", result.getFirst().getPath());
    }

    @SneakyThrows
    private InputStream createZipFile(String fileName, String string) {
        var bos = new ByteArrayOutputStream();
        var zos = new ZipOutputStream(bos);
        ZipEntry entry = new ZipEntry(fileName);


        zos.putNextEntry(entry);
        zos.write(string.getBytes());
        zos.closeEntry();

        return new ByteArrayInputStream(bos.toByteArray());
    }

}
