package br.com.detection.detectionagent.refactor.methods.zeiferisVE;

import br.com.detection.detectionagent.refactor.dataExtractions.ast.AbstractSyntaxTree;
import br.com.detection.detectionagent.refactor.dataExtractions.ast.AstHandler;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FragmentsSplitterTest {

    private final static String beforeFragment = """
            class Test extends Parent {
                @Override
                public void test() {
                    System.out.println("Test");
                    super.test();
                }
            }
            """;
    private final static String afterFragment = """
            class Test extends Parent {
                @Override
                public void test() {
                    super.test();
                    System.out.println("Test");
                }
            }
            """;
    private final static String beforeAndAfterFragment = """
            class Test extends Parent {
                @Override
                public void test() {
                    System.out.println("Test");
                    super.test();
                    System.out.println("Test");
                }
            }
            """;

    private final static String beforeAndAfterFragmentMethodCall = """
            class Test extends Parent {
                @Override
                public void test() {
                    var test = new Test2();
                    System.out.println("Test");
                    test.test();
                    System.out.println("Test");
                }
            }
            """;

    private final static String beforeAndAfterFragmentMethodCallVar = """
            class Test extends Parent {
                @Override
                public void test() {
                    var test = new Test2();
                    var str = "Test";
                    System.out.println("Test");
                    test.test("string");
                    str = "Test";
                    System.out.println(str);
                }
            }
            """;

    @Test
    @DisplayName("Should test splitByMethod with null method")
    public void shouldTestSplitByMethodWithNullMethod() {
        var result = assertThrows(IllegalArgumentException.class, () -> FragmentsSplitter.splitByMethod(null));

        assertEquals("Method has no body", result.getMessage());
    }

    @Test
    @DisplayName("Should test splitByMethod with no super call")
    public void shouldTestSplitByMethodWithNoSuperCall() {
        var method = new MethodDeclaration();

        var result = FragmentsSplitter.splitByMethod(method);

        assertEquals(0, result.getBeforeFragment().size());
        assertEquals(0, result.getAfterFragment().size());
        assertFalse(result.hasSpecificNode());
        assertEquals(0, result.getVariablesOnBeforeFragmentsMethodClass().size());
        assertTrue(result.getSuperReturnVariable().isEmpty());
    }

    @Test
    @DisplayName("Should test splitByMethod with before fragment")
    public void shouldTestSplitByMethodWithBeforeFragment() {
        var cu = (CompilationUnit) AbstractSyntaxTree.parseSingle(beforeFragment);
        var method = AstHandler.getMethods(cu).getFirst();

        var result = FragmentsSplitter.splitByMethod(method);

        assertEquals(1, result.getBeforeFragment().size());
        assertEquals(0, result.getAfterFragment().size());
        assertTrue(result.hasSpecificNode());
        assertEquals(0, result.getVariablesOnBeforeFragmentsMethodClass().size());
        assertTrue(result.getSuperReturnVariable().isEmpty());
    }

    @Test
    @DisplayName("Should test splitByMethod with after fragment")
    public void shouldTestSplitByMethodWithAfterFragment() {
        var cu = (CompilationUnit) AbstractSyntaxTree.parseSingle(afterFragment);
        var method = AstHandler.getMethods(cu).getFirst();

        var result = FragmentsSplitter.splitByMethod(method);

        assertEquals(0, result.getBeforeFragment().size());
        assertEquals(1, result.getAfterFragment().size());
        assertTrue(result.hasSpecificNode());
        assertEquals(0, result.getVariablesOnBeforeFragmentsMethodClass().size());
        assertTrue(result.getSuperReturnVariable().isEmpty());
    }

    @Test
    @DisplayName("Should test splitByMethod with before and after fragment")
    public void shouldTestSplitByMethodWithBeforeAndAfterFragment() {
        var cu = (CompilationUnit) AbstractSyntaxTree.parseSingle(beforeAndAfterFragment);
        var method = AstHandler.getMethods(cu).getFirst();

        var result = FragmentsSplitter.splitByMethod(method);

        assertEquals(1, result.getBeforeFragment().size());
        assertEquals(1, result.getAfterFragment().size());
        assertTrue(result.hasSpecificNode());
        assertEquals(0, result.getVariablesOnBeforeFragmentsMethodClass().size());
        assertTrue(result.getSuperReturnVariable().isEmpty());
    }


    @Test
    @DisplayName("Should test splitByMethodAndMethodCall with null methods")
    public void shouldTestSplitByMethodAndMethodCallWithNullMethods() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> FragmentsSplitter.splitByMethodAndMethodCall(null, null));

        assertEquals("Method has no body", result.getMessage());
    }

    @Test
    @DisplayName("Should test splitByMethodAndMethodCall with no method call")
    public void shouldTestSplitByMethodAndMethodCallWithNoMethodCall() {
        var method = new MethodDeclaration();

        var result = FragmentsSplitter.splitByMethodAndMethodCall(method, null);

        assertEquals(0, result.getBeforeFragment().size());
        assertEquals(0, result.getAfterFragment().size());
        assertFalse(result.hasSpecificNode());
        assertEquals(0, result.getVariablesOnBeforeFragmentsMethodClass().size());
        assertTrue(result.getSuperReturnVariable().isEmpty());
    }

    @Test
    @DisplayName("Should test splitByMethodAndMethodCall with methods that does not match")
    public void shouldTestSplitByMethodAndMethodCallWithMethodsThatDoesNotMatch() {
        var cu = (CompilationUnit) AbstractSyntaxTree.parseSingle(afterFragment);
        var method = AstHandler.getMethods(cu).getFirst();
        var methodCall = new MethodCallExpr();

        var result = FragmentsSplitter.splitByMethodAndMethodCall(method, methodCall);

        assertEquals(2, result.getBeforeFragment().size());
        assertEquals(0, result.getAfterFragment().size());
        assertFalse(result.hasSpecificNode());
        assertEquals(0, result.getVariablesOnBeforeFragmentsMethodClass().size());
        assertTrue(result.getSuperReturnVariable().isEmpty());
    }

    @Test
    @DisplayName("Should test splitByMethodAndMethodCall with methods that match")
    public void shouldTestSplitByMethodAndMethodCallWithMethodsThatMatch() {
        var cu = (CompilationUnit) AbstractSyntaxTree.parseSingle(beforeAndAfterFragmentMethodCall);
        var method = AstHandler.getMethods(cu).getFirst();
        var methodCall = AstHandler.getMethodCallExpr(cu).get(1);

        var result = FragmentsSplitter.splitByMethodAndMethodCall(method, methodCall);

        assertEquals(2, result.getBeforeFragment().size());
        assertEquals(1, result.getAfterFragment().size());
        assertTrue(result.hasSpecificNode());
        assertEquals(1, result.getVariablesOnBeforeFragmentsMethodClass().size());
        assertTrue(result.getSuperReturnVariable().isEmpty());
    }

    @Test
    @DisplayName("Should test splitMethodAndCall with methods that match and after variable")
    public void shouldTestSplitMethodAndCallWithMethodsThatMatchAndAfterVariable() {
        var cu = (CompilationUnit) AbstractSyntaxTree.parseSingle(beforeAndAfterFragmentMethodCallVar);
        var method = AstHandler.getMethods(cu).getFirst();
        var methodCall = AstHandler.getMethodCallExpr(cu).get(1);

        var result = FragmentsSplitter.splitByMethodAndMethodCall(method, methodCall);

        assertEquals(3, result.getBeforeFragment().size());
        assertEquals(2, result.getAfterFragment().size());
        assertTrue(result.hasSpecificNode());
        assertEquals(2, result.getVariablesOnBeforeFragmentsMethodClass().size());
        assertTrue(result.getSuperReturnVariable().isEmpty());
    }

}