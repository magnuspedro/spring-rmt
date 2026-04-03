package br.com.magnus.projectsyncbff.controller;

import br.com.magnus.config.starter.projects.BaseProject;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.projects.ProjectStatus;
import br.com.magnus.projectsyncbff.refactor.RefactorProject;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HtmxController {

    private final RefactorProject refactorProject;

    @GetMapping(path = "/")
    public String index() {
        return "index";
    }

    @SneakyThrows
    @PostMapping(path = "/upload")
    public String registration(Map<String, Object> model, @NotNull @RequestParam("file") MultipartFile file) throws IOException {
        var hash = MessageDigest.getInstance("SHA-256").digest(file.getBytes());
        // var id = new BigInteger(1, hash).toString(16);
        var id = UUID.randomUUID().toString();
        log.info("Receiving project original name: {},id: {}, size: {}", file.getOriginalFilename(), id, file.getSize());

        var project = Project.builder()
                .baseProject(BaseProject.builder()
                        .id(id)
                        .name(file.getOriginalFilename())
                        .createdAt(System.nanoTime())
                        .build())
                .size(file.getSize())
                .contentType(file.getContentType())
                .zipContent(IoUtils.toByteArray(file.getInputStream()))
                .build();

        refactorProject.process(project);

        model.put("projectId", id);
        model.put("url", "/project/" + id);
        model.put("status", ProjectStatus.EVALUATING_CANDIDATES);
        return "evaluation";
    }

    @SneakyThrows
    @GetMapping(path = "/project/{id}")
    public String getProject(Map<String, Object> model, @PathVariable String id) {
        var project = refactorProject.retrieve(id);
        if (!isTerminal(project.status())) {
            model.put("projectId", id);
            model.put("url", "/project/" + id);
            model.put("status", project.status());
            return "evaluation";
        }
        model.put("projectId", id);
        model.put("project", project);
        model.put("selectionUrl", "/project/" + id + "/selection");
        model.put("downloadUrl", "/project/" + id + "/download");
        return "candidates";
    }

    @PostMapping(path = "/project/{id}/selection")
    public String updateSelection(Map<String, Object> model,
                                  @PathVariable String id,
                                  @RequestParam(name = "requestedFileKey", required = false) List<String> requestedFileKeys) {
        var project = refactorProject.retrieve(id, requestedFileKeys == null ? Collections.emptyList() : requestedFileKeys);
        model.put("projectId", id);
        model.put("project", project);
        model.put("selectionUrl", "/project/" + id + "/selection");
        model.put("downloadUrl", "/project/" + id + "/download");
        return "candidates";
    }

    @PostMapping(path = "/project/{id}/download")
    public String downloadProject(Map<String, Object> model,
                                  @PathVariable String id,
                                  @RequestParam(name = "selectedFileKey", required = false) List<String> selectedFileKeys) {
        log.info("Downloading project id: {}, files: {}", id, selectedFileKeys);
        var url = refactorProject.downloadProject(id, selectedFileKeys == null ? List.of() : selectedFileKeys);
        model.put("url", url);
        return "link";
    }

    private boolean isTerminal(ProjectStatus status) {
        return status == ProjectStatus.FINISHED || status == ProjectStatus.NO_CANDIDATES;
    }
}
