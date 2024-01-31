package br.com.detection.detectionagent.domain.methods.zeiferisVE.preconditions;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.AstHandler;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.VoidType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class ExtractMethodPreconditionsTest {

    private ExtractMethodPreconditions extractMethodPreconditions;

    @BeforeEach
    public void setup() {
        this.extractMethodPreconditions = new ExtractMethodPreconditions(new AstHandler());
    }


    @Test
    @DisplayName("Should return false when fragments is null")
    public void shouldReturnFalseWhenFragmentsIsNull() {
        var overriddenMethod = new MethodDeclaration();
        var method = new MethodDeclaration();
        var superExpr = new SuperExpr();

        var result = this.extractMethodPreconditions.isValid(overriddenMethod, method, superExpr);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when super call is nested")
    public void shouldReturnFalseWhenSuperCallIsNested() {
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
                        new ExpressionStmt(new MethodCallExpr("cast", new CastExpr())),
                        new ReturnStmt())));

        var result = this.extractMethodPreconditions.isValid(overriddenMethod, method, superExpr);

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

        var result = this.extractMethodPreconditions.isValid(overriddenMethod, method, superExpr);

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

        var result = this.extractMethodPreconditions.isValid(overriddenMethod, method, superExpr);

        assertFalse(result);
    }


}