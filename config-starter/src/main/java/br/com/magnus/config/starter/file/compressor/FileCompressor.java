package br.com.magnus.config.starter.file.compressor;

import br.com.magnus.config.starter.file.JavaFile;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
public class FileCompressor {

    @SneakyThrows
    public static InputStream compress(List<JavaFile> javaFile) {
        Assert.notNull(javaFile, "JavaFile cannot be null");
        var baos = new ByteArrayOutputStream();
        var zipOut = new ZipOutputStream(baos);
        javaFile.forEach(file -> addRefactoredJavaFileToZip(zipOut, file));
        zipOut.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private static void addFileToZip(ZipOutputStream zipOut, String name, byte[] content) throws IOException {
        zipOut.putNextEntry(new ZipEntry(name));
        zipOut.write(content);
        zipOut.closeEntry();
    }

    private static void addRefactoredJavaFileToZip(ZipOutputStream zipOut, JavaFile file) {
        try {
            addFileToZip(zipOut, file.getFullName(), file.getCompilationUnit().toString().getBytes());
        } catch (Exception e) {
            log.error("Error creating compressed file {}", file.getName(), e);
            throw new FileCompressorException("Error creating compressed file", e);
        }
    }

    @SneakyThrows
    public static InputStream replaceFiles(InputStream project, Map<String, JavaFile> candidates) {
        Assert.notNull(project, "Project cannot be null");
        Assert.notNull(candidates, "Candidates cannot be null");

        ZipEntry zipEntry;
        var zipInputStream = new ZipInputStream(project);

        var baos = new ByteArrayOutputStream();
        var zipOut = new ZipOutputStream(baos);
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            Optional.of(zipEntry)
                    .filter(f -> candidates.get(f.getName()) == null)
                    .ifPresent(f -> addZipEntry(zipOut, zipInputStream, f));
        }
        candidates.values().forEach(f -> addOriginalJavaFileToZip(zipOut, f));
        zipOut.close();

        return new ByteArrayInputStream(baos.toByteArray());
    }


    private static void addOriginalJavaFileToZip(ZipOutputStream zipOut, JavaFile file) {
        try {
            addFileToZip(zipOut, file.getFullName(), file.getOriginalClass().getBytes());
        } catch (Exception e) {
            log.error("Error creating compressed file {}", file.getName(), e);
            throw new FileCompressorException("Error creating compressed file", e);
        }
    }

    @SneakyThrows
    private static void addZipEntry(ZipOutputStream zipOut, ZipInputStream zipInputStream, ZipEntry zipEntry) {
        addFileToZip(zipOut, zipEntry.getName(), zipInputStream.readAllBytes());
    }

}
