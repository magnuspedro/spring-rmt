package br.com.magnus.config.starter.file.compressor;

import br.com.magnus.config.starter.file.JavaFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileCompressor {

    public static InputStream compress(List<JavaFile> javaFile) {
        var baos = new ByteArrayOutputStream();
        var zipOut = new ZipOutputStream(baos);

        javaFile.forEach(file -> addFileToZip(zipOut, file));

        return new ByteArrayInputStream(baos.toByteArray());
    }

    private static void addFileToZip(ZipOutputStream zipOut, JavaFile file) {
        try {
            zipOut.putNextEntry(new ZipEntry(file.getFullName()));
            zipOut.write(file.getCompilationUnit().toString().getBytes());
            zipOut.closeEntry();
        } catch (Exception e) {
            throw new FileCompressorException("Error creating file", e);
        }
    }
}
