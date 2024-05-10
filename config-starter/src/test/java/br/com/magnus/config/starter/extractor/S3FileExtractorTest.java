package br.com.magnus.config.starter.extractor;

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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
