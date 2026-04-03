package br.com.magnus.metricscalculator.consumer;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;
import br.com.magnus.config.starter.projects.ChangedFilesAnalyzer;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.metricscalculator.qualityAttributes.QualityAttributesProcessor;
import br.com.magnus.metricscalculator.repository.ExtractProjects;
import br.com.magnus.metricscalculator.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsProcessor {

    private final ExtractProjects extractProjects;
    private final QualityAttributesProcessor processor;
    private final ProjectRepository projectRepository;

    public void process(String id) {
        log.info("Project consumed id: {}", id);
        var project = projectRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        var bucket = project.getBucket();
        var originalFiles = extractProjects.loadProjectFiles(id, bucket);
        var originalPath = extractProjects.extractProject(originalFiles);

        log.info("Extracting quality attributes extracted");
        project.getCandidatesInformation().forEach(candidate -> {
            var candidateFiles = extractProjects.loadProjectFiles(candidate.getId(), bucket);
            var candidatePath = extractProjects.extractProject(candidateFiles);
            candidate.setMetrics(processor.extract(originalPath, candidatePath));
            candidate.setFileMetricsByFile(extractFileMetrics(candidate.getFilesChanged(), originalFiles, candidateFiles));
            log.info("Candidates information: {}", candidate);
        });

        project.addStatus(ProjectStatus.FINISHED);
        project.setUpdatedAt(System.nanoTime());
        projectRepository.save(project);
    }

    private Map<String, List<QualityAttributeResult>> extractFileMetrics(Set<String> changedFiles,
                                                                         List<JavaFile> originalFiles,
                                                                         List<JavaFile> candidateFiles) {
        var analysis = ChangedFilesAnalyzer.analyze(originalFiles, changedFiles);
        var originalFilesByName = originalFiles.stream()
                .collect(Collectors.toMap(JavaFile::getFullName, file -> file, (left, right) -> left, LinkedHashMap::new));
        var candidateFilesByName = candidateFiles.stream()
                .collect(Collectors.toMap(JavaFile::getFullName, file -> file, (left, right) -> left, LinkedHashMap::new));
        var metricsByFile = new LinkedHashMap<String, List<QualityAttributeResult>>();
        var metricsByGroup = new LinkedHashMap<String, List<QualityAttributeResult>>();

        Optional.ofNullable(changedFiles).orElseGet(Set::of).forEach(file -> {
            var groupFiles = analysis.groupedFiles(file);
            var groupKey = String.join("|", groupFiles);
            var metrics = metricsByGroup.computeIfAbsent(groupKey, ignored -> {
                var originalGroupFiles = collectGroupFiles(groupFiles, originalFilesByName);
                var candidateGroupFiles = collectGroupFiles(groupFiles, candidateFilesByName);
                return new java.util.ArrayList<>(processor.extract(extractProjects.extractProject(originalGroupFiles),
                        extractProjects.extractProject(candidateGroupFiles)));
            });
            metricsByFile.put(file, new java.util.ArrayList<>(metrics));
        });

        return metricsByFile;
    }

    private List<JavaFile> collectGroupFiles(List<String> groupFiles, Map<String, JavaFile> filesByName) {
        return groupFiles.stream()
                .map(filesByName::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
