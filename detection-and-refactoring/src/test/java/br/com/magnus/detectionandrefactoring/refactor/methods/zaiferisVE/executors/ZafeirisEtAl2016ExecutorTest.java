package br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.executors;

import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.ZafeirisEtAl2016Candidate;
import fixtures.Zafeiris;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
                () -> zafeirisEtAl2016Executor.refactor(null));

        assertEquals("RefactorFiles cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test refactor for java files null")
    public void shouldTestRefactorForJavaFilesNull() {
        var refactoredFiles = RefactorFiles.builder()
                .candidates(List.of(ZafeirisEtAl2016Candidate.builder().build()))
                .build();

        var result = assertThrows(IllegalArgumentException.class,
                () -> zafeirisEtAl2016Executor.refactor(refactoredFiles));

        assertEquals("JavaFiles cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test refactor for candidate")
    public void shouldTestRefactorForCandidate() {
        var refactoredFiles = RefactorFiles.builder()
                .files(Zafeiris.createJavaFiles())
                .candidates(List.of(Zafeiris.getCandidate()))
                .build();

        zafeirisEtAl2016Executor.refactor(refactoredFiles);

        assertEquals(Zafeiris.REFACTORED_PARENT, refactoredFiles.files().getFirst().getCompilationUnit().toString());
        assertEquals(Zafeiris.REFACTORED_CHILD, (refactoredFiles.files().get(1).getCompilationUnit().toString()));
        assertEquals(2, refactoredFiles.filesChanged().size());
    }
}