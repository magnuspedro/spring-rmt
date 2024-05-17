package br.com.intermediary.intermediaryagent.controller;

import br.com.intermediary.intermediaryagent.refactor.ProjectResults;
import br.com.intermediary.intermediaryagent.refactor.RefactorProject;
import br.com.intermediary.intermediaryagent.repository.ProjectRepository;
import br.com.magnus.config.starter.projects.Project;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

@Valid
@Slf4j
@RestController
@RequestMapping("/rmt/api/v1")
@RequiredArgsConstructor
public class IntermediaryController implements Serializable {

    private final RefactorProject refactorProject;
    private final ProjectRepository projectRepository;

    @PostMapping(path = "/upload")
    public String registration(@NotNull @RequestParam("file") MultipartFile file) throws IOException {
        var id = UUID.randomUUID().toString();
        log.info("Receiving project original name: {},id: {}, size: {}", file.getOriginalFilename(), id, file.getSize());

        var project = Project.builder()
                .id(id)
                .size(file.getSize())
                .name(file.getOriginalFilename())
                .contentType(file.getContentType())
                .zipContent(IoUtils.toByteArray(file.getInputStream()))
                .build();

        refactorProject.process(project);

        return id;
    }

    @GetMapping(path = "/project/{id}")
    public ResponseEntity<ProjectResults> getProject(@PathVariable String id) {
        return refactorProject.retrieve(id);
    }
}
