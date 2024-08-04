package br.com.magnus.detectionandrefactoring.refactor.methods.cinneid.helpers;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AbstractSyntaxTree;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class HelpersTest {
    private final String testClass = """
                    class TestClass {
                    
                    public String test;
                    
                    public TestClass(String test) {
                        this.test = test;
                    }
                    
                    public TestClass() {
                    }
                    
                    public void method1() {
                    new TestClass();
                    System.out.println("Hello World");
                    }
                    public String method2() {
                    new TestClass("Test");
                    return "Hello World";
                    }
            }""";


    @Test
    @DisplayName("Should test abstractClass method")
    public void shouldTestAbstractClassMethod() {
        var clazz = AstHandler.getClassOrInterfaceDeclaration((CompilationUnit) AbstractSyntaxTree.parseSingle(testClass)).get();
        var inter = """
                interface TestInterface {
                                
                    void method1();
                                
                    String method2();
                }""";

        var result = Helpers.abstractClass(clazz, "TestInterface");

        assertEquals(inter, result.get().toString());
    }

    @Test
    @DisplayName("Should test abstractClass method without method")
    public void shouldTestAbstractClassMethodWithoutMethod() {
        var clazz = new ClassOrInterfaceDeclaration();

        var result = Helpers.abstractClass(clazz, "TestInterface");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test abstractClass method without public method")
    public void shouldTestAbstractClassMethodWithoutPublicMethod() {
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.addMethod("method1", Modifier.Keyword.PRIVATE);

        var result = Helpers.abstractClass(clazz, "TestInterface");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test makeAbstract method with parameters")
    public void shouldTestMakeAbstractMethodWithParameters() {
        var method = """
                public TestClass method1(String test) {
                    return new TestClass(test);
                }""";
        var clazz = AstHandler.getClassOrInterfaceDeclaration((CompilationUnit) AbstractSyntaxTree.parseSingle(testClass)).get();
        var constructor = clazz.getConstructors().getFirst();

        var result = Helpers.makeAbstract(constructor, "method1");

        assertEquals(method, result.get().toString());
    }

    @Test
    @DisplayName("Should test makeAbstract method without parameters")
    public void shouldTestMakeAbstractMethodWithoutParameters() {
        var method = """
                public TestClass method1() {
                    return new TestClass();
                }""";
        var clazz = AstHandler.getClassOrInterfaceDeclaration((CompilationUnit) AbstractSyntaxTree.parseSingle(testClass)).get();
        var constructor = clazz.getConstructors().get(1);

        var result = Helpers.makeAbstract(constructor, "method1");

        assertEquals(method, result.get().toString());
    }

    @Test
    @DisplayName("Should test makeAbstract method without constructor")
    public void shouldTestMakeAbstractMethodWithoutConstructor() {
        ConstructorDeclaration constructor = null;

        var result = Helpers.makeAbstract(constructor, "method1");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test replaceObjCreationMethod without arguments")
    public void shouldTestReplaceObjCreationMethodWithoutArguments() {
        var clazz = AstHandler.getClassOrInterfaceDeclaration((CompilationUnit) AbstractSyntaxTree.parseSingle(testClass)).get();
        var objectCreation = AstHandler.getObjectCreationExprList(clazz);

        var result = Helpers.replaceObjCreationWithMethInvocation(objectCreation.getFirst(), "method1");

        assertEquals("method1()", result.get().toString());
    }

    @Test
    @DisplayName("Should test replaceObjectCreationMethod with arguments")
    public void shouldTestReplaceObjectCreationMethodWithArguments() {
        var clazz = AstHandler.getClassOrInterfaceDeclaration((CompilationUnit) AbstractSyntaxTree.parseSingle(testClass)).get();
        var objectCreation = AstHandler.getObjectCreationExprList(clazz);

        var result = Helpers.replaceObjCreationWithMethInvocation(objectCreation.get(1), "method1");

        assertEquals("method1(\"Test\")", result.get().toString());
    }

    @Test
    @DisplayName("Should test replaceObjectCreationMethod with null")
    public void shouldTestReplaceObjectCreationMethodWithNull() {
        var result = Helpers.replaceObjCreationWithMethInvocation(null, "method1");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test replaceClassWithInterface")
    public void shouldTestReplaceClassWithInterface() {
        var inf = new ClassOrInterfaceDeclaration()
                .setName("TestInterface")
                .setInterface(true);
        var v = new VariableDeclarator().setType("TestClass").setName("test");

        Helpers.replaceClassWithInterface(v, inf);

        assertEquals(inf.getNameAsString(), v.getTypeAsString());
    }

    @Test
    @DisplayName("Should test replaceClassWithInterface without interface")
    public void shouldTestReplaceClassWithInterfaceWithoutInterface() {
        var inf = new ClassOrInterfaceDeclaration()
                .setName("TestInterface");
        var v = new VariableDeclarator();

        var result = assertThrows(IllegalArgumentException.class,
                () -> Helpers.replaceClassWithInterface(v, inf));

        assertEquals("The class must be an interface", result.getMessage());
    }
}