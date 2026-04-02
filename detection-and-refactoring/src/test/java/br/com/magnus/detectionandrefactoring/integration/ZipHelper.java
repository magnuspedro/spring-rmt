package br.com.magnus.detectionandrefactoring.integration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipHelper {

    private ZipHelper() {
    }

    public static byte[] createZip(Map<String, String> files) {
        var byteArrayOutputStream = new ByteArrayOutputStream();

        try (var zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (var entry : files.entrySet()) {
                var zipEntry = new ZipEntry(entry.getKey());
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(entry.getValue().getBytes());
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create ZIP file", e);
        }

        return byteArrayOutputStream.toByteArray();
    }

    public static Map<String, String> extractZip(InputStream inputStream) throws IOException {
        try (var zipInputStream = new ZipInputStream(inputStream)) {
            var content = new LinkedHashMap<String, String>();
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    var bytes = new ByteArrayOutputStream();
                    zipInputStream.transferTo(bytes);
                    content.put(entry.getName(), bytes.toString());
                }
            }
            return content;
        }
    }

    public static Map<String, String> extractZip(byte[] zipBytes) throws IOException {
        return extractZip(new ByteArrayInputStream(zipBytes));
    }
}
