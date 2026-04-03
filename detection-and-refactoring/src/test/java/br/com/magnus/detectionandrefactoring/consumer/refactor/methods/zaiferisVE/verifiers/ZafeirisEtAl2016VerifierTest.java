package br.com.magnus.detectionandrefactoring.consumer.refactor.methods.zaiferisVE.verifiers;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AbstractSyntaxTree;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.preconditions.ExtractMethodPreconditions;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.preconditions.SiblingPreconditions;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.preconditions.SuperInvocationPreconditions;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.verifiers.ZafeirisEtAl2016Verifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static fixtures.Zafeiris.createJavaFiles;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZafeirisEtAl2016VerifierTest {

    @Mock
    private SuperInvocationPreconditions superInvocationPreconditions;

    @Mock
    private ExtractMethodPreconditions extractMethodPreconditions;

    @Mock
    private SiblingPreconditions siblingPreconditions;


    private ZafeirisEtAl2016Verifier zafeirisEtAl2016Verifier;

    @BeforeEach
    void setUp() {
        zafeirisEtAl2016Verifier = new ZafeirisEtAl2016Verifier(superInvocationPreconditions, extractMethodPreconditions, siblingPreconditions);
    }

    @Test
    @DisplayName("Should retrieve candidate from null java file")
    public void shouldRetrieveCandidateFromNullJavaFile() {
        var candidates = zafeirisEtAl2016Verifier.retrieveCandidatesFrom(List.of());

        assertTrue(candidates.isEmpty());
    }

    @Test
    @DisplayName("Should violates amount of super calls or name")
    public void shouldViolatesAmountOfSuperCallsOrName() {
        var javaFiles = createJavaFiles();
        when(superInvocationPreconditions.violatesAmountOfSuperCallsOrName(any(), any())).thenReturn(true);

        var candidate = zafeirisEtAl2016Verifier.retrieveCandidatesFrom(javaFiles);

        verify(superInvocationPreconditions, never()).isOverriddenMethodValid(any(), any());
        verify(extractMethodPreconditions, never()).isValid(any(), any());
        verify(siblingPreconditions, never()).violates(any(), any());
        assertEquals(0, candidate.size());
    }

    @Test
    @DisplayName("Should overrides a invalid method")
    public void shouldOverridesAInvalidMethod() {
        var javaFiles = createJavaFiles();
        when(superInvocationPreconditions.violatesAmountOfSuperCallsOrName(any(), any())).thenReturn(false);
        when(superInvocationPreconditions.isOverriddenMethodValid(any(), any())).thenReturn(false);

        var candidate = zafeirisEtAl2016Verifier.retrieveCandidatesFrom(javaFiles);

        verify(extractMethodPreconditions, never()).isValidIgnoringMinSize(any(), any());
        verify(siblingPreconditions, never()).violates(any(), any());
        assertEquals(0, candidate.size());
    }

    @Test
    @DisplayName("Should extract invalid preconditions")
    public void shouldExtractInvalidPreconditions() {
        var javaFiles = createJavaFiles();
        when(superInvocationPreconditions.violatesAmountOfSuperCallsOrName(any(), any())).thenReturn(false);
        when(superInvocationPreconditions.isOverriddenMethodValid(any(), any())).thenReturn(true);
        when(extractMethodPreconditions.isValidIgnoringMinSize(any(), any())).thenReturn(false);

        var candidate = zafeirisEtAl2016Verifier.retrieveCandidatesFrom(javaFiles);

        verify(siblingPreconditions, never()).violates(any(), any());
        assertEquals(0, candidate.size());
    }

    @Test
    @DisplayName("Should violates sibling preconditions")
    public void shouldViolatesSiblingPreconditions() {
        var javaFiles = createJavaFiles();
        when(superInvocationPreconditions.violatesAmountOfSuperCallsOrName(any(), any())).thenReturn(false);
        when(superInvocationPreconditions.isOverriddenMethodValid(any(), any())).thenReturn(true);
        when(extractMethodPreconditions.isValidIgnoringMinSize(any(), any())).thenReturn(true);
        when(extractMethodPreconditions.hasMinimumFragmentsSize(any())).thenReturn(true);
        when(siblingPreconditions.violates(any(), any())).thenReturn(true);

        var candidate = zafeirisEtAl2016Verifier.retrieveCandidatesFrom(javaFiles);

        assertEquals(0, candidate.size());
    }


    @Test
    @DisplayName("Should retrieve candidate from java file")
    public void shouldRetrieveCandidateFromJavaFile() {
        var javaFiles = createJavaFiles();
        when(superInvocationPreconditions.violatesAmountOfSuperCallsOrName(any(), any())).thenReturn(false);
        when(superInvocationPreconditions.isOverriddenMethodValid(any(), any())).thenReturn(true);
        when(extractMethodPreconditions.isValidIgnoringMinSize(any(), any())).thenReturn(true);
        when(extractMethodPreconditions.hasMinimumFragmentsSize(any())).thenReturn(true);
        when(siblingPreconditions.violates(any(), any())).thenReturn(false);

        var candidate = zafeirisEtAl2016Verifier.retrieveCandidatesFrom(javaFiles);

        assertEquals(1, candidate.size());
    }

    @Test
    @DisplayName("Should retrieve all valid super-invocation candidates from the same class")
    public void shouldRetrieveAllValidSuperInvocationCandidatesFromSameClass() {
        var parentSource = """
                package sample;
                public class Parent {
                    protected int a() { return 1; }
                    protected int b() { return 2; }
                }
                """;
        var childSource = """
                package sample;
                public class Child extends Parent {
                    @Override
                    protected int a() {
                        int x = 1;
                        return super.a() + x;
                    }
                    @Override
                    protected int b() {
                        int y = 2;
                        return super.b() + y;
                    }
                }
                """;

        var javaFiles = List.of(
                JavaFile.builder()
                        .name("Parent.java")
                        .path("sample/")
                        .originalClass(parentSource)
                        .parsed(AbstractSyntaxTree.parseSingle(parentSource))
                        .build(),
                JavaFile.builder()
                        .name("Child.java")
                        .path("sample/")
                        .originalClass(childSource)
                        .parsed(AbstractSyntaxTree.parseSingle(childSource))
                        .build()
        );

        when(superInvocationPreconditions.violatesAmountOfSuperCallsOrName(any(), any())).thenReturn(false);
        when(superInvocationPreconditions.isOverriddenMethodValid(any(), any())).thenReturn(true);
        when(extractMethodPreconditions.isValidIgnoringMinSize(any(), any())).thenReturn(true);
        when(extractMethodPreconditions.hasMinimumFragmentsSize(any())).thenReturn(true);
        when(siblingPreconditions.violates(any(), any())).thenReturn(false);

        var candidate = zafeirisEtAl2016Verifier.retrieveCandidatesFrom(javaFiles);

        assertEquals(2, candidate.size());
    }

}
