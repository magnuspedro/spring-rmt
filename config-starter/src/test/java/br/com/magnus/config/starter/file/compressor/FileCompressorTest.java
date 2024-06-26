package br.com.magnus.config.starter.file.compressor;


import br.com.magnus.config.starter.file.JavaFile;
import com.github.javaparser.ast.CompilationUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FileCompressorTest {

    @Test
    void compressSingleFile() throws IOException {
        var javaFile = JavaFile.builder()
                .name("Test")
                .parsed(new CompilationUnit())
                .build();

        var result = FileCompressor.compress(Collections.singletonList(javaFile));

        assertNotNull(result);
        assertThat(result.readAllBytes().length).isGreaterThan(22);
    }

    @Test
    void compressMultipleFiles() throws IOException {
        var javaFile = JavaFile.builder()
                .name("Test")
                .parsed(new CompilationUnit())
                .build();

        var javaFile2 = JavaFile.builder()
                .name("Test2")
                .parsed(new CompilationUnit())
                .build();


        var result = FileCompressor.compress(Arrays.asList(javaFile, javaFile2));

        assertNotNull(result);
        assertThat(result.readAllBytes().length).isGreaterThan(22);
    }

    @Test
    void compressEmptyList() throws IOException {
        var result = FileCompressor.compress(Collections.emptyList());

        assertEquals(22, result.readAllBytes().length);
    }

    @Test
    void compressNullList() {
        var result = assertThrows(IllegalArgumentException.class, () -> FileCompressor.compress(null));

        assertEquals("JavaFile cannot be null", result.getMessage());
    }

    @Test
    void replaceFiles() {
        var javaFile1 = JavaFile.builder()
                .name("Test")
                .originalClass("Test")
                .build();
        var javaFile2 = JavaFile.builder()
                .name("Test2")
                .originalClass("Test2")
                .build();
        Map<String, JavaFile> candidates = new HashMap<>();
        candidates.put(javaFile1.getFullName(), javaFile1);
        candidates.put(javaFile2.getFullName(), javaFile2);

        InputStream project = new ByteArrayInputStream("Test project content".getBytes());
        InputStream result = FileCompressor.replaceFiles(project, candidates);

        assertNotNull(result);
    }

    @Test
    void replaceFileWithNullProject() {
        var javaFile1 = JavaFile.builder()
                .name("Test")
                .build();
        Map<String, JavaFile> candidates = new HashMap<>();
        candidates.put(javaFile1.getFullName(), javaFile1);
        InputStream project = null;

        var result = assertThrows(IllegalArgumentException.class, () -> FileCompressor.replaceFiles(project, candidates));

        assertEquals("Project cannot be null", result.getMessage());
    }

    @Test
    void replaceFileWithNullCandidates() {
        InputStream project = new ByteArrayInputStream("Test project content".getBytes());

        var result = assertThrows(IllegalArgumentException.class, () -> FileCompressor.replaceFiles(project, null));

        assertEquals("Candidates cannot be null", result.getMessage());
    }
}