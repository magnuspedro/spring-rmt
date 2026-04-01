package br.com.magnus.projectsyncbff.controller;

import br.com.magnus.projectsyncbff.refactor.RefactorProject;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({MockitoExtension.class})
@WebMvcTest(RestfulController.class)
class RestfulControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RefactorProject refactorProject;

    @Test
    @SneakyThrows
    void shouldTest200() {
        var content = "Hello, World!".getBytes();
        var filename = "hello.txt";

        var part = new MockPart("file", filename, content);
        part.getHeaders().setContentType(MediaType.TEXT_PLAIN);

        mockMvc.perform(multipart("/rmt/api/v1/upload")
                .part(part)
        ).andExpect(status().isOk());

        Mockito.verify(refactorProject, Mockito.atLeastOnce()).process(assertArg(it ->
                Assertions.assertAll("Verify project construction",
                        () -> assertThat(it.getSize(), is((long) content.length)),
                        () -> assertThat(it.getName(), is(filename)),
                        () -> assertThat(it.getContentType(), is(MediaType.TEXT_PLAIN_VALUE)),
                        () -> assertThat(it.getZipContent(), is(content)))
        ));
    }
}
