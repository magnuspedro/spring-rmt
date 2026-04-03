package br.com.magnus.projectsyncbff.validation;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UploadValidatorTest {

    private UploadValidator uploadValidator;

    @BeforeEach
    void setUp() {
        var properties = new UploadValidationProperties();
        properties.setMaxZipEntries(2);
        properties.setMaxEntrySize(1024);
        properties.setAllowedExtensions(List.of(".java"));
        properties.setAllowedContentTypes(List.of("application/zip"));
        properties.setSuspiciousPatterns(List.of("Runtime.getRuntime().exec"));
        uploadValidator = new UploadValidator(properties);
    }

    @Test
    @DisplayName("Should accept a valid Java zip upload")
    void shouldAcceptValidZipUpload() {
        var file = new MockMultipartFile("file", "project.zip", "application/zip",
                zipFile(List.of(new ZipContent("src/main/java/example/Hello.java", "class Hello {}"))));

        assertDoesNotThrow(() -> uploadValidator.validate(file));
    }

    @Test
    @DisplayName("Should reject uploads with invalid extension")
    void shouldRejectInvalidExtension() {
        var file = new MockMultipartFile("file", "project.txt", "application/zip",
                zipFile(List.of(new ZipContent("src/main/java/example/Hello.java", "class Hello {}"))));

        var exception = assertThrows(UploadValidationException.class, () -> uploadValidator.validate(file));

        assertThat(exception.getErrors()).contains("File extension must be .zip");
    }

    @Test
    @DisplayName("Should reject zip slip entries")
    void shouldRejectZipSlipEntries() {
        var file = new MockMultipartFile("file", "project.zip", "application/zip",
                zipFile(List.of(new ZipContent("../Hello.java", "class Hello {}"))));

        var exception = assertThrows(UploadValidationException.class, () -> uploadValidator.validate(file));

        assertThat(exception.getErrors()).anyMatch(error -> error.contains("invalid path"));
    }

    @Test
    @DisplayName("Should reject suspicious Java content")
    void shouldRejectSuspiciousJavaContent() {
        var file = new MockMultipartFile("file", "project.zip", "application/zip",
                zipFile(List.of(new ZipContent("src/main/java/example/Hello.java", "Runtime.getRuntime().exec(\"ls\");"))));

        var exception = assertThrows(UploadValidationException.class, () -> uploadValidator.validate(file));

        assertThat(exception.getErrors()).anyMatch(error -> error.contains("suspicious pattern"));
    }

    @Test
    @DisplayName("Should reject archives with too many entries")
    void shouldRejectTooManyEntries() {
        var file = new MockMultipartFile("file", "project.zip", "application/zip",
                zipFile(List.of(
                        new ZipContent("A.java", "class A {}"),
                        new ZipContent("B.java", "class B {}"),
                        new ZipContent("C.java", "class C {}")
                )));

        var exception = assertThrows(UploadValidationException.class, () -> uploadValidator.validate(file));

        assertThat(exception.getErrors()).contains("Zip contains more than 2 entries");
    }

    @Test
    @DisplayName("Should reject non Java entries")
    void shouldRejectNonJavaEntries() {
        var file = new MockMultipartFile("file", "project.zip", "application/zip",
                zipFile(List.of(new ZipContent("README.md", "# Test"))));

        var exception = assertThrows(UploadValidationException.class, () -> uploadValidator.validate(file));

        assertThat(exception.getErrors()).anyMatch(error -> error.contains("allowed source file"));
    }

    @SneakyThrows
    private byte[] zipFile(List<ZipContent> entries) {
        var outputStream = new ByteArrayOutputStream();
        try (var zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
            for (var entry : entries) {
                zipOutputStream.putNextEntry(new ZipEntry(entry.name()));
                zipOutputStream.write(entry.content().getBytes(StandardCharsets.UTF_8));
                zipOutputStream.closeEntry();
            }
        }
        return outputStream.toByteArray();
    }

    private record ZipContent(String name, String content) {
    }
}
