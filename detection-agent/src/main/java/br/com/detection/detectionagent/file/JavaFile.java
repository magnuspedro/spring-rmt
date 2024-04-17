package br.com.detection.detectionagent.file;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class JavaFile {
    private final String name;
    private final String path;
    private final String originalClass;
    private Object parsed;
}
