package br.com.magnus.detection.refactor.methods.weiL;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
class LiteralValueExtractorTest {


    @Test
    void shouldReturnEmpty() {
        var binaryExpr = new BinaryExpr(new NameExpr("test"), new IntegerLiteralExpr(10), BinaryExpr.Operator.EQUALS);
        var method = new MethodDeclaration();
        var parameter = new Parameter();
        parameter.setName("type");
        method.setParameters(NodeList.nodeList(parameter));

        var result = LiteralValueExtractor.extractValidLiteralFromNode(binaryExpr, method);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnCharLiteralValue() {
        var binaryExpr = new BinaryExpr(new NameExpr("type"), new CharLiteralExpr('a'), BinaryExpr.Operator.EQUALS);
        var method = new MethodDeclaration();
        var parameter = new Parameter();
        parameter.setName("type");
        method.setParameters(NodeList.nodeList(parameter));

        var result = LiteralValueExtractor.extractValidLiteralFromNode(binaryExpr, method);

        assertTrue(result.isPresent());
        assertEquals("a", result.get());
    }

    @Test
    void shouldReturnIntegerLiteralValue() {
        var binaryExpr = new BinaryExpr(new NameExpr("type"), new IntegerLiteralExpr(10), BinaryExpr.Operator.EQUALS);
        var method = new MethodDeclaration();
        var parameter = new Parameter();
        parameter.setName("type");
        method.setParameters(NodeList.nodeList(parameter));

        var result = LiteralValueExtractor.extractValidLiteralFromNode(binaryExpr, method);

        assertTrue(result.isPresent());
        assertEquals("10", result.get());
    }

    @Test
    void shouldReturnLongLiteralValue() {
        var binaryExpr = new BinaryExpr(new NameExpr("type"), new LongLiteralExpr(100), BinaryExpr.Operator.EQUALS);
        var method = new MethodDeclaration();
        var parameter = new Parameter();
        parameter.setName("type");
        method.setParameters(NodeList.nodeList(parameter));

        var result = LiteralValueExtractor.extractValidLiteralFromNode(binaryExpr, method);

        assertTrue(result.isPresent());
        assertEquals("100", result.get());
    }

    @Test
    void shouldReturnStringLiteralValue() {
        var binaryExpr = new BinaryExpr(new NameExpr("type"), new StringLiteralExpr("test"), BinaryExpr.Operator.EQUALS);
        var method = new MethodDeclaration();
        var parameter = new Parameter();
        parameter.setName("type");
        method.setParameters(NodeList.nodeList(parameter));

        var result = LiteralValueExtractor.extractValidLiteralFromNode(binaryExpr, method);

        assertTrue(result.isPresent());
        assertEquals("test", result.get());
    }
}