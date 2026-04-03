package br.com.magnus.projectsyncbff.refactor;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FileSelection {
    private final String key;
    private final String file;
    private final List<String> dependencyFiles;
    private final List<String> blockingReasons;
    private final boolean requested;
    private final boolean selected;
    private final boolean locked;
    private final boolean blocked;
}
