package br.com.detection.detectionagent.refactor;

import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.repository.S3ProjectRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ExtractFilesTest {

    private ExtractFiles extractFiles;

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
        this.extractFiles = new ExtractFiles(s3ProjectRepository);
    }

    @Test
    @DisplayName("Should test extract for null param")
    public void shouldTestExtractForNullParam() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> this.extractFiles.extract(null));

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

        var result = this.extractFiles.extract(project);

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

        var result = this.extractFiles.extract(project);

        assertEquals(1, result.size());
        assertEquals("Test.java", result.getFirst().getName());
        assertEquals("test/", result.getFirst().getPath());
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