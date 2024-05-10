package br.com.detection.detectionagent.consumer;

import br.com.detection.detectionagent.refactor.methods.DetectionMethodsManager;
import br.com.detection.detectionagent.repository.ProjectUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class RefactorCandidateConsumerTest {


    @Mock
    private DetectionMethodsManager detectionMethodsManager;
    @Mock
    private ProjectUpdater projectUpdater;
    private RefactorCandidateConsumer refactorCandidateConsumer;


    @BeforeEach
    void setUp() {
        refactorCandidateConsumer = new RefactorCandidateConsumer(detectionMethodsManager, projectUpdater);
    }

    @Test
    @DisplayName("Should test consumer with null")
    public void shouldTestConsumerWithNull() {
        assertDoesNotThrow(() -> refactorCandidateConsumer.listener(null));
    }

    @Test
    @DisplayName("Should test consumer")
    public void shouldTestConsumer() {
        assertDoesNotThrow(() -> refactorCandidateConsumer.listener("id"));
    }
}