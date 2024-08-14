package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.utils;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.NodeConverter;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class NodeConverterTest {

    private NodeConverter nodeConverter;

    @BeforeEach
    void setUp() {
        this.nodeConverter = new NodeConverter();
    }

    @Test
    @DisplayName("Should test name for null")
    public void shouldTestNameForNull() {
        var name = this.nodeConverter.name(null);

        assertEquals("", name);
    }

    @Test
    @DisplayName("Should test name for Name")
    public void shouldTestNameForName() {
        var name = new Name("name");

        var result = this.nodeConverter.name(name);

        assertEquals("Name - name", result);
    }

    @Test
    @DisplayName("Should test name for SimpleName")
    public void shouldTestNameForSimpleName() {
        var simpleName = new SimpleName("simpleName");

        var result = this.nodeConverter.name(simpleName);

        assertEquals("SimpleName - simpleName", result);
    }

    @Test
    @DisplayName("Should test name")
    public void shouldTestName() {
        var cu = new CompilationUnit();

        var name = this.nodeConverter.name(cu);

        assertEquals("CompilationUnit", name);
    }

    @Test
    @DisplayName("Should test children for null")
    public void shouldTestChildrenForNull() {
        var children = this.nodeConverter.children(null);

        assertEquals(0, children.size());
    }


    @Test
    @DisplayName("Should test children")
    public void shouldTestChildren() {
        var children = this.nodeConverter.children(new CompilationUnit("com.test.test"));

        assertEquals(1, children.size());
    }

    @Test
    @DisplayName("Should test to string with null")
    public void shouldTestToStringWithNull() {
        var result = NodeConverter.toString(null);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should test to string")
    public void shouldTestToString() {
        var result = NodeConverter.toString(new CompilationUnit("com.test.test"));

        assertNotNull(result);
    }


}
