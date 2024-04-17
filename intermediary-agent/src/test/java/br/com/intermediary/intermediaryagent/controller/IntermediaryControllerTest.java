package br.com.intermediary.intermediaryagent.controller;

import br.com.intermediary.intermediaryagent.refactor.RefactorProject;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.utils.IoUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@WebMvcTest(IntermediaryController.class)
class IntermediaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RefactorProject refactorProject;

    @SneakyThrows
    @Test
    void shouldTest200() {
        var multipart = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        mockMvc.perform(multipart("/rmt/api/v1/upload")
                .file(multipart)
        ).andExpect(status().isOk());

        Mockito.verify(refactorProject, Mockito.atLeastOnce()).process(assertArg(it ->
                        Assertions.assertAll("Verify project construction",
                                () -> assertThat(it.getSize(), is(multipart.getSize())),
                                () -> assertThat(it.getName(), is(multipart.getOriginalFilename())),
                                () -> assertThat(it.getContentType(), is(multipart.getContentType())),
                                () -> assertThat(it.getContent(), is(IoUtils.toByteArray(multipart.getInputStream()))))
                )
        );
    }

}