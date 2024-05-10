package br.com.magnus.config.starter.file.compressor;

import br.com.magnus.config.starter.file.JavaFile;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class FileCompressor {

    @SneakyThrows
    public static InputStream compress(List<JavaFile> javaFile) {
        Assert.notNull(javaFile, "JavaFile cannot be null");
        var baos = new ByteArrayOutputStream();
        var zipOut = new ZipOutputStream(baos);
        javaFile.forEach(file -> addFileToZip(zipOut, file));
        zipOut.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private static void addFileToZip(ZipOutputStream zipOut, JavaFile file) {
        try {
            zipOut.putNextEntry(new ZipEntry(file.getFullName()));
            zipOut.write(file.getCompilationUnit().toString().getBytes());
            zipOut.closeEntry();
        } catch (Exception e) {
            log.error("Error creating compressed file {}", file.getName(), e);
            throw new FileCompressorException("Error creating compressed file", e);
        }
    }
}
