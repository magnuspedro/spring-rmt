package br.com.magnus.detection.refactor.methods.weiL.executors;

import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.detectionandrefactoring.refactor.methods.weiL.WeiEtAl2014FactoryCandidate;
import br.com.magnus.detectionandrefactoring.refactor.methods.weiL.executors.WeiEtAl2014StrategyExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    @DisplayName("Should test for parameter null")
    public void shouldTestForParameterNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> this.weiEtAl2014StrategyExecutor.refactor(null));

        assertEquals("RefactorFiles cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for java files null")
    public void shouldTestForJavaFilesNull() {
        var refactoredFiles = RefactorFiles.builder()
                .candidates(List.of(WeiEtAl2014FactoryCandidate.builder().build()))
                .build();

        var result = assertThrows(IllegalArgumentException.class,
                () -> this.weiEtAl2014StrategyExecutor.refactor(refactoredFiles));

        assertEquals("JavaFiles cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for candidate null")
    public void shouldTestForCandidateNull() {
        var refactoredFiles = RefactorFiles.builder()
                .files(List.of())
                .build();

        var result = assertThrows(IllegalArgumentException.class,
                () -> this.weiEtAl2014StrategyExecutor.refactor(refactoredFiles));

        assertEquals("Candidate cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for valid candidate")
    public void shouldTestForValidCandidate() {
        var refactoredFiles = RefactorFiles.builder()
                .files(createJavaFilesStrategy())
                .candidates(List.of(createStrategyCandidate()))
                .build();

        this.weiEtAl2014StrategyExecutor.refactor(refactoredFiles);

        assertEquals(5, refactoredFiles.files().size());
        assertEquals(MOVE_TICKET__REFACTORED, refactoredFiles.files().getFirst().getCompilationUnit().toString());
        assertEquals(STRATEGY_REFACTORED, refactoredFiles.files().get(1).getCompilationUnit().toString());
        assertEquals(STRATEGY_S_REFACTORED, refactoredFiles.files().get(2).getCompilationUnit().toString());
        assertEquals(STRATEGY_C_REFACTORED, refactoredFiles.files().get(3).getCompilationUnit().toString());
        assertEquals(STRATEGY_M_REFACTORED, refactoredFiles.files().get(4).getCompilationUnit().toString());
        assertEquals(5, refactoredFiles.filesChanged().size());
    }
}