package br.com.magnus.detection.refactor.methods.zaiferisVE.preconditions;

import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.preconditions.SuperInvocationPreconditions;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.VoidType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SuperInvocationPreconditionsTest {

    private SuperInvocationPreconditions superInvocationPreconditions;

    @BeforeEach
    void setUp() {
        superInvocationPreconditions = new SuperInvocationPreconditions();
    }

    @Test
    @DisplayName("Should return true for a method without super call and with valid names")
    public void shouldReturnTrueForAMethodWithoutSuperCallAndWithValidNames() {
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(), new VoidType(), "methodTest");

        var result = superInvocationPreconditions.violatesAmountOfSuperCallsOrName(method, List.of());

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for no super call and method starting with get")
    public void shouldReturnTrueForNoSuperCallAndMethodStartingWithGet() {
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(), new VoidType(), "getTet");

        var result = superInvocationPreconditions.violatesAmountOfSuperCallsOrName(method, List.of());

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for no super call and method starting with set")
    public void shouldReturnTrueForNoSuperCallAndMethodStartingWithSet() {
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(), new VoidType(), "setTest");

        var result = superInvocationPreconditions.violatesAmountOfSuperCallsOrName(method, List.of());

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for no super call and method with invalid name")
    public void shouldReturnTrueForNoSuperCallAndMethodWithInvalidName() {
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(), new VoidType(), "toString");

        var result = superInvocationPreconditions.violatesAmountOfSuperCallsOrName(method, List.of());

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false for one super call and method with valid name")
    public void shouldReturnFalseForOneSuperCallAndMethodWithValidName() {
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(), new VoidType(), "methodTest");

        var result = superInvocationPreconditions.violatesAmountOfSuperCallsOrName(method, List.of(new SuperExpr()));

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false for one super call and a method starting with get")
    public void shouldReturnFalseForOneSuperCallAndAMethodStartingWithGet() {
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(), new VoidType(), "getTest");

        var result = superInvocationPreconditions.violatesAmountOfSuperCallsOrName(method, List.of(new SuperExpr()));

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false for one super call and a method starting with set")
    public void shouldReturnFalseForOneSuperCallAndAMethodStartingWithSet() {
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(), new VoidType(), "setTest");

        var result = superInvocationPreconditions.violatesAmountOfSuperCallsOrName(method, List.of(new SuperExpr()));

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false for one super call and a method starting with invalid name")
    public void shouldReturnFalseForOneSuperCallAndAMethodStartingWithInvalidName() {
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(), new VoidType(), "toString");

        var result = superInvocationPreconditions.violatesAmountOfSuperCallsOrName(method, List.of(new SuperExpr()));

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return true for a overridden valid public method")
    public void shouldReturnTrueForAOverriddenValidPublicMethod() {
        var overriddenMethod = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(new IfStmt(), new ReturnStmt())));
        var method = new MethodDeclaration();

        var result = superInvocationPreconditions.isOverriddenMethodValid(overriddenMethod, method);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false for a overridden valid protected method")
    public void shouldReturnFalseForAOverriddenValidProtectedMethod() {
        var overriddenMethod = new MethodDeclaration(Modifier.PRIVATE.toEnumSet(),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(new IfStmt(), new ReturnStmt())));
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(), new VoidType(), "testMethod");

        var result = superInvocationPreconditions.isOverriddenMethodValid(overriddenMethod, method);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return fail for a overriden valid without child nodes")
    public void shouldReturnFailForAOverridenValidWithoutChildNodes() {
        var overriddenMethod = new MethodDeclaration(Modifier.PRIVATE.toEnumSet(),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList()));
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(), new VoidType(), "testMethod");

        var result = superInvocationPreconditions.isOverriddenMethodValid(overriddenMethod, method);

        assertFalse(result);
    }


}