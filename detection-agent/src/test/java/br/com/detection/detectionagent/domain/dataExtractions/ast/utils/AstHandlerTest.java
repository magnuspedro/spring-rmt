package br.com.detection.detectionagent.domain.dataExtractions.ast.utils;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AstHandlerTest {

    private AstHandler astHandler;

    @BeforeEach
    void setup() {
        this.astHandler = new AstHandler();
    }

    @Test
    @DisplayName("Should test method get declared fields for null")
    public void shouldTestMethodGetDeclaredFieldsForNull() {
        var result = assertThrows(NullNodeException.class,
                () -> this.astHandler.getDeclaredFields(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test method get declared fields for no fields")
    public void shouldTestMethodGetDeclaredFieldsForNoFields() {
        var node = new MethodDeclaration();

        var result = this.astHandler.getDeclaredFields(node);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test method get declared fields")
    public void shouldTestMethodGetDeclaredFields() {
        var node = new ClassOrInterfaceDeclaration(
                null,
                Modifier.PUBLIC.toEnumSet(),
                NodeList.nodeList(),
                false,
                new SimpleName("TestClazz"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                NodeList.nodeList(),
                NodeList.nodeList(new FieldDeclaration()));

        var result = this.astHandler.getDeclaredFields(node);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should test get object creation expr with node null")
    public void shouldTestGetObjectCreationExprWithNodeNull() {
        var result = assertThrows(NullNodeException.class,
                () -> this.astHandler.getObjectCreationExpr(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get object creation expr with no object creation expr")
    public void shouldTestGetObjectCreationExprWithNoObjectCreationExpr() {
        var node = new MethodDeclaration();

        var result = this.astHandler.getObjectCreationExpr(node);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get object creation expr")
    public void shouldTestGetObjectCreationExpr() {
        var objectCreationExpr = new ObjectCreationExpr();
        var node = new ObjectCreationExpr(null, new ClassOrInterfaceType(), NodeList.nodeList(objectCreationExpr));

        var result = this.astHandler.getObjectCreationExpr(node);

        assertEquals(objectCreationExpr, result.get());
    }

    @Test
    @DisplayName("Should teste get return stmt null")
    public void shouldTesteGetReturnStmtNull() {
        var result = assertThrows(NullIfStmtException.class,
                () -> this.astHandler.getReturnStmt(null));

        assertEquals("IfStmt cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get return stmt with no then block")
    public void shouldTestGetReturnStmtWithNoThenBlock() {
        var node = new IfStmt();

        var result = this.astHandler.getReturnStmt(node);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get return stmt")
    public void shouldTestGetReturnStmt() {
        var returnStmt = new ReturnStmt();
        var node = new IfStmt(new BooleanLiteralExpr(),
                new BlockStmt(null, NodeList.nodeList(returnStmt)),
                new BlockStmt());

        var result = this.astHandler.getReturnStmt(node);

        assertEquals(returnStmt, result.get());
    }

    @Test
    @DisplayName("Should test get name expr for null")
    public void shouldTestGetNameExprForNull() {
        var result = assertThrows(NullNameException.class,
                () -> this.astHandler.getNameExpr(null));

        assertEquals("Name cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get name expr for no child nodes")
    public void shouldTestGetNameExprForNoChildNodes() {
        var node = new SimpleName();

        var result = this.astHandler.getNameExpr(node);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test for get name expr with NameExpr")
    public void shouldTestForGetNameExprWithNameExpr() {
        var node = new VariableDeclarator();
        node.setInitializer("method");

        var result = this.astHandler.getNameExpr(node);

        assertEquals("method", result.get().toString());
    }

    @Test
    @DisplayName("Should test for get variable simple name with null")
    public void shouldTestForGetVariableSimpleNameWithNull() {
        var result = assertThrows(NullNameException.class,
                () -> this.astHandler.getVariableSimpleName(null));

        assertEquals("Name cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for get variable simple name")
    public void shouldTestForGetVariableSimpleName() {
        var node = new VariableDeclarator(PrimitiveType.intType(), "i");

        var result = this.astHandler.getVariableSimpleName(node);

        assertEquals("i", result.get().toString());
    }

    @Test
    @DisplayName("Should test for get parent type with null")
    public void shouldTestForGetParentTypeWithNull() {
        CompilationUnit cu = null;

        var result = assertThrows(NoClassOrInterfaceException.class,
                () -> this.astHandler.getParentType(cu));

        assertEquals("No class or interface found in the compilation unit", result.getMessage());
    }

    @Test
    @DisplayName("Should test for get parent from compilation unit")
    public void shouldTestForGetParentFromCompilationUnit() {
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.setImplementedTypes(NodeList.nodeList(new ClassOrInterfaceType("Interface")));
        var cu = new CompilationUnit();
        cu.getTypes().add(clazz);

        var result = this.astHandler.getParentType(cu);

        assertEquals("Interface", result.get().getNameAsString());
    }

    @Test
    @DisplayName("Should test for get parent type from class or interface declaration null")
    public void shouldTestForGetParentTypeFromClassOrInterfaceDeclarationNull() {
        ClassOrInterfaceDeclaration clazz = null;

        var result = assertThrows(NoClassOrInterfaceDeclarationException.class,
                () -> this.astHandler.getParentType(clazz));

        assertEquals("No class or interface declaration found", result.getMessage());
    }

    @Test
    @DisplayName("Should test for get parent type from class or interface declaration without parent type")
    public void shouldTestForGetParentTypeFromClassOrInterfaceDeclarationWithoutParentType() {
        var clazz = new ClassOrInterfaceDeclaration();

        var result = this.astHandler.getParentType(clazz);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test for get parent type from class or interface declaration")
    public void shouldTestForGetParentTypeFromClassOrInterfaceDeclaration() {
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.setParentNode(new ClassOrInterfaceType("Parent"));

        var result = this.astHandler.getParentType(clazz);

        assertEquals("Parent", result.get().getNameAsString());
    }

    @Test
    @DisplayName("Should test for get parent from compilation unit and all classes with null")
    public void shouldTestForGetParentFromCompilationUnitAndAllClassesWithNull() {
        CompilationUnit cu = null;

        var result = assertThrows(NoClassOrInterfaceException.class,
                () -> this.astHandler.getParent(cu, List.of()));

        assertEquals("No class or interface found in the compilation unit", result.getMessage());
    }

    @Test
    @DisplayName("Should test for get parent without parent class")
    public void shouldTestForGetParentWithoutParentClass() {
        var cu = new CompilationUnit();

        var result = this.astHandler.getParent(cu, List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test for get parent with different names")
    public void shouldTestForGetParentWithDifferentNames() {
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.setImplementedTypes(NodeList.nodeList(new ClassOrInterfaceType("Interface")));
        var cu = new CompilationUnit();
        cu.getTypes().add(clazz);

        var result = this.astHandler.getParent(cu, List.of(cu));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test for get parent with parent class")
    public void shouldTestForGetParentWithParentClass() {
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.setImplementedTypes(NodeList.nodeList(new ClassOrInterfaceType("Interface")));
        var cu = new CompilationUnit();
        cu.getTypes().add(clazz);
        var cuList = List.of(new CompilationUnit());
        var clazzDeclaration = new ClassOrInterfaceDeclaration();
        clazzDeclaration.setName("Interface");
        cuList.get(0).getTypes().add(clazzDeclaration);

        var result = this.astHandler.getParent(cu, cuList);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should test for get package declaration null")
    public void shouldTestForGetPackageDeclarationNull() {
        CompilationUnit cu = null;

        var result = assertThrows(NullCompilationUnitException.class,
                () -> astHandler.getPackageDeclaration(cu));

        assertEquals("Compilation unit cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for get package declaration without package declaration")
    public void shouldTestForGetPackageDeclarationWithoutPackageDeclaration() {
        var cu = new CompilationUnit();

        var result = assertThrows(NoPackageDeclarationException.class,
                () -> astHandler.getPackageDeclaration(cu));

        assertEquals("Package declaration not be found", result.getMessage());
    }

    @Test
    @DisplayName("Should test for get package declaration with package declaration")
    public void shouldTestForGetPackageDeclarationWithPackageDeclaration() {
        var cu = new CompilationUnit();
        cu.setPackageDeclaration("br.com.detection.detectionagent.domain.dataExtractions.ast.utils");

        var result = astHandler.getPackageDeclaration(cu);

        assertEquals("br.com.detection.detectionagent.domain.dataExtractions.ast.utils", result.getNameAsString());
    }
}
