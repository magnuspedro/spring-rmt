package br.com.magnus.detection.refactor.methods.zaiferisVE.preconditions;

import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.ZafeirisEtAl2016Candidate;
import br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE.preconditions.SiblingPreconditions;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VarType;
import com.github.javaparser.ast.type.VoidType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SiblingPreconditionsTest {

    private SiblingPreconditions siblingPreconditions;

    @BeforeEach
    void setup() {
        this.siblingPreconditions = new SiblingPreconditions();
    }

    @Test
    @DisplayName("Should return false when no violations are found")
    public void shouldReturnFalseWhenNoViolationsFound() {
        var method = new MethodDeclaration(
                NodeList.nodeList(Modifier.publicModifier()),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(new IfStmt(), new ReturnStmt())));
        var candidates = List.of(ZafeirisEtAl2016Candidate.builder()
                .overridingMethod(method)
                .superCall(new SuperExpr())
                .build());

        var result = siblingPreconditions.violates(candidates);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should throw exception when variables size is greater than one")
    public void shouldThrowExceptionWhenVariablesSizeIsGreaterThanOne() {
        var method = new MethodDeclaration(
                NodeList.nodeList(Modifier.publicModifier()),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(new IfStmt(new VariableDeclarationExpr(new VarType(), "varIf"), new ReturnStmt(), null),
                        new ExpressionStmt(new VariableDeclarationExpr(new VarType(), "var")),
                        new ExpressionStmt(new VariableDeclarationExpr(new PrimitiveType(), "primitive")),
                        new ExpressionStmt(new VariableDeclarationExpr(new PrimitiveType(), "primitive2")),
                        new ExpressionStmt(new MethodCallExpr("super", new SuperExpr(),
                                new NameExpr("varIf"),
                                new NameExpr("var"),
                                new NameExpr("primitive"),
                                new NameExpr("primitive2")
                        )),
                        new ReturnStmt())));
        var candidates = List.of(ZafeirisEtAl2016Candidate.builder()
                        .overridingMethod(new MethodDeclaration())
                        .superCall(new SuperExpr())
                        .build(),
                ZafeirisEtAl2016Candidate.builder()
                        .overridingMethod(method)
                        .superCall(new SuperExpr())
                        .build(),
                ZafeirisEtAl2016Candidate.builder()
                        .overridingMethod(new MethodDeclaration())
                        .superCall(new SuperExpr())
                        .build());

        assertThrows(IllegalStateException.class, () -> siblingPreconditions.violates(candidates));
    }

    @Test
    @DisplayName("Should return true when all the conditions are met")
    public void shouldReturnTrueWhenAllTheConditionsAreMet() {
        var superExpr = new SuperExpr();
        superExpr.setParentNode(new MethodCallExpr());
        var method = new MethodDeclaration(
                NodeList.nodeList(Modifier.publicModifier()),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(new IfStmt(new VariableDeclarationExpr(new VarType(), "varIf"), new ReturnStmt(), null),
                        new ExpressionStmt(new VariableDeclarationExpr(new VarType(), "var5")),
                        new ExpressionStmt(new VariableDeclarationExpr(new PrimitiveType(), "primitive")),
                        new ExpressionStmt(new VariableDeclarationExpr(new PrimitiveType(), "primitive2")),
                        new ExpressionStmt(new MethodCallExpr("super", new SuperExpr(),
                                new NameExpr("varIf"),
                                new NameExpr("var5"),
                                new NameExpr("primitive"),
                                new NameExpr("primitive2")
                        )),
                        new ReturnStmt())));
        var method2 = new MethodDeclaration(
                NodeList.nodeList(Modifier.publicModifier()),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(
                        new ExpressionStmt(new VariableDeclarationExpr(new PrimitiveType(), "var")),
                        new ExpressionStmt(new MethodCallExpr("super", new SuperExpr(),
                                new NameExpr("var")
                        )),
                        new ReturnStmt())));
        var method3 = new MethodDeclaration(
                NodeList.nodeList(Modifier.publicModifier()),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(
                        new ExpressionStmt(new VariableDeclarationExpr(new VarType(), "var2")),
                        new ExpressionStmt(new MethodCallExpr("super", new SuperExpr(),
                                new NameExpr("var2")
                        )),
                        new ReturnStmt())));
        var candidates = List.of(ZafeirisEtAl2016Candidate.builder()
                        .id("1")
                        .overridingMethod(method)
                        .superCall(superExpr)
                        .classDcl(new ClassOrInterfaceDeclaration())
                        .build(),
                ZafeirisEtAl2016Candidate.builder()
                        .id("2")
                        .overridingMethod(method2)
                        .superCall(superExpr)
                        .classDcl(new ClassOrInterfaceDeclaration())
                        .build(),
                ZafeirisEtAl2016Candidate.builder()
                        .id("3")
                        .overridingMethod(method3)
                        .superCall(superExpr)
                        .classDcl(new ClassOrInterfaceDeclaration())
                        .build());

        var result = siblingPreconditions.violates(candidates);

        assertTrue(result);
    }


    @Test
    @DisplayName("Should return false when variables have the same name")
    public void shouldReturnFalseWhenVariablesHaveTheSameName() {
        var superExpr = new SuperExpr();
        superExpr.setParentNode(new MethodCallExpr());
        var method = new MethodDeclaration(
                NodeList.nodeList(Modifier.publicModifier()),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(new IfStmt(new VariableDeclarationExpr(new VarType(), "varIf"), new ReturnStmt(), null),
                        new ExpressionStmt(new VariableDeclarationExpr(new VarType(), "var")),
                        new ExpressionStmt(new VariableDeclarationExpr(new PrimitiveType(), "primitive")),
                        new ExpressionStmt(new VariableDeclarationExpr(new PrimitiveType(), "primitive2")),
                        new ExpressionStmt(new MethodCallExpr("super", new SuperExpr(),
                                new NameExpr("varIf"),
                                new NameExpr("var5"),
                                new NameExpr("primitive"),
                                new NameExpr("primitive2")
                        )),
                        new ReturnStmt())));

        var method2 = new MethodDeclaration(
                NodeList.nodeList(Modifier.publicModifier()),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(
                        new ExpressionStmt(new VariableDeclarationExpr(new VarType(), "var")),
                        new ExpressionStmt(new MethodCallExpr("super", new SuperExpr(),
                                new NameExpr("var")
                        )),
                        new ReturnStmt())));
        var method3 = new MethodDeclaration(
                NodeList.nodeList(Modifier.publicModifier()),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new VoidType(),
                new SimpleName("parentMethod"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                new BlockStmt(NodeList.nodeList(
                        new ExpressionStmt(new VariableDeclarationExpr(new VarType(), "var")),
                        new ExpressionStmt(new MethodCallExpr("super", new SuperExpr(),
                                new NameExpr("var")
                        )),
                        new ReturnStmt())));
        var candidates = List.of(ZafeirisEtAl2016Candidate.builder()
                        .id("1")
                        .overridingMethod(method)
                        .superCall(superExpr)
                        .classDcl(new ClassOrInterfaceDeclaration())
                        .build(),
                ZafeirisEtAl2016Candidate.builder()
                        .id("2")
                        .overridingMethod(method2)
                        .superCall(superExpr)
                        .classDcl(new ClassOrInterfaceDeclaration())
                        .build(),
                ZafeirisEtAl2016Candidate.builder()
                        .id("3")
                        .overridingMethod(method3)
                        .superCall(superExpr)
                        .classDcl(new ClassOrInterfaceDeclaration())
                        .build());

        var result = siblingPreconditions.violates(candidates);

        assertFalse(result);
    }
}