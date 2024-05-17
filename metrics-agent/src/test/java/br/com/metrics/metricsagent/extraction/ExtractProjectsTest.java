package br.com.metrics.metricsagent.extraction;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.file.extractor.FileExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtractProjectsTest {

    @Mock
    private FileExtractor fileExtractor;

    private ExtractProjects extractProjects;

    @BeforeEach
    void setUp() {
        extractProjects = new ExtractProjects(fileExtractor);
    }

    @Test
    @DisplayName("Project extraction is successful")
    void shouldExtractProjectSuccessfully() {
        when(fileExtractor.extract(anyString(), anyString()))
                .thenReturn(Collections.singletonList(JavaFile.builder()
                        .name("Test.java")
                        .originalClass("public class Test {}")
                        .path("test/")
                        .build()));

        Path path = extractProjects.extractProject("id", "bucket");

        assertTrue(path.toFile().exists());
    }

    @Test
    @DisplayName("No files are extracted")
    void shouldHandleNoFilesExtracted() {
        when(fileExtractor.extract(anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        Path path = extractProjects.extractProject("id", "bucket");

        assertTrue(path.toFile().exists());
        assertEquals(0, path.toFile().list().length);
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when id is null")
    void shouldThrowExceptionWhenIdIsNull() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            extractProjects.extractProject(null, "bucket");
        });

        assertEquals("Id cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when bucket is null")
    void shouldThrowExceptionWhenBucketIsNull() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            extractProjects.extractProject("id", null);
        });

        assertEquals("Bucket cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when JavaFile name is null")
    void shouldThrowExceptionWhenJavaFileNameIsNull() {
        when(fileExtractor.extract(anyString(), anyString()))
                .thenReturn(Collections.singletonList(JavaFile.builder()
                        .name(null)
                        .originalClass(null)
                        .path("test/")
                        .build()));

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> extractProjects.extractProject("id", "bucket"));

        assertEquals("Name cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when JavaFile original class is null")
    void shouldThrowExceptionWhenJavaFileOriginalClassIsNull() {
        when(fileExtractor.extract(anyString(), anyString()))
                .thenReturn(Collections.singletonList(JavaFile.builder()
                        .name("Test.java")
                        .originalClass(null)
                        .path("test/")
                        .build()));

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> extractProjects.extractProject("id", "bucket"));

        assertEquals("Original class cannot be null", exception.getMessage());
    }
}