package br.com.magnus.metricscalculator.integration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
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
}
