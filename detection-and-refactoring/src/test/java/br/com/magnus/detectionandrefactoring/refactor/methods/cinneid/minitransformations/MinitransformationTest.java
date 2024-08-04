package br.com.magnus.detectionandrefactoring.refactor.methods.cinneid.minitransformations;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AbstractSyntaxTree;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
class MinitransformationTest {
    private final String testClass = """
                    class TestClass {
                    
                    public String test;
                    public TestClass2 test2;
                    
                    public TestClass(String test) {
                        this.test = test;
                    }
                    
                    public TestClass() {
                    }
                    
                    public void method1() {
                    TestClass2 t2 = new TestClass2();
                    System.out.println("Hello World");
                    }
                    public String method2() {
                    TestClass2 t2 = new TestClass2("Test");
                    return "Hello World";
                    }
            }""";

    private final String testClass2 = """
               class TestClass2 {
                    
                    public String test;
                    
                    public TestClass2(String test) {
                        this.test = test;
                    }
                    
                    public TestClass2() {
                    }
                }
            """;


    @Test
    @DisplayName("Should create interface and implements it")
    public void shouldAbstractionInterfaceAndImplementsIt() {
        var clazz = AstHandler.getClassOrInterfaceDeclaration((CompilationUnit) AbstractSyntaxTree.parseSingle(testClass)).get();

        Minitransformation.Abstraction(clazz);

        assertEquals("TestClassInterface", clazz.getImplementedTypes().getFirst().get().getNameAsString());
    }

    @Test
    @DisplayName("Should encapsulateConstructor")
    public void shouldEncapsulateConstructor() {
        var result = """
                class TestClass {

                    public String test;

                    public TestClass(String test) {
                        this.test = test;
                    }

                    public TestClass() {
                    }

                    public void method1() {
                        createP();
                        System.out.println("Hello World");
                    }

                    public String method2() {
                        createP("Test");
                        return "Hello World";
                    }

                    public TestClass2 createP(String test) {
                        return new TestClass2(test);
                    }

                    public TestClass2 createP() {
                        return new TestClass2();
                    }
                }""";
        var clazz = AstHandler.getClassOrInterfaceDeclaration((CompilationUnit) AbstractSyntaxTree.parseSingle(testClass)).get();
        var clazz2 = AstHandler.getClassOrInterfaceDeclaration((CompilationUnit) AbstractSyntaxTree.parseSingle(testClass2)).get();

        Minitransformation.encapsulateConstruction(clazz, clazz2, "createP");

        assertEquals(result, clazz.toString());
    }

    @Test
    @DisplayName("Should test abstractAccess")
    public void shouldTestAbstractAccessWithoutInterface() {
        var clazz = AstHandler.getClassOrInterfaceDeclaration((CompilationUnit) AbstractSyntaxTree.parseSingle(testClass)).get();
        var clazz2 = AstHandler.getClassOrInterfaceDeclaration((CompilationUnit) AbstractSyntaxTree.parseSingle(testClass2)).get();
        var inf = new ClassOrInterfaceDeclaration().setName("TestClassInterface").setInterface(true);

        Minitransformation.abstractAccess(clazz, clazz2, inf, Set.of());

        var declarations = clazz.getMethods().stream().map(AstHandler::getVariableDeclarations).flatMap(Collection::stream).toList();
        assertEquals("TestClassInterface", clazz.getFieldByName("test2").get().getVariables().getFirst().get().getTypeAsString());
        assertEquals("TestClassInterface", declarations.getFirst().getTypeAsString());
        assertEquals("TestClassInterface", declarations.get(1).getTypeAsString());
    }

    @Test
    @DisplayName("Should test abstractAccess with skipMethod")
    public void shouldTestAbstractAccessWithSkipMethod() {
        var clazz = AstHandler.getClassOrInterfaceDeclaration((CompilationUnit) AbstractSyntaxTree.parseSingle(testClass)).get();
        var clazz2 = AstHandler.getClassOrInterfaceDeclaration((CompilationUnit) AbstractSyntaxTree.parseSingle(testClass2)).get();
        var inf = new ClassOrInterfaceDeclaration().setName("TestClassInterface").setInterface(true);

        Minitransformation.abstractAccess(clazz, clazz2, inf, Set.of("method2"));

        var declarations = clazz.getMethods().stream().map(AstHandler::getVariableDeclarations).flatMap(Collection::stream).toList();
        assertEquals("TestClassInterface", clazz.getFieldByName("test2").get().getVariables().getFirst().get().getTypeAsString());
        assertEquals("TestClassInterface", declarations.getFirst().getTypeAsString());
        assertEquals("TestClass2", declarations.get(1).getTypeAsString());
    }


}