package br.com.magnus.projectsyncbff.gateway;

import br.com.magnus.projectsyncbff.configuration.QueueProperties;
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
    private SendProjectSqS sendProject;

    @BeforeEach
    void setup() {
        var sqsProperties = new QueueProperties("fila");
        this.sendProject = new SendProjectSqS(sqsTemplate, sqsProperties);
    }

    @Test
    @DisplayName("Deve testar o envio do projeto para a fila")
    void deveTestarOEvnioDoProjetoParaAFila() {
        this.sendProject.send("id");

       verify(this.sqsTemplate, atLeastOnce()).sendAsync(any());
    }

}