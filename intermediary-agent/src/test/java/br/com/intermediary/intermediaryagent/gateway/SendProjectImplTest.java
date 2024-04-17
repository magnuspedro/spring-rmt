package br.com.intermediary.intermediaryagent.gateway;

import br.com.intermediary.intermediaryagent.configuration.SqsProperties;
import br.com.magnus.config.starter.projects.Project;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendProjectImplTest {

    @Mock
    private SqsTemplate sqsTemplate;
    private SendProjectImpl sendProject;

    @BeforeEach
    void setup() {
        var sqsProperties = new SqsProperties("fila");
        this.sendProject = new SendProjectImpl(sqsTemplate, sqsProperties);
    }

    @Test
    @DisplayName("Deve testar o envio do projeto para a fila")
    void deveTestarOEvnioDoProjetoParaAFila() {
        var project = Project.builder().id("id").build();

        this.sendProject.send(project);

       verify(this.sqsTemplate, atLeastOnce()).sendAsync(any());
    }

}