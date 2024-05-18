package br.com.intermediary.intermediaryagent.controller;

import br.com.intermediary.intermediaryagent.refactor.RefactorProject;
import br.com.magnus.config.starter.projects.Project;
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
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class IntermediaryHtmxController {

    private final RefactorProject refactorProject;

    @PostMapping(path = "/upload")
    public String registration(Map<String, Object> model, @NotNull @RequestParam("file") MultipartFile file) throws IOException {
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

        model.put("url", "/project/" + id);
        return "evaluation";
    }

    @SneakyThrows
    @GetMapping(path = "/project/{id}")
    public String getProject(Map<String, Object> model, @PathVariable String id) {
        var project = refactorProject.retrieveRetry(id, 0);
        model.put("status", project.status());
        model.put("candidates", project.candidatesInformation());

        return "candidates";
    }
}
