package br.com.magnus.detectionandrefactoring.refactor.methods.cinneid.minitransformations;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AbstractSyntaxTree;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
class MinitransformationTest {
    private final String testClass = """
                    class TestClass {
                    public void method1() {
                    System.out.println("Hello World");
                    }
                    public String method2() {
                    return "Hello World";
                    }
            }""";

    @Test
    @DisplayName("Should create interface and implements it")
    public void shouldAbstractionInterfaceAndImplementsIt() {
        var clazz = AstHandler.getClassOrInterfaceDeclaration((CompilationUnit) AbstractSyntaxTree.parseSingle(testClass)).get();

        Minitransformation.Abstraction(clazz);

        assertEquals("TestClassInterface", clazz.getImplementedTypes().getFirst().get().getNameAsString());
    }

}