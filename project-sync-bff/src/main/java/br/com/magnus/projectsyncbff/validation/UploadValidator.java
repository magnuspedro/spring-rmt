package br.com.magnus.projectsyncbff.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@RequiredArgsConstructor
public class UploadValidator {
    private static final int BUFFER_SIZE = 8192;

    private final UploadValidationProperties properties;

    public void validate(MultipartFile file) {
        Assert.notNull(file, "File cannot be null");

        var errors = new ArrayList<String>();
        validateFileMetadata(file, errors);

        if (errors.isEmpty()) {
            validateZipContent(file, errors);
        }

        if (!errors.isEmpty()) {
            throw new UploadValidationException(errors);
        }
    }

    private void validateFileMetadata(MultipartFile file, ArrayList<String> errors) {
        var originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        if (!originalFilename.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            errors.add("File extension must be .zip");
        }

        var contentType = file.getContentType();
        if (contentType == null || properties.getAllowedContentTypes().stream().noneMatch(contentType::equalsIgnoreCase)) {
            errors.add("Content type must be one of " + properties.getAllowedContentTypes());
        }
    }

    private void validateZipContent(MultipartFile file, ArrayList<String> errors) {
        try (var zipInputStream = new ZipInputStream(new BufferedInputStream(file.getInputStream()))) {
            var entryCount = 0;
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                entryCount++;
                if (entryCount > properties.getMaxZipEntries()) {
                    errors.add("Zip contains more than " + properties.getMaxZipEntries() + " entries");
                    return;
                }

                validateEntry(zipEntry, zipInputStream, errors);
                zipInputStream.closeEntry();
                if (!errors.isEmpty()) {
                    return;
                }
            }
        } catch (IOException exception) {
            errors.add("Invalid zip file");
        }
    }

    private void validateEntry(ZipEntry zipEntry, ZipInputStream zipInputStream, ArrayList<String> errors) throws IOException {
        var normalizedPath = Path.of(zipEntry.getName()).normalize();
        if (normalizedPath.isAbsolute() || normalizedPath.startsWith("..")) {
            errors.add("Zip entry contains an invalid path: " + zipEntry.getName());
            return;
        }

        if (zipEntry.isDirectory()) {
            return;
        }

        var content = readEntry(zipEntry, zipInputStream);
        if (content == null) {
            errors.add("Zip entry exceeds the maximum allowed size: " + zipEntry.getName());
            return;
        }

        // Only check for suspicious patterns in text-based files
        var fileName = zipEntry.getName().toLowerCase();
        if (properties.getTextBasedExtensions().stream().anyMatch(fileName::endsWith)) {
            var source = content.toString(StandardCharsets.UTF_8);
            properties.getSuspiciousPatterns().stream()
                    .filter(source::contains)
                    .findFirst()
                    .ifPresent(pattern -> errors.add("Zip entry contains a suspicious pattern (" + pattern + "): " + zipEntry.getName()));
        }
    }

    private ByteArrayOutputStream readEntry(ZipEntry zipEntry, ZipInputStream zipInputStream) throws IOException {
        var outputStream = new ByteArrayOutputStream();
        var buffer = new byte[BUFFER_SIZE];
        long entrySize = 0;
        int read;
        while ((read = zipInputStream.read(buffer)) != -1) {
            entrySize += read;
            if (entrySize > properties.getMaxEntrySize()) {
                return null;
            }
            outputStream.write(buffer, 0, read);
        }
        return outputStream;
    }
}
