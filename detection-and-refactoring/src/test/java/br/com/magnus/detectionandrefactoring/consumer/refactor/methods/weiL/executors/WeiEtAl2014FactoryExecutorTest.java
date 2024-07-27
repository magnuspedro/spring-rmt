package br.com.magnus.detectionandrefactoring.consumer.refactor.methods.weiL.executors;

import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.detectionandrefactoring.refactor.methods.weiL.WeiEtAl2014FactoryCandidate;
import br.com.magnus.detectionandrefactoring.refactor.methods.weiL.executors.WeiEtAl2014FactoryExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    @DisplayName("Should test for parameter null")
    public void shouldTestForParameterNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> this.weiEtAl2014FactoryExecutor.refactor(null));

        assertEquals("RefactorFiles cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for java files null")
    public void shouldTestForJavaFilesNull() {
        var refactoredFiles = RefactorFiles.builder()
                .candidates(List.of(WeiEtAl2014FactoryCandidate.builder().build()))
                .build();

        var result = assertThrows(IllegalArgumentException.class,
                () -> this.weiEtAl2014FactoryExecutor.refactor(refactoredFiles));

        assertEquals("JavaFiles cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for candidate null")
    public void shouldTestForCandidateNull() {
        var refactoredFiles = RefactorFiles.builder()
                .files(List.of())
                .build();

        var result = assertThrows(IllegalArgumentException.class,
                () -> this.weiEtAl2014FactoryExecutor.refactor(refactoredFiles));

        assertEquals("Candidate cannot be null", result.getMessage());
    }


    @Test
    @DisplayName("Should test for valid candidate")
    public void shouldTestForValidCandidate() {
        var refactoredFiles = RefactorFiles.builder()
                .files(createJavaFilesFactory())
                .candidates(List.of(createFactoryCandidate()))
                .build();

        this.weiEtAl2014FactoryExecutor.refactor(refactoredFiles);

        assertEquals(6, refactoredFiles.files().size());
        assertEquals(LOGGER_FACTORY_REFACTORED, refactoredFiles.files().get(3).getCompilationUnit().toString());
        assertEquals(DATA_BASE_LOGGER_FACTORY_REFACTORED, refactoredFiles.files().get(4).getCompilationUnit().toString());
        assertEquals(FILE_LOGGER_FACTORY_REFACTORED, refactoredFiles.files().get(5).getCompilationUnit().toString());
        assertEquals(3, refactoredFiles.filesChanged().size());
    }
}