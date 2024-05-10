package br.com.detection.detectionagent.refactor.methods.zeiferisVE.executors;

import br.com.detection.detectionagent.refactor.methods.zeiferisVE.ZafeirisEtAl2016Candidate;
import fixtures.Zafeiris;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ZafeirisEtAl2016ExecutorTest {

    private ZafeirisEtAl2016Executor zafeirisEtAl2016Executor;

    @BeforeEach
    void setUp() {
        this.zafeirisEtAl2016Executor = new ZafeirisEtAl2016Executor();
    }

    @Test
    @DisplayName("Should test refactor for both parameter null")
    public void shouldTestRefactorForBothParameterNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> zafeirisEtAl2016Executor.refactor(null, null));

        assertEquals("Candidate cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test refactor for java files null")
    public void shouldTestRefactorForJavaFilesNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> zafeirisEtAl2016Executor.refactor(ZafeirisEtAl2016Candidate.builder().build(), null));

        assertEquals("JavaFiles cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test refactor for candidate")
    public void shouldTestRefactorForCandidate() {
        var candidate = Zafeiris.getCandidate();
        var javaFiles = Zafeiris.createJavaFiles();

        zafeirisEtAl2016Executor.refactor(candidate, javaFiles);

        assertEquals(Zafeiris.REFACTORED_PARENT, javaFiles.getFirst().getCompilationUnit().toString());
        assertEquals(Zafeiris.REFACTORED_CHILD, candidate.getCompilationUnit().toString());
    }
}