package br.com.detection.detectionagent.domain.dataExtractions.ast.utils;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions.*;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
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
    @DisplayName("Should test get return stmt null")
    public void shouldTestGetReturnStmtNull() {
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
        var result = assertThrows(NullNodeException.class,
                () -> this.astHandler.getNameExpr(null));

        assertEquals("Node cannot be null", result.getMessage());
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
        var result = assertThrows(NullNodeException.class,
                () -> this.astHandler.getVariableSimpleName(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for get variable simple name")
    public void shouldTestForGetVariableSimpleName() {
        var node = new VariableDeclarator(PrimitiveType.intType(), "i");

        var result = this.astHandler.getVariableSimpleName(node);

        assertEquals("i", result.get().toString());
    }

    @Test
    @DisplayName("Should test for get simple name with null")
    public void shouldTestForGetSimpleNameWithNull() {
        var result = assertThrows(NullNodeException.class,
                () -> this.astHandler.getSimpleName(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should tet for get simple name")
    public void shouldTetForGetSimpleName() {
        var clazz = new ClassOrInterfaceDeclaration();

        var result = this.astHandler.getSimpleName(clazz);

        assertThat(result.get(), instanceOf(SimpleName.class));
        assertEquals("empty", result.get().toString());
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
        var result = assertThrows(NoClassOrInterfaceException.class,
                () -> this.astHandler.getParent(null, List.of()));

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
        var result = assertThrows(NullCompilationUnitException.class,
                () -> astHandler.getPackageDeclaration(null));

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

    @Test
    @DisplayName("Should test get class or interface declaration with null")
    public void shouldTestGetClassOrInterfaceDeclarationWithNull() {
        var result = assertThrows(NoClassOrInterfaceException.class,
                () -> astHandler.getClassOrInterfaceDeclaration(null));

        assertEquals("No class or interface found in the compilation unit", result.getMessage());
    }

    @Test
    @DisplayName("Should test get class or interface declaration")
    public void shouldTestGetClassOrInterfaceDeclaration() {
        var clazz = new ClassOrInterfaceDeclaration();
        var cu = new CompilationUnit();
        cu.getTypes().add(clazz);

        var result = astHandler.getClassOrInterfaceDeclaration(cu);

        assertEquals(clazz, result.get());
    }

    @Test
    @DisplayName("Should test get methods with null")
    public void shouldTestGetMethodsWithNull() {
        var result = assertThrows(NullCompilationUnitException.class,
                () -> astHandler.getMethods(null));

        assertEquals("Compilation unit cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get methods without methods")
    public void shouldTestGetMethodsWithoutMethods() {
        var cu = new CompilationUnit();

        var result = astHandler.getMethods(cu);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get methods from compilation unit")
    public void shouldTestGetMethodsFromCompilationUnit() {
        var cu = new CompilationUnit();
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.addMethod("method");
        cu.getTypes().add(clazz);

        var result = astHandler.getMethods(cu);

        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get block statement with null")
    public void shouldTestGetBlockStatementWithNull() {
        var result = astHandler.getBlockStatement(null);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get block statement without block statement")
    public void shouldTestGetBlockStatementWithoutBlockStatement() {
        var node = new ClassOrInterfaceDeclaration();

        var result = astHandler.getBlockStatement(node);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get block statement")
    public void shouldTestGetBlockStatement() {
        var node = new MethodDeclaration();

        var result = astHandler.getBlockStatement(node);

        assertThat(result.get(), instanceOf(BlockStmt.class));
    }

    @Test
    @DisplayName("Should test get expression statement with null")
    public void shouldTestGetExpressionStatementWithNull() {
        var result = astHandler.getExpressionStatement(null);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get expression statement for blockstmt")
    public void shouldTestGetExpressionStatementForBlockstmt() {
        var node = new BlockStmt();

        var result = astHandler.getExpressionStatement(node);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get expression statement for class or interface declaration")
    public void shouldTestGetExpressionStatementForClassOrInterfaceDeclaration() {
        var node = new ClassOrInterfaceDeclaration();

        var result = astHandler.getExpressionStatement(node);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get expression statement for expression statement")
    public void shouldTestGetExpressionStatementForExpressionStatement() {
        var node = new ExpressionStmt();

        var result = astHandler.getExpressionStatement(node);

        assertThat(result.get(), instanceOf(ExpressionStmt.class));
    }

    @Test
    @DisplayName("Should test get expression statement for a parent node")
    public void shouldTestGetExpressionStatementForAParentNode() {
        var node = new IfStmt();
        node.setParentNode(new ExpressionStmt());

        var result = astHandler.getExpressionStatement(node);

        assertThat(result.get(), instanceOf(ExpressionStmt.class));
    }

    @Test
    @DisplayName("Should test get super calls with null")
    public void shouldTestGetSuperCallsWithNull() {
        var result = assertThrows(NullNodeException.class,
                () -> astHandler.getSuperCalls(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get super calls for SuperExpr")
    public void shouldTestGetSuperCallsForSuperExpr() {
        var node = new SuperExpr();

        var result = astHandler.getSuperCalls(node);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should test get super calls for child")
    public void shouldTestGetSuperCallsForChild() {
        var node = new BlockStmt(NodeList.nodeList(new ExpressionStmt(new SuperExpr()),
                new ExpressionStmt(new SuperExpr())));

        var result = astHandler.getSuperCalls(node);

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should test retrieve overridden method for both parameters null")
    public void shouldTestRetrieveOverriddenMethodForBothParametersNull() {
        var result = assertThrows(NullNodeException.class,
                () -> astHandler.retrieveOverriddenMethod(null, null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test retrieve overridden method for overridden method null")
    public void shouldTestRetrieveOverriddenMethodForOverriddenMethodNull() {
        var cu = new CompilationUnit();

        var result = assertThrows(NullNodeException.class,
                () -> astHandler.retrieveOverriddenMethod(cu, null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test retrieve overridden method for parent null")
    public void shouldTestRetrieveOverriddenMethodForParentNull() {
        var method = new MethodDeclaration();

        var result = assertThrows(NullCompilationUnitException.class,
                () -> astHandler.retrieveOverriddenMethod(null, method));

        assertEquals("Compilation unit cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test retrieve overridden method for methods that do no match")
    public void shouldTestRetrieveOverriddenMethodForMethodsThatDoNoMatch() {
        var cu = new CompilationUnit();
        var clazz = new ClassOrInterfaceDeclaration();
        var method = new MethodDeclaration();
        method.setName("metodo");
        method.setParameters(NodeList.nodeList(new Parameter(PrimitiveType.intType(), "i")));
        clazz.addMember(method);
        cu.getTypes().add(clazz);
        var overriddenMethod = new MethodDeclaration();

        var result = astHandler.retrieveOverriddenMethod(cu, overriddenMethod);

        assertNull(result);
    }

    @Test
    @DisplayName("Should test retrieve overridden method for methods that match")
    public void shouldTestRetrieveOverriddenMethodForMethodsThatMatch() {
        var cu = new CompilationUnit();
        var clazz = new ClassOrInterfaceDeclaration();
        var method = new MethodDeclaration();
        method.setName("metodo");
        method.setParameters(NodeList.nodeList(new Parameter(PrimitiveType.intType(), "i")));
        clazz.addMember(method);
        cu.getTypes().add(clazz);
        var overriddenMethod = new MethodDeclaration();
        overriddenMethod.setName("metodo");
        overriddenMethod.setParameters(NodeList.nodeList(new Parameter(PrimitiveType.intType(), "i")));

        var result = astHandler.retrieveOverriddenMethod(cu, overriddenMethod);

        assertNotNull(result);
        assertEquals("metodo", result.getNameAsString());
    }

    @Test
    @DisplayName("Should test methods params match for null")
    public void shouldTestMethodsParamsMatchForNull() {
        var result = assertThrows(NullMethodException.class,
                () -> astHandler.methodsParamsMatch(null, null));

        assertEquals("Method cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test methods params match for m1 null")
    public void shouldTestMethodsParamsMatchForM1Null() {
        var result = assertThrows(NullMethodException.class,
                () -> astHandler.methodsParamsMatch(new MethodDeclaration(), null));

        assertEquals("Method cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test methods params match for m2 null")
    public void shouldTestMethodsParamsMatchForM2Null() {
        var result = assertThrows(NullMethodException.class,
                () -> astHandler.methodsParamsMatch(null, new MethodDeclaration()));

        assertEquals("Method cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test methods params match for different methods")
    public void shouldTestMethodsParamsMatchForDifferentMethods() {
        var m1 = new MethodDeclaration();
        m1.setParameters(NodeList.nodeList(new Parameter(PrimitiveType.intType(), "i")));
        var m2 = new MethodDeclaration();

        var result = astHandler.methodsParamsMatch(m1, m2);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test methods params match for same methods")
    public void shouldTestMethodsParamsMatchForSameMethods() {
        var m1 = new MethodDeclaration();
        m1.setParameters(NodeList.nodeList(new Parameter(PrimitiveType.intType(), "i")));
        var m2 = new MethodDeclaration();
        m2.setParameters(NodeList.nodeList(new Parameter(PrimitiveType.intType(), "i")));

        var result = astHandler.methodsParamsMatch(m1, m2);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test child has direct super call for null params")
    public void shouldTestChildHasDirectSuperCallForNullParams() {
        var result = assertThrows(NullNodeException.class,
                () -> astHandler.childHasDirectSuperCall(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test child has direct super call for node with condition")
    public void shouldTestChildHasDirectSuperCallForNodeWithCondition() {
        var node = new IfStmt();

        var result = astHandler.childHasDirectSuperCall(node);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test child has direct super call for node with try stmt")
    public void shouldTestChildHasDirectSuperCallForNodeWithTryStmt() {
        var node = new TryStmt();

        var result = astHandler.childHasDirectSuperCall(node);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test child has direct super call for node with catch clause")
    public void shouldTestChildHasDirectSuperCallForNodeWithCatchClause() {
        var node = new CatchClause();

        var result = astHandler.childHasDirectSuperCall(node);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test child has direct super call for node with without method call")
    public void shouldTestChildHasDirectSuperCallForNodeWithWithoutMethodCall() {
        var node = new BlockStmt();

        var result = astHandler.childHasDirectSuperCall(node);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test child has direct super call for node with method call")
    public void shouldTestChildHasDirectSuperCallForNodeWithMethodCall() {
        var node = new ExpressionStmt(new SuperExpr());

        var result = astHandler.childHasDirectSuperCall(node);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test child has direct super call for node with method call in child")
    public void shouldTestChildHasDirectSuperCallForNodeWithMethodCallInChild() {
        var node = new BlockStmt(NodeList.nodeList(new ExpressionStmt(new SuperExpr())));

        var result = astHandler.childHasDirectSuperCall(node);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test node has return statement with null")
    public void shouldTestNodeHasReturnStatementWithNull() {
        var result = astHandler.nodeHasReturnStatement(null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test node has return statement with no return statement")
    public void shouldTestNodeHasReturnStatementWithNoReturnStatement() {
        var result = astHandler.nodeHasReturnStatement(new BlockStmt());

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test  node has return statement")
    public void shouldTestNodeHasReturnStatement() {
        var blockStmt = new BlockStmt(NodeList.nodeList(new ReturnStmt()));

        var result = astHandler.nodeHasReturnStatement(blockStmt);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test node throws exception with null")
    public void shouldTestNodeThrowsExceptionWithNull() {
        var result = astHandler.nodeThrowsException(null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test node throws exception with no throw")
    public void shouldTestNodeThrowsExceptionWithNoThrow() {
        var result = astHandler.nodeThrowsException(new BlockStmt());

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test node throws exception")
    public void shouldTestNodeThrowsException() {
        var blockStmt = new BlockStmt(NodeList.nodeList(new ThrowStmt()));

        var result = astHandler.nodeThrowsException(blockStmt);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test for node has clazz with null")
    public void shouldTestForNodeHasClazzWithNull() {
        var result = assertThrows(ClassExpectedException.class,
                () -> astHandler.nodeHasClazz(null, null));

        assertEquals("Class is expected as a parameter", result.getMessage());
    }

    @Test
    @DisplayName("Should test for node has clazz with null clazz")
    public void shouldTestForNodeHasClazzWithNullClazz() {
        var result = assertThrows(ClassExpectedException.class,
                () -> astHandler.nodeHasClazz(new ClassOrInterfaceDeclaration(), null));

        assertEquals("Class is expected as a parameter", result.getMessage());
    }


    @Test
    @DisplayName("Should test for node has clazz with null node")
    public void shouldTestForNodeHasClazzWithNullNode() {
        var result = astHandler.nodeHasClazz(null, ClassOrInterfaceDeclaration.class);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test for node has clazz")
    public void shouldTestForNodeHasClazz() {
        var clazz = new ClassOrInterfaceDeclaration();

        var result = astHandler.nodeHasClazz(clazz, ClassOrInterfaceDeclaration.class);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test extract variable dclr from node with null")
    public void shouldTestExtractVariableDclrFromNodeWithNull() {
        var result = assertThrows(NullNodeException.class,
                () -> astHandler.extractVariableDclrFromNode(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test extract variable dclr from node with no variable declaration")
    public void shouldTestExtractVariableDclrFromNodeWithNoVariableDeclaration() {
        var result = astHandler.extractVariableDclrFromNode(new BlockStmt());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test extract variable dclr from node")
    public void shouldTestExtractVariableDclrFromNode() {
        var blockStmt = new BlockStmt();
        var expressionStmt = new ExpressionStmt();
        var variableDeclarator = new VariableDeclarator();
        var variableDeclarationExpr = new VariableDeclarationExpr(variableDeclarator);
        expressionStmt.setExpression(variableDeclarationExpr);
        blockStmt.setStatements(NodeList.nodeList(expressionStmt));

        var result = astHandler.extractVariableDclrFromNode(blockStmt);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should test variable is present in method call with both parameters null")
    public void shouldTestVariableIsPresentInMethodCallWithBothParametersNull() {
        var result = assertThrows(MethodCallExpectedException.class,
                () -> astHandler.variableIsPresentInMethodCall(null, null));

        assertEquals("Method is expected as a parameter", result.getMessage());
    }

    @Test
    @DisplayName("Should test variable is present in method call with null method call")
    public void shouldTestVariableIsPresentInMethodCallWithNullMethodCall() {
        var result = assertThrows(MethodCallExpectedException.class,
                () -> astHandler.variableIsPresentInMethodCall(new VariableDeclarationExpr(), null));

        assertEquals("Method is expected as a parameter", result.getMessage());
    }

    @Test
    @DisplayName("Should test variable is present in method call with null variable declaration exp")
    public void shouldTestVariableIsPresentInMethodCallWithNullVariableDeclarationExp() {
        var result = astHandler.variableIsPresentInMethodCall(null, new MethodCallExpr());

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test variable is present in method call")
    public void shouldTestVariableIsPresentInMethodCall() {
        var variableDeclarationExpr = new VariableDeclarationExpr();
        var variableDeclarator = new VariableDeclarator(PrimitiveType.intType(), "i");
        variableDeclarationExpr.setVariables(NodeList.nodeList(variableDeclarator));
        var methodCallExpr = new MethodCallExpr("method", new NameExpr("i"));

        var result = astHandler.variableIsPresentInMethodCall(variableDeclarationExpr, methodCallExpr);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test get variable name with null")
    public void shouldTestGetVariableNameWithNull() {
        var result = assertThrows(VariableDeclarationExpectedException.class,
                () -> astHandler.getVariableName(null));

        assertEquals("Variable declaration is expected as a parameter", result.getMessage());
    }

    @Test
    @DisplayName("Should test get variable")
    public void shouldTestGetVariable() {
        var variableDeclaratorExpr = new VariableDeclarationExpr();
        var variableDeclarator = new VariableDeclarator();
        variableDeclarator.setName("i");
        variableDeclaratorExpr.setVariables(NodeList.nodeList(variableDeclarator));

        var result = astHandler.getVariableName(variableDeclaratorExpr);

        assertEquals("i", result.toString());
    }

    @Test
    @DisplayName("Should test node has simple name with parameters null")
    public void shouldTestNodeHasSimpleNameWithParametersNull() {
        var result = assertThrows(SimpleNameException.class,
                () -> astHandler.nodeHasSimpleName(null, null));

        assertEquals("Simple name not found", result.getMessage());
    }

    @Test
    @DisplayName("Should test node has simple name with node null")
    public void shouldTestNodeHasSimpleNameWithNodeNull() {
        var result = assertThrows(NullNodeException.class,
                () -> astHandler.nodeHasSimpleName(new SimpleName(), null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test node has simple name with name null")
    public void shouldTestNodeHasSimpleNameWithNameNull() {
        var result = assertThrows(SimpleNameException.class,
                () -> astHandler.nodeHasSimpleName(null, new SimpleName()));

        assertEquals("Simple name not found", result.getMessage());
    }

    @Test
    @DisplayName("Should test node has simple name for node without name")
    public void shouldTestNodeHasSimpleNameForNodeWithoutName() {
        var name = new SimpleName();
        var node = new ArrayCreationLevel();

        var result = astHandler.nodeHasSimpleName(name, node);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test node has simple name")
    public void shouldTestNodeHasSimpleName() {
        var name = new SimpleName();
        var node = new MethodDeclaration();

        var result = astHandler.nodeHasSimpleName(name, node);

        assertTrue(result);
    }
}
