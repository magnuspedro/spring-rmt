package br.com.detection.detectionagent.refactor;

import br.com.detection.detectionagent.methods.DetectionMethodsManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class RefactorCandidateConsumerTest {

    private RefactorCandidateConsumer refactorCandidateConsumer;

    @Mock
    private DetectionMethodsManager detectionMethodsManager;

    @BeforeEach
    void setUp() {
        refactorCandidateConsumer = new RefactorCandidateConsumer(detectionMethodsManager);
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