package br.com.magnus.projectsyncbff.controller;

import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.projectsyncbff.refactor.ProjectResults;
import br.com.magnus.projectsyncbff.refactor.RefactorProject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

@Valid
@Slf4j
@RestController
@RequestMapping("/rmt/api/v1")
@RequiredArgsConstructor
public class RestfulController implements Serializable {

    private final RefactorProject refactorProject;

    @SneakyThrows
    @PostMapping(path = "/upload")
    public String registration(@NotNull @RequestParam("file") MultipartFile file) throws IOException {
        var hash = MessageDigest.getInstance("SHA-256").digest(file.getBytes());
        var id = new BigInteger(1, hash).toString(16);
        log.info("Receiving project from front end original name: {},id: {}, size: {}", file.getOriginalFilename(), id, file.getSize());

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
        var project = refactorProject.retrieve(id);
        return ResponseEntity
                .status(200)
                .body(project);
    }

    @PostMapping(path = "/project/{id}/download")
    public String downloadProject(@PathVariable String id, @RequestBody List<String> candidatesIds) {
        log.info("Downloading project id: {}, candidates: {}", id, candidatesIds);
        return refactorProject.downloadProject(id, candidatesIds);
    }
}
