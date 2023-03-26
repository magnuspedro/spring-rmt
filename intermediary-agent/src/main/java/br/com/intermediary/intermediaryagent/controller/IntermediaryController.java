package br.com.intermediary.intermediaryagent.controller;

import br.com.intermediary.intermediaryagent.refactor.RefactorProject;
import br.com.messages.members.api.intermediary.IntermediaryAgentCoreApi;
import br.com.messages.members.api.intermediary.IntermediaryAgentProjectsApi;
import br.com.messages.projects.Project;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.utils.IoUtils;

import java.io.Serializable;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(IntermediaryAgentCoreApi.AGENT_PATH + IntermediaryAgentProjectsApi.ROOT)
@RequiredArgsConstructor
public class IntermediaryController implements Serializable {

    private final RefactorProject refactorProject;

    @SneakyThrows
    @Valid
    @PostMapping(path = "upload")
    public String registration(@RequestParam("file") @NotNull MultipartFile file) {
        var id = UUID.randomUUID().toString();
        log.info("Receiving project original name: {},id: {}, size: {}", file.getOriginalFilename(), id, file.getSize());

        var project = Project.builder()
                .id(id)
                .size(file.getSize())
                .name(file.getOriginalFilename())
                .contentType(file.getContentType())
                .content(IoUtils.toByteArray(file.getInputStream()))
                .build();

        refactorProject.process(project);

        return id;
    }
}
