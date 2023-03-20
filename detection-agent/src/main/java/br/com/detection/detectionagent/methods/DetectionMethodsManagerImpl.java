package br.com.detection.detectionagent.methods;

import br.com.detection.detectionagent.methods.dataExtractions.forks.DataExtractionFork;
import br.com.messages.files.FileRepositoryCollections;
import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.projects.Project;
import br.com.messages.projects.ProjectsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DetectionMethodsManagerImpl implements DetectionMethodsManager {

    private final ProjectsRepository projectsRepository;

    private final List<DataExtractionFork> dataExtractionForks;

    private final Map<String, List<RefactoringCandidate>> projectsCandidates = new HashMap<>();

    @Override
    public List<RefactoringCandidate> extractCandidates(String projectId) {

        final Project project = this.projectsRepository.get(FileRepositoryCollections.PROJECTS, projectId)
                .orElseThrow(IllegalArgumentException::new);

        if (!this.projectsCandidates.containsKey(project.getId())) {
            this.projectsCandidates.put(project.getId(), new ArrayList<>());
        }
        this.projectsCandidates.get(project.getId()).clear();
        this.projectsCandidates.get(project.getId()).addAll(this.dataExtractionForks.stream()
                .flatMap(f -> f.findCandidates(project).stream()).toList());

        return this.projectsCandidates.get(project.getId());
    }

    @Override
    public String refactor(String projectId, Collection<RefactoringCandidadeDTO> eligiblePatterns) {

        Project project = this.projectsRepository.get(FileRepositoryCollections.PROJECTS, projectId)
                .orElseThrow(IllegalArgumentException::new);

        Optional.ofNullable(this.projectsCandidates.get(projectId)).orElseThrow(IllegalStateException::new);

        for (RefactoringCandidate candidate : eligiblePatterns.stream()
                .filter(dto -> this.isCandidateProcessed(projectId, dto))
                .map(dto -> this.parseCandidateDTO(projectId, dto).get()).toList()) {

            final Optional<DataExtractionFork> fork = this.dataExtractionForks.stream()
                    .filter(f -> f.belongsTo(candidate.getReference())).findFirst();

            fork.ifPresent(dataExtractionFork -> dataExtractionFork.refactor(project, candidate));

            return this.projectsRepository
                    .get(FileRepositoryCollections.REFACTORED_PROJECTS, projectId)
                    .orElseThrow(IllegalArgumentException::new).getId();
        }

        throw new IllegalArgumentException();
    }

    private boolean isCandidateProcessed(String projectId, RefactoringCandidadeDTO rc) {
        final boolean processed = this.projectsCandidates.get(projectId).stream()
                .anyMatch(c -> c.getId().equals(rc.getId()));

        return processed;
    }

    private Optional<RefactoringCandidate> parseCandidateDTO(String projectId, RefactoringCandidadeDTO dto) {
        final Optional<RefactoringCandidate> candidate = this.projectsCandidates.get(projectId).stream()
                .filter(rc -> dto.getId().equals(rc.getId())).findFirst();

        return candidate;
    }

    @Override
    public List<Reference> getReferences() {
        return this.dataExtractionForks.stream().map(DataExtractionFork::getReferences).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

}
