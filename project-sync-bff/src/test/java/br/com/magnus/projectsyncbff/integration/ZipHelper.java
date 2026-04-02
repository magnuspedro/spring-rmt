package br.com.magnus.projectsyncbff.integration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility for creating in-memory ZIP files from string content.
 * Used in integration tests to create test project fixtures.
 */
public class ZipHelper {

    private ZipHelper() {
    }

    /**
     * Creates an in-memory ZIP file from a map of file paths to content.
     *
     * @param files map of zip entry path (e.g., "src/Main.java") to file content
     * @return byte array containing the ZIP file
     */
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
