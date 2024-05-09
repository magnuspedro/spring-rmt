package br.com.detection.detectionagent.domain.methods.weiL.verifiers;

import br.com.detection.detectionagent.file.JavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import fixtures.Wei;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WeiEtAl2014StrategyVerifierTest {

    private WeiEtAl2014StrategyVerifier weiEtAl2014StrategyVerifier;

    @BeforeEach
    void setUp() {
        this.weiEtAl2014StrategyVerifier = new WeiEtAl2014StrategyVerifier();
    }

    @Test
    @DisplayName("Should test retrieveCandidatesFrom with argument null")
    public void shouldTestRetrieveCandidatesFromWithArgumentNull() {
        var result = assertThrows(IllegalArgumentException.class, () -> this.weiEtAl2014StrategyVerifier.retrieveCandidatesFrom(null));

        assertEquals("JavaFiles cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test retrieveCandidatesFrom with empty list")
    public void shouldTestRetrieveCandidatesFromWithEmptyList() {
        var result = this.weiEtAl2014StrategyVerifier.retrieveCandidatesFrom(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test retrieveCandidateFrom with no class")
    public void shouldTestRetrieveCandidateFromWithNoClass() {
        var file = JavaFile.builder()
                .parsed(new CompilationUnit())
                .build();

        var result = this.weiEtAl2014StrategyVerifier.retrieveCandidatesFrom(List.of(file));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test retrieveCandidateFrom with interface")
    public void shouldTestRetrieveCandidateFromWithInterface() {
        var cu = new CompilationUnit();
        var clazz = cu.addClass("Test");
        clazz.setInterface(true);
        var file = JavaFile.builder()
                .parsed(cu)
                .build();

        var result = this.weiEtAl2014StrategyVerifier.retrieveCandidatesFrom(List.of(file));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test retrieveCandidateFrom with no methods")
    public void shouldTestRetrieveCandidateFromWithNoMethods() {
        var cu = new CompilationUnit();
        cu.addClass("Test");
        var file = JavaFile.builder()
                .parsed(cu)
                .build();

        var result = this.weiEtAl2014StrategyVerifier.retrieveCandidatesFrom(List.of(file));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test retrieveCandidateFrom with non void method")
    public void shouldTestRetrieveCandidateFromWithNonVoidMethod() {
        var cu = new CompilationUnit();
        var clazz = cu.addClass("Test");
        var method = clazz.addMethod("test");
        method.setType("int");
        var file = JavaFile.builder()
                .parsed(cu)
                .build();

        var result = this.weiEtAl2014StrategyVerifier.retrieveCandidatesFrom(List.of(file));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test retrieveCandidateFrom with one parameter")
    public void shouldTestRetrieveCandidateFromWithOneParameter() {
        var cu = new CompilationUnit();
        var clazz = cu.addClass("Test");
        var method = clazz.addMethod("test");
        method.setType("int");
        method.addParameter(new Parameter());
        var file = JavaFile.builder()
                .parsed(cu)
                .build();

        var result = this.weiEtAl2014StrategyVerifier.retrieveCandidatesFrom(List.of(file));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test retrieveCandidateFrom with two parameters")
    public void shouldTestRetrieveCandidateFromWithTwoParameters() {
        var cu = new CompilationUnit();
        var clazz = cu.addClass("Test");
        var method = clazz.addMethod("test");
        method.setType("int");
        method.setParameters(NodeList.nodeList(new Parameter(), new Parameter()));
        var file = JavaFile.builder()
                .parsed(cu)
                .build();

        var result = this.weiEtAl2014StrategyVerifier.retrieveCandidatesFrom(List.of(file));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test retrieveCandidateFrom with empty parameters")
    public void shouldTestRetrieveCandidateFromWithEmptyParameters() {
        var cu = new CompilationUnit();
        var clazz = cu.addClass("Test");
        var method = clazz.addMethod("test");
        method.setType("int");
        method.setParameters(NodeList.nodeList());
        var file = JavaFile.builder()
                .parsed(cu)
                .build();

        var result = this.weiEtAl2014StrategyVerifier.retrieveCandidatesFrom(List.of(file));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test retrieveCandidateFrom with valid class")
    public void shouldTestRetrieveCandidateFromWithValidClass() {
        var files = Wei.createJavaFilesStrategy();

        var result = this.weiEtAl2014StrategyVerifier.retrieveCandidatesFrom(files);

        assertEquals(1, result.size());
    }

}