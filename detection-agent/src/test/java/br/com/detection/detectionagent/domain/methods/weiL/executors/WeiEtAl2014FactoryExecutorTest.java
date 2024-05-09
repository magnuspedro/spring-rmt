package br.com.detection.detectionagent.domain.methods.weiL.executors;

import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014FactoryCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static fixtures.Wei.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeiEtAl2014FactoryExecutorTest {

    private WeiEtAl2014FactoryExecutor weiEtAl2014FactoryExecutor;

    @BeforeEach
    void setUp() {
        this.weiEtAl2014FactoryExecutor = new WeiEtAl2014FactoryExecutor();
    }

    @Test
    @DisplayName("Should test for both parameters null")
    public void shouldTestForBothParametersNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> this.weiEtAl2014FactoryExecutor.refactor(null, null));

        assertEquals("Candidate cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for java files null")
    public void shouldTestForJavaFilesNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> this.weiEtAl2014FactoryExecutor.refactor(WeiEtAl2014FactoryCandidate.builder().build(), null));

        assertEquals("JavaFiles cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for valid candidate")
    public void shouldTestForValidCandidate() {
        var files = createJavaFilesFactory();
        var candidate = createFactoryCandidate();

        this.weiEtAl2014FactoryExecutor.refactor(candidate, files);

        assertEquals(6, files.size());
        assertEquals(LOGGER_FACTORY_REFACTORED, files.get(3).getCompilationUnit().toString());
        assertEquals(DATA_BASE_LOGGER_FACTORY_REFACTORED, files.get(4).getCompilationUnit().toString());
        assertEquals(FILE_LOGGER_FACTORY_REFACTORED, files.get(5).getCompilationUnit().toString());
    }
}