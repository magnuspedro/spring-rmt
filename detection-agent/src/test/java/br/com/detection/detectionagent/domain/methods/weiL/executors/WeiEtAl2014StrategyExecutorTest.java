package br.com.detection.detectionagent.domain.methods.weiL.executors;

import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014FactoryCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static fixtures.Wei.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeiEtAl2014StrategyExecutorTest {

    private WeiEtAl2014StrategyExecutor weiEtAl2014StrategyExecutor;

    @BeforeEach
    void setUp() {
        this.weiEtAl2014StrategyExecutor = new WeiEtAl2014StrategyExecutor();
    }

    @Test
    @DisplayName("Should test for both parameters null")
    public void shouldTestForBothParametersNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> this.weiEtAl2014StrategyExecutor.refactor(null, null));

        assertEquals("Candidate cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for java files null")
    public void shouldTestForJavaFilesNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> this.weiEtAl2014StrategyExecutor.refactor(WeiEtAl2014FactoryCandidate.builder().build(), null));

        assertEquals("JavaFiles cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for valid candidate")
    public void shouldTestForValidCandidate() {
        var files = createJavaFilesStrategy();
        var candidate = createStrategyCandidate();

        this.weiEtAl2014StrategyExecutor.refactor(candidate, files);

        assertEquals(5, files.size());
        assertEquals(MOVE_TICKET__REFACTORED, files.get(0).getCompilationUnit().toString());
        assertEquals(STRATEGY_REFACTORED, files.get(1).getCompilationUnit().toString());
        assertEquals(STRATEGY_S_REFACTORED, files.get(2).getCompilationUnit().toString());
        assertEquals(STRATEGY_C_REFACTORED, files.get(3).getCompilationUnit().toString());
        assertEquals(STRATEGY_M_REFACTORED, files.get(4).getCompilationUnit().toString());
    }
}