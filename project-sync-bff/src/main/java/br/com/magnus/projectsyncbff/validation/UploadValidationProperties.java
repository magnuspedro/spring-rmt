package br.com.magnus.projectsyncbff.validation;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "upload.validation")
public class UploadValidationProperties {
    private int maxZipEntries = 500;
    private long maxEntrySize = 10 * 1024 * 1024;
    private List<String> allowedExtensions = List.of(".java");
    private List<String> allowedContentTypes = List.of("application/zip", "application/x-zip-compressed", "application/octet-stream");
    private List<String> textBasedExtensions;
    private List<String> suspiciousPatterns = List.of(
            "Runtime.getRuntime().exec",
            "ProcessBuilder",
            "System.exit",
            "ScriptEngine"
    );
}
