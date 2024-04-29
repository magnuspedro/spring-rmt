package br.com.detection.detectionagent.methods.dataExtractions;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.exception.NullJavaFileException;
import br.com.detection.detectionagent.methods.dataExtractions.forks.AbstractSyntaxTreeDependent;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class AbstractSyntaxTreeTest {

    private AbstractSyntaxTree abstractSyntaxTree;

    private final String clazz = """
            public class Test {
                public static void main(String[] args) {
                    System.out.println("Hello World");
                }
            }
            """;

    @BeforeEach
    void setUp() {
        this.abstractSyntaxTree = new AbstractSyntaxTree();
    }

    @Test
    @DisplayName("Should test parseAll for null param")
    public void shouldTestParseAllForNullParam() {
        assertThrows(NullJavaFileException.class,
                () -> this.abstractSyntaxTree.parseAll(null));
    }

    @Test
    @DisplayName("Should test parseAll for a java file")
    public void shouldTestParseAllForAJavaFile() {
        var javaFiles = List.of(JavaFile.builder()
                .originalClass(new String(clazz.getBytes()))
                .build());

        var result = this.abstractSyntaxTree.parseAll(javaFiles);

        assertThat(result.getFirst(), instanceOf(CompilationUnit.class));
    }

    @Test
    @DisplayName("Should test parse single string static for param null")
    public void shouldTestParseSingleStringStaticForParamNull() {
        String javaFile = null;

        var result = assertThrows(IllegalArgumentException.class
                , () -> AbstractSyntaxTree.parseSingle(javaFile));

        assertEquals("File cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test parse single for java file for static")
    public void shouldTestParseSingleForJavaFileForStatic() {
        var javaFile = new String(clazz.getBytes());

        var result = AbstractSyntaxTree.parseSingle(javaFile);

        assertThat(result, instanceOf(CompilationUnit.class));
    }

    @Test
    @DisplayName("Should test parse single string for param null")
    public void shouldTestParseSingleStringForParamNull() {
        JavaFile javaFile = null;

        assertThrows(NullJavaFileException.class
                , () -> this.abstractSyntaxTree.parseSingle(javaFile));
    }

    @Test
    @DisplayName("Should test parse single for java file")
    public void shouldTestParseSingleForJavaFile() {
        var javaFile = JavaFile.builder()
                .originalClass(new String(clazz.getBytes()))
                .build();

        var result = this.abstractSyntaxTree.parseSingle(javaFile);

        assertThat(result, instanceOf(CompilationUnit.class));
    }

    @Test
    @DisplayName("Should test supports for null parameter")
    public void shouldTestSupportsForNullParameter() {
        var result = this.abstractSyntaxTree.supports(null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test supports for object instance of AbstractSyntaxTreeDependent")
    public void shouldTestSupportsForObjectInstanceOfAbstractSyntaxTreeDependent() {
        var result = this.abstractSyntaxTree.supports(new AbstractSyntaxTreeDependentImpl());

        assertTrue(result);
    }

    private static class AbstractSyntaxTreeDependentImpl implements AbstractSyntaxTreeDependent {

    }
}
