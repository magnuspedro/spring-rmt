package br.com.magnus.projectsyncbff.refactor;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.file.extractor.FileExtractor;
import br.com.magnus.config.starter.projects.BaseProject;
import br.com.magnus.config.starter.projects.CandidateInformation;
import br.com.magnus.config.starter.projects.ChangedFilesAnalyzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectSelectionPlanner {

    private static final String KEY_SEPARATOR = "::";

    private final FileExtractor fileExtractor;

    public ProjectSelection plan(BaseProject project, List<String> requestedFileKeys) {
        var candidates = Optional.ofNullable(project.getCandidatesInformation()).orElseGet(List::of);
        if (candidates.isEmpty()) {
            return ProjectSelection.builder()
                    .candidates(List.of())
                    .requestedFileKeys(List.of())
                    .selectedFileKeys(List.of())
                    .selectableFileCount(0)
                    .blockedCount(0)
                    .downloadable(false)
                    .build();
        }

        var analysis = analyze(project, candidates);
        var validKeys = analysis.groupByFileKey().keySet();
        var requested = Optional.ofNullable(requestedFileKeys)
                .orElseGet(List::of)
                .stream()
                .filter(validKeys::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var selected = expand(requested, analysis.groupByFileKey());

        var candidateSelections = candidates.stream()
                .map(candidate -> {
                    var fileSelections = filesChanged(candidate).stream()
                            .map(file -> {
                                var key = fileKey(candidate.getId(), file);
                                return FileSelection.builder()
                                        .key(key)
                                        .file(file)
                                        .dependencyFiles(analysis.relatedFilesByKey().getOrDefault(key, List.of()))
                                        .blockingReasons(List.of())
                                        .metrics(candidate.getFileMetrics(file))
                                        .requested(requested.contains(key))
                                        .selected(selected.contains(key))
                                        .locked(selected.contains(key) && !requested.contains(key))
                                        .blocked(false)
                                        .build();
                            })
                            .toList();
                    return CandidateSelection.builder()
                            .candidate(candidate)
                            .files(fileSelections)
                            .selected(fileSelections.stream().anyMatch(FileSelection::isSelected))
                            .blocked(false)
                            .build();
                })
                .toList();

        return ProjectSelection.builder()
                .candidates(candidateSelections)
                .requestedFileKeys(List.copyOf(requested))
                .selectedFileKeys(List.copyOf(selected))
                .selectableFileCount(validKeys.size())
                .blockedCount(0)
                .downloadable(!selected.isEmpty())
                .build();
    }

    public void validateSelection(BaseProject project, List<String> selectedFileKeys) {
        var candidates = Optional.ofNullable(project.getCandidatesInformation()).orElseGet(List::of);
        var analysis = analyze(project, candidates);
        var validKeys = analysis.groupByFileKey().keySet();
        var selected = Optional.ofNullable(selectedFileKeys)
                .orElseGet(List::of)
                .stream()
                .filter(validKeys::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (selected.isEmpty()) {
            throw new SelectionValidationException("Select at least one file before downloading the refactored project.", List.of());
        }

        var closure = expand(selected, analysis.groupByFileKey());
        if (!closure.equals(selected)) {
            var missing = closure.stream()
                    .filter(key -> !selected.contains(key))
                    .toList();
            throw new SelectionValidationException("Some dependent files are missing from the selection.", missing);
        }
    }

    public Map<String, List<String>> groupSelectedFilesByCandidate(List<String> selectedFileKeys) {
        return Optional.ofNullable(selectedFileKeys)
                .orElseGet(List::of)
                .stream()
                .map(ProjectSelectionPlanner::parseFileKey)
                .collect(Collectors.groupingBy(FileKey::candidateId,
                        LinkedHashMap::new,
                        Collectors.mapping(FileKey::file, Collectors.toList())));
    }

    private SelectionAnalysis analyze(BaseProject project, List<CandidateInformation> candidates) {
        final List<JavaFile> sourceFiles;
        try {
            sourceFiles = fileExtractor.extract(project);
        } catch (Exception exception) {
            log.warn("Skipping dependency analysis for project {} because the original sources could not be extracted", project.getId(), exception);
            return emptyAnalysis(candidates);
        }

        var relatedFilesByKey = new LinkedHashMap<String, List<String>>();
        var groupByFileKey = new LinkedHashMap<String, Set<String>>();

        candidates.forEach(candidate -> {
            var candidateFiles = filesChanged(candidate);
            var candidateAnalysis = ChangedFilesAnalyzer.analyze(sourceFiles, candidateFiles);
            candidateFiles.forEach(file -> {
                var key = fileKey(candidate.getId(), file);
                relatedFilesByKey.put(key, candidateAnalysis.relatedFiles(file));
                groupByFileKey.put(key, candidateAnalysis.groupedFiles(file).stream()
                        .filter(groupedFile -> !groupedFile.equals(file))
                        .map(groupedFile -> fileKey(candidate.getId(), groupedFile))
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
            });
        });

        return new SelectionAnalysis(relatedFilesByKey, groupByFileKey);
    }

    private SelectionAnalysis emptyAnalysis(List<CandidateInformation> candidates) {
        var relatedFilesByKey = new LinkedHashMap<String, List<String>>();
        var groupByFileKey = new LinkedHashMap<String, Set<String>>();
        candidates.forEach(candidate -> filesChanged(candidate).forEach(file -> {
            var key = fileKey(candidate.getId(), file);
            relatedFilesByKey.put(key, List.of());
            groupByFileKey.put(key, new LinkedHashSet<>());
        }));
        return new SelectionAnalysis(relatedFilesByKey, groupByFileKey);
    }

    private static LinkedHashSet<String> expand(Set<String> requested, Map<String, Set<String>> groupedFilesByKey) {
        var expanded = new LinkedHashSet<String>();
        var queue = new ArrayDeque<>(requested);
        while (!queue.isEmpty()) {
            var key = queue.removeFirst();
            if (!expanded.add(key)) {
                continue;
            }
            groupedFilesByKey.getOrDefault(key, Set.of()).forEach(queue::addLast);
        }
        return expanded;
    }

    private static Set<String> filesChanged(CandidateInformation candidate) {
        return Optional.ofNullable(candidate.getFilesChanged()).orElseGet(Set::of);
    }

    public static String fileKey(String candidateId, String file) {
        return candidateId + KEY_SEPARATOR + file;
    }

    private static FileKey parseFileKey(String key) {
        var split = key.split(KEY_SEPARATOR, 2);
        return new FileKey(split[0], split.length > 1 ? split[1] : "");
    }

    private record SelectionAnalysis(Map<String, List<String>> relatedFilesByKey,
                                     Map<String, Set<String>> groupByFileKey) {
    }

    private record FileKey(String candidateId, String file) {
    }
}
