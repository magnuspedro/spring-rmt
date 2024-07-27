package br.com.magnus.detection.refactor.dataExtractions.ast.utils;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.exceptions.*;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
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

    @Test
    @DisplayName("Should test method get declared fields for null")
    public void shouldTestMethodGetDeclaredFieldsForNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.getDeclaredFields(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test method get declared fields for no fields")
    public void shouldTestMethodGetDeclaredFieldsForNoFields() {
        var node = new MethodDeclaration();

        var result = AstHandler.getDeclaredFields(node);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test method get declared fields")
    public void shouldTestMethodGetDeclaredFields() {
        var node = new ClassOrInterfaceDeclaration(
                NodeList.nodeList(Modifier.publicModifier()),
                NodeList.nodeList(),
                false,
                new SimpleName("TestClazz"),
                NodeList.nodeList(),
                NodeList.nodeList(),
                NodeList.nodeList(),
                NodeList.nodeList(),
                NodeList.nodeList(new FieldDeclaration()));

        var result = AstHandler.getDeclaredFields(node);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should test get object creation expr with node null")
    public void shouldTestGetObjectCreationExprWithNodeNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.getObjectCreationExpr(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get object creation expr with no object creation expr")
    public void shouldTestGetObjectCreationExprWithNoObjectCreationExpr() {
        var node = new MethodDeclaration();

        var result = AstHandler.getObjectCreationExpr(node);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get object creation expr")
    public void shouldTestGetObjectCreationExpr() {
        var objectCreationExpr = new ObjectCreationExpr();
        var node = new ObjectCreationExpr(null, new ClassOrInterfaceType(), NodeList.nodeList(objectCreationExpr));

        var result = AstHandler.getObjectCreationExpr(node);

        assertEquals(objectCreationExpr, result.get());
    }

    @Test
    @DisplayName("Should test get return stmt null")
    public void shouldTestGetReturnStmtNull() {
        var result = assertThrows(NullIfStmtException.class,
                () -> AstHandler.getReturnStmt(null));

        assertEquals("IfStmt cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get return stmt with no then block")
    public void shouldTestGetReturnStmtWithNoThenBlock() {
        var node = new IfStmt();

        var result = AstHandler.getReturnStmt(node);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get return stmt")
    public void shouldTestGetReturnStmt() {
        var returnStmt = new ReturnStmt();
        var node = new IfStmt(new BooleanLiteralExpr(),
                new BlockStmt(null, NodeList.nodeList(returnStmt)),
                new BlockStmt());

        var result = AstHandler.getReturnStmt(node);

        assertEquals(returnStmt, result.get());
    }

    @Test
    @DisplayName("Should test get name expr for null")
    public void shouldTestGetNameExprForNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.getNameExpr(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get name expr for no child nodes")
    public void shouldTestGetNameExprForNoChildNodes() {
        var node = new SimpleName();

        var result = AstHandler.getNameExpr(node);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test for get name expr with NameExpr")
    public void shouldTestForGetNameExprWithNameExpr() {
        var node = new VariableDeclarator();
        node.setInitializer("method");

        var result = AstHandler.getNameExpr(node);

        assertEquals("method", result.get().toString());
    }

    @Test
    @DisplayName("Should test for get variable simple name with null")
    public void shouldTestForGetVariableSimpleNameWithNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.getVariableSimpleName(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for get variable simple name")
    public void shouldTestForGetVariableSimpleName() {
        var node = new VariableDeclarationExpr(PrimitiveType.intType(), "i");

        var result = AstHandler.getVariableSimpleName(node);

        assertEquals("i", result.get().toString());
    }

    @Test
    @DisplayName("Should test for get simple name with null")
    public void shouldTestForGetSimpleNameWithNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.getSimpleName(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should tet for get simple name")
    public void shouldTetForGetSimpleName() {
        var clazz = new ClassOrInterfaceDeclaration();

        var result = AstHandler.getSimpleName(clazz);

        assertThat(result.get(), instanceOf(SimpleName.class));
        assertEquals("empty", result.get().toString());
    }

    @Test
    @DisplayName("Should test for get parent type with null")
    public void shouldTestForGetParentTypeWithNull() {
        CompilationUnit cu = null;

        var result = assertThrows(NoClassOrInterfaceException.class,
                () -> AstHandler.getParentType(cu));

        assertEquals("No class or interface found in the compilation unit", result.getMessage());
    }

    @Test
    @DisplayName("Should test for get parent from compilation unit")
    public void shouldTestForGetParentFromCompilationUnit() {
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.setImplementedTypes(NodeList.nodeList(new ClassOrInterfaceType("Interface")));
        var cu = new CompilationUnit();
        cu.getTypes().add(clazz);

        var result = AstHandler.getParentType(cu);

        assertEquals("Interface", result.get().getNameAsString());
    }

    @Test
    @DisplayName("Should test for get parent type from class or interface declaration null")
    public void shouldTestForGetParentTypeFromClassOrInterfaceDeclarationNull() {
        ClassOrInterfaceDeclaration clazz = null;

        var result = assertThrows(NoClassOrInterfaceDeclarationException.class,
                () -> AstHandler.getParentType(clazz));

        assertEquals("No class or interface declaration found", result.getMessage());
    }

    @Test
    @DisplayName("Should test for get parent type from class or interface declaration without parent type")
    public void shouldTestForGetParentTypeFromClassOrInterfaceDeclarationWithoutParentType() {
        var clazz = new ClassOrInterfaceDeclaration();

        var result = AstHandler.getParentType(clazz);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test for get parent type from class or interface declaration")
    public void shouldTestForGetParentTypeFromClassOrInterfaceDeclaration() {
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.setParentNode(new ClassOrInterfaceType("Parent"));

        var result = AstHandler.getParentType(clazz);

        assertEquals("Parent", result.get().getNameAsString());
    }

    @Test
    @DisplayName("Should test for get parent from compilation unit and all classes with null")
    public void shouldTestForGetParentFromCompilationUnitAndAllClassesWithNull() {
        var result = assertThrows(NoClassOrInterfaceException.class,
                () -> AstHandler.getParent(null, List.of()));

        assertEquals("No class or interface found in the compilation unit", result.getMessage());
    }

    @Test
    @DisplayName("Should test for get parent without parent class")
    public void shouldTestForGetParentWithoutParentClass() {
        var cu = new CompilationUnit();

        var result = AstHandler.getParent(cu, List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test for get parent with different names")
    public void shouldTestForGetParentWithDifferentNames() {
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.setImplementedTypes(NodeList.nodeList(new ClassOrInterfaceType("Interface")));
        var cu = new CompilationUnit();
        cu.getTypes().add(clazz);

        var result = AstHandler.getParent(cu, List.of(cu));

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

        var result = AstHandler.getParent(cu, cuList);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should test for get parent from compilation unit and parent with null")
    public void shouldTestForGetParentFromCompilationUnitAndParentWithNull() {
        CompilationUnit parent = null;
        var result = assertThrows(NoClassOrInterfaceException.class,
                () -> AstHandler.getParent(null, parent));

        assertEquals("No class or interface found in the compilation unit", result.getMessage());
    }

    @Test
    @DisplayName("Should test for get parent without cu parent class")
    public void shouldTestForGetParentWithoutCUParentClass() {
        var cu = new CompilationUnit();
        CompilationUnit parent = null;


        var result = AstHandler.getParent(cu, parent);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test for get single parent with different names")
    public void shouldTestForGetSingleParentWithDifferentNames() {
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.setImplementedTypes(NodeList.nodeList(new ClassOrInterfaceType("Interface")));
        var cu = new CompilationUnit();
        cu.getTypes().add(clazz);

        var result = AstHandler.getParent(cu, cu);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test for get parent with single parent class")
    public void shouldTestForGetParentWithSingleParentClass() {
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.setImplementedTypes(NodeList.nodeList(new ClassOrInterfaceType("Interface")));
        var cu = new CompilationUnit();
        cu.getTypes().add(clazz);
        var cuList = List.of(new CompilationUnit());
        var clazzDeclaration = new ClassOrInterfaceDeclaration();
        clazzDeclaration.setName("Interface");
        cuList.get(0).getTypes().add(clazzDeclaration);

        var result = AstHandler.getParent(cu, cuList);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should test for get package declaration null")
    public void shouldTestForGetPackageDeclarationNull() {
        var result = assertThrows(NullCompilationUnitException.class,
                () -> AstHandler.getPackageDeclaration(null));

        assertEquals("Compilation unit cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for get package declaration without package declaration")
    public void shouldTestForGetPackageDeclarationWithoutPackageDeclaration() {
        var cu = new CompilationUnit();

        var result = assertThrows(NoPackageDeclarationException.class,
                () -> AstHandler.getPackageDeclaration(cu));

        assertEquals("Package declaration not be found", result.getMessage());
    }

    @Test
    @DisplayName("Should test for get package declaration with package declaration")
    public void shouldTestForGetPackageDeclarationWithPackageDeclaration() {
        var cu = new CompilationUnit();
        cu.setPackageDeclaration("br.com.detection.detectionagent.domain.dataExtractions.ast.utils");

        var result = AstHandler.getPackageDeclaration(cu);

        assertEquals("br.com.detection.detectionagent.domain.dataExtractions.ast.utils", result.getNameAsString());
    }

    @Test
    @DisplayName("Should test get class or interface declaration with null")
    public void shouldTestGetClassOrInterfaceDeclarationWithNull() {
        var result = assertThrows(NoClassOrInterfaceException.class,
                () -> AstHandler.getClassOrInterfaceDeclaration(null));

        assertEquals("No class or interface found in the compilation unit", result.getMessage());
    }

    @Test
    @DisplayName("Should test get class or interface declaration")
    public void shouldTestGetClassOrInterfaceDeclaration() {
        var clazz = new ClassOrInterfaceDeclaration();
        var cu = new CompilationUnit();
        cu.getTypes().add(clazz);

        var result = AstHandler.getClassOrInterfaceDeclaration(cu);

        assertEquals(clazz, result.get());
    }

    @Test
    @DisplayName("Should test get methods with null")
    public void shouldTestGetMethodsWithNull() {
        CompilationUnit cu = null;

        var result = assertThrows(NullCompilationUnitException.class,
                () -> AstHandler.getMethods(cu));

        assertEquals("Compilation unit cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get methods without methods")
    public void shouldTestGetMethodsWithoutMethods() {
        var cu = new CompilationUnit();

        var result = AstHandler.getMethods(cu);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get methods from compilation unit")
    public void shouldTestGetMethodsFromCompilationUnit() {
        var cu = new CompilationUnit();
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.addMethod("method");
        cu.getTypes().add(clazz);

        var result = AstHandler.getMethods(cu);

        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get methods with null for Class")
    public void shouldTestGetMethodsWithNullForClass() {
        ClassOrInterfaceDeclaration cu = null;

        var result = assertThrows(NoClassOrInterfaceException.class,
                () -> AstHandler.getMethods(cu));

        assertEquals("No class or interface found in the compilation unit", result.getMessage());
    }

    @Test
    @DisplayName("Should test get methods without methods for Class")
    public void shouldTestGetMethodsWithoutMethodsForClass() {
        var cu = new ClassOrInterfaceDeclaration();

        var result = AstHandler.getMethods(cu);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get methods from compilation unit for class")
    public void shouldTestGetMethodsFromCompilationUnitForClass() {
        var cu = new CompilationUnit();
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.addMethod("method");
        cu.getTypes().add(clazz);

        var result = AstHandler.getMethods(cu);

        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get block statement with null")
    public void shouldTestGetBlockStatementWithNull() {
        var result = AstHandler.getBlockStatement(null);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get block statement without block statement")
    public void shouldTestGetBlockStatementWithoutBlockStatement() {
        var node = new ClassOrInterfaceDeclaration();

        var result = AstHandler.getBlockStatement(node);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get block statement")
    public void shouldTestGetBlockStatement() {
        var node = new MethodDeclaration();

        var result = AstHandler.getBlockStatement(node);

        assertThat(result.get(), instanceOf(BlockStmt.class));
    }

    @Test
    @DisplayName("Should test get expression statement with null")
    public void shouldTestGetExpressionStatementWithNull() {
        var result = AstHandler.getExpressionStatement(null);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get expression statement for blockstmt")
    public void shouldTestGetExpressionStatementForBlockstmt() {
        var node = new BlockStmt();

        var result = AstHandler.getExpressionStatement(node);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get expression statement for class or interface declaration")
    public void shouldTestGetExpressionStatementForClassOrInterfaceDeclaration() {
        var node = new ClassOrInterfaceDeclaration();

        var result = AstHandler.getExpressionStatement(node);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get expression statement for expression statement")
    public void shouldTestGetExpressionStatementForExpressionStatement() {
        var node = new ExpressionStmt();

        var result = AstHandler.getExpressionStatement(node);

        assertThat(result.get(), instanceOf(ExpressionStmt.class));
    }

    @Test
    @DisplayName("Should test get expression statement for a parent node")
    public void shouldTestGetExpressionStatementForAParentNode() {
        var node = new IfStmt();
        node.setParentNode(new ExpressionStmt());

        var result = AstHandler.getExpressionStatement(node);

        assertThat(result.get(), instanceOf(ExpressionStmt.class));
    }

    @Test
    @DisplayName("Should test get super calls with null")
    public void shouldTestGetSuperCallsWithNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.getSuperCalls(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get super calls for SuperExpr")
    public void shouldTestGetSuperCallsForSuperExpr() {
        var node = new SuperExpr();

        var result = AstHandler.getSuperCalls(node);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should test get super calls for child")
    public void shouldTestGetSuperCallsForChild() {
        var node = new BlockStmt(NodeList.nodeList(new ExpressionStmt(new SuperExpr()),
                new ExpressionStmt(new SuperExpr())));

        var result = AstHandler.getSuperCalls(node);

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should test retrieve overridden method for both parameters null")
    public void shouldTestRetrieveOverriddenMethodForBothParametersNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.retrieveOverriddenMethod(null, null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test retrieve overridden method for overridden method null")
    public void shouldTestRetrieveOverriddenMethodForOverriddenMethodNull() {
        var cu = new CompilationUnit();

        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.retrieveOverriddenMethod(cu, null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test retrieve overridden method for parent null")
    public void shouldTestRetrieveOverriddenMethodForParentNull() {
        var method = new MethodDeclaration();

        var result = assertThrows(NullCompilationUnitException.class,
                () -> AstHandler.retrieveOverriddenMethod(null, method));

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

        var result = AstHandler.retrieveOverriddenMethod(cu, overriddenMethod);

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

        var result = AstHandler.retrieveOverriddenMethod(cu, overriddenMethod);

        assertNotNull(result);
        assertEquals("metodo", result.getNameAsString());
    }

    @Test
    @DisplayName("Should test methods params match for null")
    public void shouldTestMethodsParamsMatchForNull() {
        var result = assertThrows(NullMethodException.class,
                () -> AstHandler.methodsParamsMatch(null, null));

        assertEquals("Method cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test methods params match for m1 null")
    public void shouldTestMethodsParamsMatchForM1Null() {
        var result = assertThrows(NullMethodException.class,
                () -> AstHandler.methodsParamsMatch(new MethodDeclaration(), null));

        assertEquals("Method cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test methods params match for m2 null")
    public void shouldTestMethodsParamsMatchForM2Null() {
        var result = assertThrows(NullMethodException.class,
                () -> AstHandler.methodsParamsMatch(null, new MethodDeclaration()));

        assertEquals("Method cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test methods params match for different methods")
    public void shouldTestMethodsParamsMatchForDifferentMethods() {
        var m1 = new MethodDeclaration();
        m1.setParameters(NodeList.nodeList(new Parameter(PrimitiveType.intType(), "i")));
        var m2 = new MethodDeclaration();

        var result = AstHandler.methodsParamsMatch(m1, m2);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test methods params match for same methods")
    public void shouldTestMethodsParamsMatchForSameMethods() {
        var m1 = new MethodDeclaration();
        m1.setParameters(NodeList.nodeList(new Parameter(PrimitiveType.intType(), "i")));
        var m2 = new MethodDeclaration();
        m2.setParameters(NodeList.nodeList(new Parameter(PrimitiveType.intType(), "i")));

        var result = AstHandler.methodsParamsMatch(m1, m2);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test child has direct super call for null params")
    public void shouldTestChildHasDirectSuperCallForNullParams() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.childHasDirectSuperCall(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test child has direct super call for node with condition")
    public void shouldTestChildHasDirectSuperCallForNodeWithCondition() {
        var node = new IfStmt();

        var result = AstHandler.childHasDirectSuperCall(node);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test child has direct super call for node with try stmt")
    public void shouldTestChildHasDirectSuperCallForNodeWithTryStmt() {
        var node = new TryStmt();

        var result = AstHandler.childHasDirectSuperCall(node);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test child has direct super call for node with catch clause")
    public void shouldTestChildHasDirectSuperCallForNodeWithCatchClause() {
        var node = new CatchClause();

        var result = AstHandler.childHasDirectSuperCall(node);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test child has direct super call for node with without method call")
    public void shouldTestChildHasDirectSuperCallForNodeWithWithoutMethodCall() {
        var node = new BlockStmt();

        var result = AstHandler.childHasDirectSuperCall(node);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test child has direct super call for node with method call")
    public void shouldTestChildHasDirectSuperCallForNodeWithMethodCall() {
        var node = new ExpressionStmt(new SuperExpr());

        var result = AstHandler.childHasDirectSuperCall(node);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test child has direct super call for node with method call in child")
    public void shouldTestChildHasDirectSuperCallForNodeWithMethodCallInChild() {
        var node = new BlockStmt(NodeList.nodeList(new ExpressionStmt(new SuperExpr())));

        var result = AstHandler.childHasDirectSuperCall(node);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test node has return statement with null")
    public void shouldTestNodeHasReturnStatementWithNull() {
        var result = AstHandler.nodeHasReturnStatement(null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test node has return statement with no return statement")
    public void shouldTestNodeHasReturnStatementWithNoReturnStatement() {
        var result = AstHandler.nodeHasReturnStatement(new BlockStmt());

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test  node has return statement")
    public void shouldTestNodeHasReturnStatement() {
        var blockStmt = new BlockStmt(NodeList.nodeList(new ReturnStmt()));

        var result = AstHandler.nodeHasReturnStatement(blockStmt);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test node throws exception with null")
    public void shouldTestNodeThrowsExceptionWithNull() {
        var result = AstHandler.nodeThrowsException(null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test node throws exception with no throw")
    public void shouldTestNodeThrowsExceptionWithNoThrow() {
        var result = AstHandler.nodeThrowsException(new BlockStmt());

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test node throws exception")
    public void shouldTestNodeThrowsException() {
        var blockStmt = new BlockStmt(NodeList.nodeList(new ThrowStmt()));

        var result = AstHandler.nodeThrowsException(blockStmt);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test for node has clazz with null")
    public void shouldTestForNodeHasClazzWithNull() {
        var result = assertThrows(ClassExpectedException.class,
                () -> AstHandler.nodeHasClazz(null, null));

        assertEquals("Class is expected as a parameter", result.getMessage());
    }

    @Test
    @DisplayName("Should test for node has clazz with null clazz")
    public void shouldTestForNodeHasClazzWithNullClazz() {
        var result = assertThrows(ClassExpectedException.class,
                () -> AstHandler.nodeHasClazz(new ClassOrInterfaceDeclaration(), null));

        assertEquals("Class is expected as a parameter", result.getMessage());
    }


    @Test
    @DisplayName("Should test for node has clazz with null node")
    public void shouldTestForNodeHasClazzWithNullNode() {
        var result = AstHandler.nodeHasClazz(null, ClassOrInterfaceDeclaration.class);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test for node has clazz")
    public void shouldTestForNodeHasClazz() {
        var clazz = new ClassOrInterfaceDeclaration();

        var result = AstHandler.nodeHasClazz(clazz, ClassOrInterfaceDeclaration.class);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test extract variable dclr from node with null")
    public void shouldTestExtractVariableDclrFromNodeWithNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.extractVariableDclrFromNode(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test extract variable dclr from node with no variable declaration")
    public void shouldTestExtractVariableDclrFromNodeWithNoVariableDeclaration() {
        var result = AstHandler.extractVariableDclrFromNode(new BlockStmt());

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

        var result = AstHandler.extractVariableDclrFromNode(blockStmt);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should test variable is present in method call with both parameters null")
    public void shouldTestVariableIsPresentInMethodCallWithBothParametersNull() {
        var result = assertThrows(MethodCallExpectedException.class,
                () -> AstHandler.variableIsPresentInMethodCall(null, null));

        assertEquals("Method is expected as a parameter", result.getMessage());
    }

    @Test
    @DisplayName("Should test variable is present in method call with null method call")
    public void shouldTestVariableIsPresentInMethodCallWithNullMethodCall() {
        var result = assertThrows(MethodCallExpectedException.class,
                () -> AstHandler.variableIsPresentInMethodCall(new VariableDeclarationExpr(), null));

        assertEquals("Method is expected as a parameter", result.getMessage());
    }

    @Test
    @DisplayName("Should test variable is present in method call with null variable declaration exp")
    public void shouldTestVariableIsPresentInMethodCallWithNullVariableDeclarationExp() {
        var result = AstHandler.variableIsPresentInMethodCall(null, new MethodCallExpr());

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test variable is present in method call")
    public void shouldTestVariableIsPresentInMethodCall() {
        var variableDeclarationExpr = new VariableDeclarationExpr();
        var variableDeclarator = new VariableDeclarator(PrimitiveType.intType(), "i");
        variableDeclarationExpr.setVariables(NodeList.nodeList(variableDeclarator));
        var methodCallExpr = new MethodCallExpr("method", new NameExpr("i"));

        var result = AstHandler.variableIsPresentInMethodCall(variableDeclarationExpr, methodCallExpr);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test get variable name with null")
    public void shouldTestGetVariableNameWithNull() {
        var result = assertThrows(VariableDeclarationExpectedException.class,
                () -> AstHandler.getVariableName(null));

        assertEquals("Variable declaration is expected as a parameter", result.getMessage());
    }

    @Test
    @DisplayName("Should test get variable")
    public void shouldTestGetVariable() {
        var variableDeclaratorExpr = new VariableDeclarationExpr();
        var variableDeclarator = new VariableDeclarator();
        variableDeclarator.setName("i");
        variableDeclaratorExpr.setVariables(NodeList.nodeList(variableDeclarator));

        var result = AstHandler.getVariableName(variableDeclaratorExpr);

        assertEquals("i", result.toString());
    }

    @Test
    @DisplayName("Should test node has simple name with parameters null")
    public void shouldTestNodeHasSimpleNameWithParametersNull() {
        var result = assertThrows(SimpleNameException.class,
                () -> AstHandler.nodeHasSimpleName(null, null));

        assertEquals("Simple name not found", result.getMessage());
    }

    @Test
    @DisplayName("Should test node has simple name with node null")
    public void shouldTestNodeHasSimpleNameWithNodeNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.nodeHasSimpleName(new SimpleName(), null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test node has simple name with name null")
    public void shouldTestNodeHasSimpleNameWithNameNull() {
        var result = assertThrows(SimpleNameException.class,
                () -> AstHandler.nodeHasSimpleName(null, new SimpleName()));

        assertEquals("Simple name not found", result.getMessage());
    }

    @Test
    @DisplayName("Should test node has simple name for node without name")
    public void shouldTestNodeHasSimpleNameForNodeWithoutName() {
        var name = new SimpleName();
        var node = new ArrayCreationLevel();

        var result = AstHandler.nodeHasSimpleName(name, node);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test node has simple name")
    public void shouldTestNodeHasSimpleName() {
        var name = new SimpleName();
        var node = new MethodDeclaration();

        var result = AstHandler.nodeHasSimpleName(name, node);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test get node by type with both parameters null")
    public void shouldTestGetNodeByTypeWithBothParametersNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.getNodeByType(null, null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get node by type with type null")
    public void shouldTestGetNodeByTypeWithTypeNull() {
        var result = assertThrows(ClassExpectedException.class,
                () -> AstHandler.getNodeByType(new BlockStmt(), null));

        assertEquals("Class is expected as a parameter", result.getMessage());
    }

    @Test
    @DisplayName("Should test get node by type with node null")
    public void shouldTestGetNodeByTypeWithNodeNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.getNodeByType(null, ClassOrInterfaceDeclaration.class));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get node by type for node without type")
    public void shouldTestGetNodeByTypeForNodeWithoutType() {
        var result = AstHandler.getNodeByType(new BlockStmt(),
                ClassOrInterfaceDeclaration.class);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get node by type for node with existing type")
    public void shouldTestGetNodeByTypeForNodeWithExistingType() {
        var result = AstHandler.getNodeByType(new ClassOrInterfaceDeclaration(),
                SimpleName.class);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should test get method call expr with null node")
    public void shouldTestGetMethodCallExprWithNullNode() {
        var result = AstHandler.getMethodCallExpr(null);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get method call expr with no method call expr")
    public void shouldTestGetMethodCallExprWithNoMethodCallExpr() {
        var result = AstHandler.getMethodCallExpr(new BlockStmt());

        assertTrue(result.isEmpty());
    }


    @Test
    @DisplayName("Should test get method call expr for MethodCallExpr")
    public void shouldTestGetMethodCallExprForMethodCallExpr() {
        var result = AstHandler.getMethodCallExpr(new MethodCallExpr());

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should tet get method call expr for child")
    public void shouldTetGetMethodCallExprForChild() {
        var cu = new CompilationUnit();
        var clazz = new ClassOrInterfaceDeclaration();
        var method = new MethodDeclaration();
        var body = new BlockStmt();
        var expression = new ExpressionStmt();
        var methodCallExpr = new MethodCallExpr();
        expression.setExpression(methodCallExpr);
        body.setStatements(NodeList.nodeList(expression));
        method.setBody(body);
        clazz.setMembers(NodeList.nodeList(method));
        cu.getTypes().add(clazz);

        var result = AstHandler.getMethodCallExpr(methodCallExpr);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should test do variable name match with both parameters null")
    public void shouldTestDoVariableNameMatchWithBothParametersNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.doVariablesNameMatch(null, null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test do variable name match with first parameter null")
    public void shouldTestDoVariableNameMatchWithFirstParameterNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.doVariablesNameMatch(null, new VariableDeclarationExpr()));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test do variable name match with second parameter null")
    public void shouldTestDoVariableNameMatchWithSecondParameterNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.doVariablesNameMatch(new VariableDeclarationExpr(), null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test do variable name match for different names")
    public void shouldTestDoVariableNameMatchForDifferentNames() {
        var var1 = new VariableDeclarationExpr();
        var1.setVariables(NodeList.nodeList(new VariableDeclarator(PrimitiveType.intType(), "i")));
        var var2 = new VariableDeclarationExpr();

        var result = AstHandler.doVariablesNameMatch(var1, var2);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test do variable name match for same names")
    public void shouldTestDoVariableNameMatchForSameNames() {
        var var1 = new VariableDeclarationExpr();
        var1.setVariables(NodeList.nodeList(new VariableDeclarator(PrimitiveType.intType(), "i")));
        var var2 = new VariableDeclarationExpr();
        var2.setVariables(NodeList.nodeList(new VariableDeclarator(PrimitiveType.intType(), "i")));

        var result = AstHandler.doVariablesNameMatch(var1, var2);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test does node contain matching method with both parameters null")
    public void shouldTestDoesNodeContainMatchingMethodWithBothParametersNull() {
        var result = AstHandler.doesNodeContainMatchingMethodCall(null, null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test does node contain matching method with second parameter null")
    public void shouldTestDoesNodeContainMatchingMethodWithSecondParameterNull() {
        var result = assertThrows(NullMethodException.class,
                () -> AstHandler.doesNodeContainMatchingMethodCall(new MethodCallExpr(), null));

        assertEquals("Method cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test does node contain matching method with first parameter null")
    public void shouldTestDoesNodeContainMatchingMethodWithFirstParameterNull() {
        var result = AstHandler.doesNodeContainMatchingMethodCall(null, new MethodCallExpr());

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test does node contain matching method call for methods that do not match")
    public void shouldTestDoesNodeContainMatchingMethodCallForMethodsThatDoNotMatch() {
        var cu = new CompilationUnit();
        var method = new MethodCallExpr("method", new VariableDeclarationExpr());

        var result = AstHandler.doesNodeContainMatchingMethodCall(cu, method);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test does node contain matching method call for methods that match")
    public void shouldTestDoesNodeContainMatchingMethodCallForMethodsThatMatch() {
        var body = new BlockStmt();
        var method = new MethodCallExpr();
        body.setStatements(NodeList.nodeList(new ExpressionStmt(new MethodCallExpr())));

        var result = AstHandler.doesNodeContainMatchingMethodCall(body, method);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test does method calls match for both parameters null")
    public void shouldTestDoesMethodCallsMatchForBothParametersNull() {
        var result = assertThrows(NullMethodException.class,
                () -> astHandler.doesMethodCallsMatch(null, null));

        assertEquals("Method cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test does method calls match for first parameter null")
    public void shouldTestDoesMethodCallsMatchForFirstParameterNull() {
        var result = assertThrows(NullMethodException.class,
                () -> astHandler.doesMethodCallsMatch(null, new MethodCallExpr()));

        assertEquals("Method cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test does method calls match for second parameter null")
    public void shouldTestDoesMethodCallsMatchForSecondParameterNull() {
        var result = assertThrows(NullMethodException.class,
                () -> astHandler.doesMethodCallsMatch(new MethodCallExpr(), null));

        assertEquals("Method cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test does method calls match for different methods")
    public void shouldTestDoesMethodCallsMatchForDifferentMethods() {
        var method1 = new MethodCallExpr("method1", new VariableDeclarationExpr());
        var method2 = new MethodCallExpr("method1");

        var result = astHandler.doesMethodCallsMatch(method1, method2);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test does method call match for different names")
    public void shouldTestDoesMethodCallMatchForDifferentNames() {
        var method1 = new MethodCallExpr("method1");
        var method2 = new MethodCallExpr("method2");

        var result = astHandler.doesMethodCallsMatch(method1, method2);

        assertFalse(result);
    }


    @Test
    @DisplayName("Should test does method call match for same methods")
    public void shouldTestDoesMethodCallMatchForSameMethods() {
        var method1 = new MethodCallExpr("method1", new VariableDeclarationExpr());
        var method2 = new MethodCallExpr("method1", new VariableDeclarationExpr());

        var result = astHandler.doesMethodCallsMatch(method1, method2);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test does compilation unit match with both parameters null")
    public void shouldTestDoesCompilationUnitMatchWithBothParametersNull() {
        var result = assertThrows(NoClassOrInterfaceException.class,
                () -> AstHandler.doesCompilationUnitsMatch(null, null));

        assertEquals("No class or interface found in the compilation unit", result.getMessage());
    }

    @Test
    @DisplayName("Should test does compilation unit match for first parameter null")
    public void shouldTestDoesCompilationUnitMatchForFirstParameterNull() {
        var result = assertThrows(NoClassOrInterfaceException.class,
                () -> AstHandler.doesCompilationUnitsMatch(null, new CompilationUnit()));

        assertEquals("No class or interface found in the compilation unit", result.getMessage());
    }

    @Test
    @DisplayName("Should test does compilation unit match for second parameter null")
    public void shouldTestDoesCompilationUnitMatchForSecondParameterNull() {
        var result = assertThrows(NoClassOrInterfaceException.class,
                () -> AstHandler.doesCompilationUnitsMatch(new CompilationUnit(), null));

        assertEquals("No class or interface found in the compilation unit", result.getMessage());
    }

    @Test
    @DisplayName("Should test does compilation unit match for different name")
    public void shouldTestDoesCompilationUnitMatchForDifferentName() {
        var cu1 = new CompilationUnit();
        cu1.setPackageDeclaration("br.com.test");
        var cu2 = new CompilationUnit();

        var result = AstHandler.doesCompilationUnitsMatch(cu1, cu2);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test does compilation unit for different classes")
    public void shouldTestDoesCompilationUnitForDifferentClasses() {
        var cu1 = new CompilationUnit();
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.setName("Class");
        cu1.getTypes().add(clazz);
        cu1.setPackageDeclaration("br.com.test");
        var cu2 = new CompilationUnit();
        cu2.setPackageDeclaration("br.com.test");

        var result = AstHandler.doesCompilationUnitsMatch(cu1, cu2);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test does compilation unit for sem classes")
    public void shouldTestDoesCompilationUnitForSemClasses() {
        var cu1 = new CompilationUnit();
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.setName("Class");
        cu1.getTypes().add(clazz);
        cu1.setPackageDeclaration("br.com.test");
        var cu2 = new CompilationUnit();
        var clazz2 = new ClassOrInterfaceDeclaration();
        clazz2.setName("Class");
        cu2.getTypes().add(clazz2);
        cu2.setPackageDeclaration("br.com.test");

        var result = AstHandler.doesCompilationUnitsMatch(cu1, cu2);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test get if statement with null")
    public void shouldTestGetIfStatementWithNull() {
        var result = assertThrows(NullMethodException.class,
                () -> AstHandler.getIfStatements(null));

        assertEquals("Method cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get if statement for method declaration without ifStmt")
    public void shouldTestGetIfStatementForMethodDeclarationWithoutIfStmt() {
        var result = AstHandler.getIfStatements(new MethodDeclaration());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get if statement for method declaration with ifStmt")
    public void shouldTestGetIfStatementForMethodDeclarationWithIfStmt() {
        var method = new MethodDeclaration();
        method.setBody(new BlockStmt(NodeList.nodeList(new IfStmt())));

        var result = AstHandler.getIfStatements(method);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should test get if statement for method declaration with two ifStmt")
    public void shouldTestGetIfStatementForMethodDeclarationWithTwoIfStmt() {
        var method = new MethodDeclaration();
        method.setBody(new BlockStmt(NodeList.nodeList(new IfStmt(), new IfStmt())));

        var result = AstHandler.getIfStatements(method);

        assertEquals(2, result.size());
    }


    @Test
    @DisplayName("Should test get if statement for method declaration with inner ifStmt")
    public void shouldTestGetIfStatementForMethodDeclarationWithInnerIfStmt() {
        var method = new MethodDeclaration();
        var ifStmt = new IfStmt();
        ifStmt.setElseStmt(new IfStmt());
        method.setBody(new BlockStmt(NodeList.nodeList(ifStmt)));

        var result = AstHandler.getIfStatements(method);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should test get literal expr with null")
    public void shouldTestGetLiteralExprWithNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.getLiteralExpr(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get literal expr for node without literal expr")
    public void shouldTestGetLiteralExprForNodeWithoutLiteralExpr() {
        var result = AstHandler.getLiteralExpr(new BlockStmt());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get literal expr for node with literal expr")
    public void shouldTestGetLiteralExprForNodeWithLiteralExpr() {
        var expression = new ExpressionStmt();
        expression.setExpression(new BooleanLiteralExpr());
        var result = AstHandler.getLiteralExpr(expression);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should test get variable declaration in node with both parameters null")
    public void shouldTestGetVariableDeclarationInNodeWithBothParametersNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.getVariableDeclarationInNode(null, null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get variable declaration in node with first param null")
    public void shouldTestGetVariableDeclarationInNodeWithFirstParamNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.getVariableDeclarationInNode(null, ""));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get variable declaration in node with second param null")
    public void shouldTestGetVariableDeclarationInNodeWithSecondParamNull() {
        var result = assertThrows(IllegalArgumentException.class,
                () -> AstHandler.getVariableDeclarationInNode(new BlockStmt(), null));

        assertEquals("Return name cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get variable declaration in node for no match")
    public void shouldTestGetVariableDeclarationInNodeForNoMatch() {
        var result = AstHandler.getVariableDeclarationInNode(new VariableDeclarationExpr(), "");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get variable declaration in node with match for name")
    public void shouldTestGetVariableDeclarationInNodeWithMatchForName() {
        var variable = new VariableDeclarationExpr();
        variable.setVariables(NodeList.nodeList(new VariableDeclarator(PrimitiveType.intType(), "i")));

        var result = AstHandler.getVariableDeclarationInNode(variable, "i");

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should test get variable declaration in node with match for name in child")
    public void shouldTestGetVariableDeclarationInNodeWithMatchForNameInChild() {
        var expression = new ExpressionStmt();
        var variable = new VariableDeclarationExpr();
        variable.setVariables(NodeList.nodeList(new VariableDeclarator(PrimitiveType.intType(), "i")));
        expression.setExpression(variable);

        var result = AstHandler.getVariableDeclarationInNode(expression, "i");

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should test for get method return class type for null")
    public void shouldTestForGetMethodReturnClassTypeForNull() {
        var result = AstHandler.getMethodReturnClassType(null);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test for get method return class type")
    public void shouldTestForGetMethodReturnClassType() {
        var method = new MethodDeclaration();
        method.setType(new ClassOrInterfaceType("Custom"));

        var result = AstHandler.getMethodReturnClassType(method);

        assertEquals("Custom", result.get().toString());
    }

    @Test
    @DisplayName("Should test for does node uses var with both null")
    public void shouldTestForDoesNodeUsesVarWithBothNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.doesNodeUsesVar(null, null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for does node uses var with first null")
    public void shouldTestForDoesNodeUsesVarWithFirstNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.doesNodeUsesVar(null, new VariableDeclarator()));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test for does node uses var with second null")
    public void shouldTestForDoesNodeUsesVarWithSecondNull() {
        var result = assertThrows(VariableDeclarationExpectedException.class,
                () -> AstHandler.doesNodeUsesVar(new BlockStmt(), null));

        assertEquals("Variable declaration is expected as a parameter", result.getMessage());
    }

    @Test
    @DisplayName("Should test does node uses var for no instance of nameExpr")
    public void shouldTestDoesNodeUsesVarForNoInstanceOfNameExpr() {
        var result = AstHandler.doesNodeUsesVar(new BlockStmt(), new VariableDeclarator());

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test does node uses var for different names")
    public void shouldTestDoesNodeUsesVarForDifferentNames() {
        var nameExpr = new NameExpr("i");
        var variable = new VariableDeclarator();
        variable.setName("io");

        var result = AstHandler.doesNodeUsesVar(nameExpr, variable);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should test does node uses var for same name")
    public void shouldTestDoesNodeUsesVarForSameName() {
        var nameExpr = new NameExpr("i");
        var variable = new VariableDeclarator();
        variable.setName("i");

        var result = AstHandler.doesNodeUsesVar(nameExpr, variable);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test does node uses var for same name in child")
    public void shouldTestDoesNodeUsesVarForSameNameInChild() {
        var blockStmt = new BlockStmt(NodeList.nodeList(new ExpressionStmt(new NameExpr("i"))));
        var variable = new VariableDeclarator();
        variable.setName("i");

        var result = AstHandler.doesNodeUsesVar(blockStmt, variable);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should test get variable declarations with param null")
    public void shouldTestGetVariableDeclarationsWithParamNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.getVariableDeclarations(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test get variable declaration for a node that is not a variable declarator")
    public void shouldTestGetVariableDeclarationForANodeThatIsNotAVariableDeclarator() {
        var result = AstHandler.getVariableDeclarations(new BlockStmt());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test get variable declaration for node that is variable declarator")
    public void shouldTestGetVariableDeclarationForNodeThatIsVariableDeclarator() {
        var variableDeclarator = new VariableDeclarator();

        var result = AstHandler.getVariableDeclarations(variableDeclarator);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should test get variable declaration for node that is variable declarator in child")
    public void shouldTestGetVariableDeclarationForNodeThatIsVariableDeclaratorInChild() {
        var variable = new VariableDeclarationExpr();
        variable.setVariables(NodeList.nodeList(new VariableDeclarator(), new VariableDeclarator()));

        var result = AstHandler.getVariableDeclarations(variable);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should test class get method by name with both parameters null")
    public void shouldTestClassGetMethodByNameWithBothParametersNull() {
        ClassOrInterfaceDeclaration clazz = null;

        assertThrows(NoClassOrInterfaceException.class,
                () -> AstHandler.getMethodByName(clazz, null));
    }

    @Test
    @DisplayName("Should test class get method by name with clazz null")
    public void shouldTestClassGetMethodByNameWithClazzNull() {
        ClassOrInterfaceDeclaration clazz = null;

        assertThrows(NoClassOrInterfaceException.class,
                () -> AstHandler.getMethodByName(clazz, "test"));
    }

    @Test
    @DisplayName("Should test class get method by name with name null")
    public void shouldTestClassGetMethodByNameWithNameNull() {
        var clazz = new ClassOrInterfaceDeclaration();

        var result = AstHandler.getMethodByName(clazz, null);

        assertNull(result);
    }

    @Test
    @DisplayName("Should test class get method by name for no match")
    public void shouldTestClassGetMethodByNameForNoMatch() {
        var clazz = new ClassOrInterfaceDeclaration();

        var result = AstHandler.getMethodByName(clazz, "method");

        assertNull(result);
    }

    @Test
    @DisplayName("Should test class get method by name for match")
    public void shouldTestClassGetMethodByNameForMatch() {
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.setMembers(NodeList.nodeList(new MethodDeclaration()));

        var result = AstHandler.getMethodByName(clazz, "empty");

        assertEquals(result.getNameAsString(), "empty");
    }

    @Test
    @DisplayName("Should test compilation unit get method by name with both parameters null")
    public void shouldTestCompilationUnitGetMethodByNameWithBothParametersNull() {
        CompilationUnit clazz = null;

        assertThrows(NullCompilationUnitException.class,
                () -> AstHandler.getMethodByName(clazz, null));
    }

    @Test
    @DisplayName("Should test compilation unit get method by name with clazz null")
    public void shouldTestCompilationUnitGetMethodByNameWithClazzNull() {
        CompilationUnit clazz = null;

        assertThrows(NullCompilationUnitException.class,
                () -> AstHandler.getMethodByName(clazz, "test"));
    }

    @Test
    @DisplayName("Should test compilation unit get method by name with name null")
    public void shouldTestCompilationUnitGetMethodByNameWithNameNull() {
        var clazz = new CompilationUnit();

        var result = AstHandler.getMethodByName(clazz, null);

        assertNull(result);
    }

    @Test
    @DisplayName("Should test compilation unit get method by name for no match")
    public void shouldTestCompilationUnitGetMethodByNameForNoMatch() {
        var clazz = new CompilationUnit();

        var result = AstHandler.getMethodByName(clazz, "method");

        assertNull(result);
    }

    @Test
    @DisplayName("Should test compilation Unit get method by name for match")
    public void shouldTestCompilationUnitGetMethodByNameForMatch() {
        var cu = new CompilationUnit();
        var clazz = new ClassOrInterfaceDeclaration();
        clazz.setMembers(NodeList.nodeList(new MethodDeclaration()));
        cu.getTypes().add(clazz);

        var result = AstHandler.getMethodByName(clazz, "empty");

        assertEquals(result.getNameAsString(), "empty");
    }

    @Test
    @DisplayName("Should test getObjectCreationExprList with null")
    public void shouldTestGetObjectCreationExprListWithNull() {
        var result = assertThrows(NullNodeException.class,
                () -> AstHandler.getObjectCreationExprList(null));

        assertEquals("Node cannot be null", result.getMessage());
    }

    @Test
    @DisplayName("Should test getObjectCreationExprList for node without object creation expr")
    public void shouldTestGetObjectCreationExprListForNodeWithoutObjectCreationExpr() {
        var cu = new CompilationUnit();

        var result = AstHandler.getObjectCreationExprList(cu);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should test getObjectCreationExprList for node with object creation expr")
    public void shouldTestGetObjectCreationExprListForNodeWithObjectCreationExpr() {
        var expression = new ExpressionStmt();
        expression.setExpression(new ObjectCreationExpr());

        var result = AstHandler.getObjectCreationExprList(expression);


        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should test getVariableDeclarator for node")
    public void shouldTestGetVariableDeclaratorForNode() {
        var variableDeclarator = new VariableDeclarator();
        var variableDeclarationExpr = new VariableDeclarationExpr(variableDeclarator);

        var result = AstHandler.getVariableDeclarator(variableDeclarationExpr.getChildNodes());

        assertEquals(result, variableDeclarator);
    }

    @Test
    @DisplayName("Should test getVariableDeclarator for null")
    public void shouldTestGetVariableDeclaratorForNull() {
        assertThrows(VariableDeclarationExpectedException.class,() -> AstHandler.getVariableDeclarator(null)) ;
    }
}