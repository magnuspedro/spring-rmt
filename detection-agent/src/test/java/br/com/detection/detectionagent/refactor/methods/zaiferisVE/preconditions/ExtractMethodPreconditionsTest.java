package br.com.detection.detectionagent.refactor.methods.zaiferisVE.preconditions;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VarType;
import com.github.javaparser.ast.type.VoidType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ExtractMethodPreconditionsTest {

    private ExtractMethodPreconditions extractMethodPreconditions;

    @BeforeEach
    public void setup() {
        this.extractMethodPreconditions = new ExtractMethodPreconditions();
    }


    @Test
    @DisplayName("Should return false when fragments is null")
    public void shouldReturnFalseWhenFragmentsIsNull() {
        var overriddenMethod = new MethodDeclaration();
        var method = new MethodDeclaration();

        var result = this.extractMethodPreconditions.isValid(overriddenMethod, method);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when super call is nested")
    public void shouldReturnFalseWhenSuperCallIsNested() {
        var overriddenMethod = new MethodDeclaration();
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(
                        new ExpressionStmt(new MethodCallExpr("cast", new CastExpr())),
                        new ReturnStmt())));

        var result = this.extractMethodPreconditions.isValid(overriddenMethod, method);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when before fragment throw exception")
    public void shouldReturnFalseWhenBeforeFragmentThrowException() {
        var overriddenMethod = new MethodDeclaration();
        var superExpr = new SuperExpr();
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(
                        new ThrowStmt(),
                        new ExpressionStmt(new MethodCallExpr("super", superExpr))
                )));

        var result = this.extractMethodPreconditions.isValid(overriddenMethod, method);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when before fragment has return")
    public void shouldReturnFalseWhenBeforeFragmentHasReturn() {
        var overriddenMethod = new MethodDeclaration();
        var superExpr = new SuperExpr();
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(
                        new ReturnStmt(),
                        new ExpressionStmt(new MethodCallExpr("super", superExpr))
                )));

        var result = this.extractMethodPreconditions.isValid(overriddenMethod, method);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return true when has multiple variables in before fragments method calls")
    public void shouldReturnTrueWhenHasMultipleVariablesInBeforeFragmentsMethodCalls() {
        var overriddenMethod = new MethodDeclaration();
        var superExpr = new SuperExpr();
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(
                        new ExpressionStmt(new VariableDeclarationExpr(new PrimitiveType(), "primitive")),
                        new ExpressionStmt(new VariableDeclarationExpr(new VarType(), "var")),
                        new ExpressionStmt(new MethodCallExpr("super", new NameExpr("primitive"), new NameExpr("var"), superExpr))
                )));

        var result = this.extractMethodPreconditions.isValid(overriddenMethod, method);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when method values do not match")
    public void shouldReturnFalseWhenMethodValuesDoNotMatch() {
        var overriddenMethod = new MethodDeclaration(
                Modifier.PUBLIC.toEnumSet(),
                "overridenMethod",
                new VoidType(),
                NodeList.nodeList()
        );
        var superExpr = new SuperExpr();
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(new Parameter()),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(
                        new ExpressionStmt(new VariableDeclarationExpr(new PrimitiveType(), "primitive")),
                        new ExpressionStmt(new MethodCallExpr("super", new NameExpr("primitive"), superExpr))
                )));

        var result = this.extractMethodPreconditions.isValid(overriddenMethod, method);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when gragments do no have min size")
    public void shouldReturnFalseWhenGragmentsDoNoHaveMinSize() {
        var overriddenMethod = new MethodDeclaration(
                Modifier.PUBLIC.toEnumSet(),
                "overridenMethod",
                new VoidType(),
                NodeList.nodeList()
        );
        var superExpr = new SuperExpr();
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(
                        new ExpressionStmt(new VariableDeclarationExpr(new PrimitiveType(), "primitive")),
                        new ExpressionStmt(new MethodCallExpr("super", new NameExpr("primitive"), superExpr))
                )));

        var result = this.extractMethodPreconditions.isValid(overriddenMethod, method);

        assertFalse(result);
    }

    @Test
    @DisplayName("Shuold return true when all conditions are matched")
    public void shuoldReturnTrueWhenAllConditionsAreMatched() {
        var overriddenMethod = new MethodDeclaration(
                Modifier.PUBLIC.toEnumSet(),
                "overridenMethod",
                new VoidType(),
                NodeList.nodeList()
        );
        var superExpr = new SuperExpr();
        var method = new MethodDeclaration(Modifier.PUBLIC.toEnumSet(),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(
                        new ExpressionStmt(new VariableDeclarationExpr(new PrimitiveType(), "primitive")),
                        new ExpressionStmt(new VariableDeclarationExpr(new PrimitiveType(), "primitive2")),
                        new ExpressionStmt(new VariableDeclarationExpr(new PrimitiveType(), "primitive3")),
                        new ExpressionStmt(new MethodCallExpr("super", new NameExpr("primitive"), superExpr))
                )));

        var result = this.extractMethodPreconditions.isValid(overriddenMethod, method);

        assertTrue(result);
    }


}