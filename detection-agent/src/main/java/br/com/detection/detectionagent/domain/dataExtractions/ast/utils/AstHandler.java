package br.com.detection.detectionagent.domain.dataExtractions.ast.utils;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithCondition;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AstHandler {

    public static Collection<FieldDeclaration> getDeclaredFields(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(FieldDeclaration.class::isInstance)
                .map(FieldDeclaration.class::cast)
                .collect(Collectors.toList());
    }

    public static Optional<ObjectCreationExpr> getObjectCreationExpr(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(ObjectCreationExpr.class::isInstance)
                .map(ObjectCreationExpr.class::cast)
                .findFirst();
    }

    public static Optional<ReturnStmt> getReturnStmt(IfStmt ifStmt) {
        if (ifStmt == null) {
            throw new NullIfStmtException();
        }

        if (ifStmt.hasThenBlock()) {
            return ifStmt.getThenStmt()
                    .getChildNodes()
                    .stream()
                    .filter(ReturnStmt.class::isInstance)
                    .map(ReturnStmt.class::cast)
                    .findFirst();
        }
        return Optional.empty();
    }

    public static Optional<NameExpr> getNameExpr(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(NameExpr.class::isInstance)
                .map(NameExpr.class::cast)
                .findFirst();
    }

    public static Optional<SimpleName> getVariableSimpleName(VariableDeclarationExpr node) {
        Stream<VariableDeclarator> variableDeclarator = Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(VariableDeclarator.class::isInstance)
                .map(VariableDeclarator.class::cast);

        return variableDeclarator
                .map(Node::getChildNodes)
                .flatMap(it -> it.stream()
                        .filter(SimpleName.class::isInstance)
                        .map(SimpleName.class::cast))
                .findFirst();
    }

    public static Optional<SimpleName> getSimpleName(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(SimpleName.class::isInstance)
                .map(SimpleName.class::cast)
                .findFirst();
    }

    public static Optional<ClassOrInterfaceType> getParentType(CompilationUnit cUnit) {
        return getClassOrInterfaceDeclaration(cUnit)
                .flatMap(classOrInterfaceDeclaration -> classOrInterfaceDeclaration
                        .getChildNodes()
                        .stream()
                        .filter(ClassOrInterfaceType.class::isInstance)
                        .map(ClassOrInterfaceType.class::cast)
                        .findFirst());
    }

    public static Optional<ClassOrInterfaceType> getParentType(ClassOrInterfaceDeclaration classDclr) {
        return Optional.ofNullable(classDclr)
                .map(Node::getParentNode)
                .orElseThrow(NoClassOrInterfaceDeclarationException::new)
                .stream()
                .filter(ClassOrInterfaceType.class::isInstance)
                .map(ClassOrInterfaceType.class::cast)
                .findFirst();
    }

    public static Optional<CompilationUnit> getParent(CompilationUnit cUnit, Collection<CompilationUnit> allClasses) {
        final Optional<ClassOrInterfaceType> parentDef = getParentType(cUnit);

        if (parentDef.isPresent()) {

            var typeName = getSimpleName(parentDef.get())
                    .orElseThrow(SimpleNameException::new);

            for (CompilationUnit parent : allClasses) {
                final var declaration = getClassOrInterfaceDeclaration(parent);
                var isClassNameEqualsTypeName = declaration.map(dcl -> getSimpleName(dcl)
                                .orElseThrow(SimpleNameException::new))
                        .filter(typeName::equals)
                        .isPresent();

                if (isClassNameEqualsTypeName) {
                    return Optional.of(parent);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<CompilationUnit> getParent(CompilationUnit cUnit, CompilationUnit parent) {
        final Optional<ClassOrInterfaceType> parentDef = getParentType(cUnit);

        if (parentDef.isPresent()) {

            var typeName = getSimpleName(parentDef.get())
                    .orElseThrow(SimpleNameException::new);

            final var declaration = getClassOrInterfaceDeclaration(parent);
            var isClassNameEqualsTypeName = declaration.map(dcl -> getSimpleName(dcl)
                            .orElseThrow(SimpleNameException::new))
                    .filter(typeName::equals)
                    .isPresent();

            if (isClassNameEqualsTypeName) {
                return Optional.of(parent);
            }
        }
        return Optional.empty();
    }

    public static PackageDeclaration getPackageDeclaration(CompilationUnit cUnit) {
        return Optional.ofNullable(cUnit)
                .map(Node::getChildNodes)
                .orElseThrow(NullCompilationUnitException::new)
                .stream()
                .filter(PackageDeclaration.class::isInstance)
                .map(PackageDeclaration.class::cast)
                .findFirst()
                .orElseThrow(NoPackageDeclarationException::new);
    }

    public static Optional<ClassOrInterfaceDeclaration> getClassOrInterfaceDeclaration(CompilationUnit cUnit) {
        return Optional.ofNullable(cUnit)
                .map(Node::getChildNodes)
                .orElseThrow(NoClassOrInterfaceException::new)
                .stream()
                .filter(ClassOrInterfaceDeclaration.class::isInstance)
                .map(ClassOrInterfaceDeclaration.class::cast)
                .findFirst();
    }

    public static Collection<MethodDeclaration> getMethods(CompilationUnit cUnit) {
        return Optional.ofNullable(cUnit)
                .map(Node::getChildNodes)
                .orElseThrow(NullCompilationUnitException::new)
                .stream()
                .filter(n -> n instanceof ClassOrInterfaceDeclaration)
                .flatMap(n -> n.getChildNodes().stream())
                .filter(cn -> cn instanceof MethodDeclaration)
                .map(MethodDeclaration.class::cast)
                .collect(Collectors.toList());
    }

    public static Collection<MethodDeclaration> getMethods(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        return Optional.ofNullable(classOrInterfaceDeclaration)
                .map(Node::getChildNodes)
                .orElseThrow(NoClassOrInterfaceException::new)
                .stream()
                .filter(n -> n instanceof MethodDeclaration)
                .map(MethodDeclaration.class::cast)
                .collect(Collectors.toList());
    }

    public static Optional<BlockStmt> getBlockStatement(Node n) {
        return Optional.ofNullable(n)
                .map(Node::getChildNodes)
                .flatMap(it -> it.stream()
                        .filter(BlockStmt.class::isInstance)
                        .map(BlockStmt.class::cast)
                        .findFirst());
    }

    public static Optional<ExpressionStmt> getExpressionStatement(Node node) {
        if (node == null || node instanceof BlockStmt || node instanceof ClassOrInterfaceDeclaration) {
            return Optional.empty();
        }
        if (node instanceof ExpressionStmt) {
            return Optional.of((ExpressionStmt) node);
        }
        return getExpressionStatement(node.getParentNode().orElse(null));
    }

    public static List<SuperExpr> getSuperCalls(Node node) {
        final List<SuperExpr> superCalls = new ArrayList<>();

        if (node == null) {
            throw new NullNodeException();
        }

        if (node instanceof SuperExpr) {
            superCalls.add((SuperExpr) node);
        }

        if (node.getChildNodes() == null || node.getChildNodes().isEmpty()) {
            return superCalls;
        }

        superCalls.addAll(node.getChildNodes()
                .stream()
                .flatMap(cn -> getSuperCalls(cn).stream())
                .toList());

        return superCalls;
    }

    public static MethodDeclaration retrieveOverriddenMethod(CompilationUnit parent,
                                                             MethodDeclaration overridingMethod) {

        final String childMethodName = getSimpleName(overridingMethod)
                .orElseThrow(SimpleNameException::new)
                .asString();

        for (MethodDeclaration parentMethod : getMethods(parent)) {
            final String simpleName = getSimpleName(parentMethod)
                    .orElseThrow(SimpleNameException::new)
                    .asString();

            if (childMethodName.equals(simpleName) && methodsParamsMatch(overridingMethod, parentMethod)) {
                return parentMethod;
            }
        }
        return null;
    }

    public static boolean methodsParamsMatch(MethodDeclaration m1, MethodDeclaration m2) {
        if (m1 == null || m2 == null) {
            throw new NullMethodException();
        }

        for (int i = 0; i < m1.getParameters().size(); i++) {

            if (isPositionOutOfBounds(i, m2.getParameters())
                    || !m1.getParameters().get(i).getType().equals(m2.getParameters().get(i).getType())) {
                return false;
            }
        }
        return true;
    }

    public static boolean childHasDirectSuperCall(Node node) {
        if (node == null) {
            throw new NullNodeException();
        }
        if (node instanceof NodeWithCondition || node instanceof TryStmt || node instanceof CatchClause) {
            return false;
        }

        if (node instanceof MethodCallExpr && ((MethodCallExpr) node).getArguments() != null) {
            boolean argumentsPresentSuper = ((MethodCallExpr) node)
                    .getArguments()
                    .stream()
                    .anyMatch(AstHandler::childHasDirectSuperCall);

            if (argumentsPresentSuper) {
                return true;
            }
        }

        if (node.getChildNodes() == null || node.getChildNodes().isEmpty()) {
            return false;
        }

        if (node instanceof SuperExpr || node.getChildNodes().stream().anyMatch(c -> c instanceof SuperExpr)) {
            return true;
        }

        return node.getChildNodes().stream().anyMatch(AstHandler::childHasDirectSuperCall);
    }

    public static boolean nodeHasReturnStatement(Node node) {
        return nodeHasClazz(node, ReturnStmt.class);
    }

    public static boolean nodeThrowsException(Node node) {
        return nodeHasClazz(node, ThrowStmt.class);
    }

    public static boolean nodeHasClazz(Node node, Class<?> clazz) {
        var nonNullClazz = Optional.ofNullable(clazz)
                .orElseThrow(ClassExpectedException::new);

        if (nonNullClazz.isInstance(node)) {
            return true;
        }

        if (node == null || node.getChildNodes() == null || node.getChildNodes().isEmpty()) {
            return false;
        }

        return node.getChildNodes().stream().anyMatch(n -> nodeHasClazz(n, nonNullClazz));
    }

    public static Collection<VariableDeclarationExpr> extractVariableDclrFromNode(Node node) {
        var nonNUllNode = Optional.ofNullable(node)
                .orElseThrow(NullNodeException::new);

        if (node instanceof VariableDeclarationExpr) {
            final List<VariableDeclarationExpr> variables = new ArrayList<>();
            variables.add((VariableDeclarationExpr) node);
            return variables;
        }

        if (node.getChildNodes() == null || node.getChildNodes().isEmpty()) {
            return new ArrayList<>();
        } else {
            return node.getChildNodes().stream().flatMap(cn -> extractVariableDclrFromNode(cn).stream())
                    .collect(Collectors.toList());
        }
    }

    public static boolean variableIsPresentInMethodCall(VariableDeclarationExpr var, MethodCallExpr methodCall) {
        var simpleNameList = Optional.ofNullable(methodCall)
                .map(MethodCallExpr::getChildNodes)
                .orElseThrow(MethodCallExpectedException::new)
                .stream()
                .filter(NameExpr.class::isInstance)
                .map(NameExpr.class::cast)
                .map(NameExpr::getName)
                .toList();

        for (SimpleName paramName : simpleNameList) {
            if (getVariableName(var).equals(paramName)) {
                return true;
            }
        }
        return false;
    }

    public static SimpleName getVariableName(VariableDeclarationExpr var) {
        return Optional.ofNullable(var)
                .map(VariableDeclarationExpr::getChildNodes)
                .orElseThrow(VariableDeclarationExpectedException::new)
                .stream()
                .filter(VariableDeclarator.class::isInstance)
                .map(VariableDeclarator.class::cast)
                .findFirst()
                .map(VariableDeclarator::getName)
                .orElseThrow(SimpleNameException::new);
    }

    public static boolean nodeHasSimpleName(SimpleName name, Node node) {
        var nonNullName = Optional.ofNullable(name)
                .orElseThrow(SimpleNameException::new);
        var nonNullNode = Optional.ofNullable(node)
                .orElseThrow(NullNodeException::new);

        if (nonNullNode instanceof SimpleName && nonNullNode.equals(nonNullName)) {
            return true;
        }

        if (nonNullNode.getChildNodes() == null || nonNullNode.getChildNodes().isEmpty()) {
            return false;
        }

        return nonNullNode.getChildNodes()
                .stream()
                .anyMatch(n -> nodeHasSimpleName(nonNullName, n));
    }

    public static <T extends Node> Collection<T> getNodeByType(Node node, Class<T> type) {
        if (node == null) {
            throw new NullNodeException();
        }
        if (type == null) {
            throw new ClassExpectedException();
        }

        final Collection<T> methodCalls = new ArrayList<>();

        if (type.isInstance(node)) {
            methodCalls.add(type.cast(node));
        } else if (node.getChildNodes().isEmpty()) {
            return methodCalls;
        }

        methodCalls.addAll(node.getChildNodes().stream()
                .map(n -> getNodeByType(n, type))
                .flatMap(Collection::stream)
                .toList());

        return methodCalls;
    }

    public static Collection<MethodCallExpr> getMethodCallExpr(Node node) {
        final Collection<MethodCallExpr> methodCalls = new ArrayList<>();

        if (node == null) {
            return methodCalls;
        } else if (node instanceof MethodCallExpr) {
            methodCalls.add((MethodCallExpr) node);
        } else if (node.getChildNodes() == null || node.getChildNodes().isEmpty()) {
            return methodCalls;
        }

        methodCalls.addAll(node.getChildNodes().stream()
                .map(AstHandler::getMethodCallExpr)
                .flatMap(Collection::stream)
                .toList());

        return methodCalls;
    }

    public static boolean doVariablesNameMatch(VariableDeclarationExpr var1, VariableDeclarationExpr var2) {
        return getVariableSimpleName(var1).equals(getVariableSimpleName(var2));
    }

    public static boolean doesNodeContainMatchingMethodCall(Node node, MethodCallExpr methodCall) {

        final var methodCalls = getMethodCallExpr(node);

        return methodCalls.stream().anyMatch(m -> doesMethodCallsMatch(m, methodCall));
    }

    private static boolean isPositionOutOfBounds(int position, NodeList<?> list) {
        return (list.size() - 1) < position;
    }

    public static boolean doesMethodCallsMatch(MethodCallExpr mc1, MethodCallExpr mc2) {
        if (mc1 == null || mc2 == null) {
            throw new NullMethodException();
        }

        if (!mc1.getName().equals(mc2.getName())) {
            return false;
        }

        for (int i = 0; i < mc1.getArguments().size(); i++) {
            if (isPositionOutOfBounds(i, mc2.getArguments()) || !mc1.getArguments().get(i).equals(mc2.getArguments().get(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean doesCompilationUnitsMatch(CompilationUnit c1, CompilationUnit c2) {
        return doesCompilationUnitsMatch(c1, getClassOrInterfaceDeclaration(c2), c2.getPackageDeclaration());
    }

    public static boolean doesCompilationUnitsMatch(CompilationUnit c1, Optional<ClassOrInterfaceDeclaration> classOrInterface2,
                                                    Optional<PackageDeclaration> package2) {

        final String p1 = Optional.ofNullable(c1)
                .flatMap(CompilationUnit::getPackageDeclaration)
                .map(PackageDeclaration::getNameAsString)
                .orElse("");
        final String p2 = package2
                .map(PackageDeclaration::getNameAsString)
                .orElse("");

        final String type1 = getClassOrInterfaceDeclaration(c1).map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElse("");
        final String type2 = classOrInterface2.map(ClassOrInterfaceDeclaration::getNameAsString).orElse("");

        return p1.equals(p2) && !type1.isEmpty() && type1.equals(type2);
    }

    public static Collection<IfStmt> getIfStatements(MethodDeclaration method) {
        if (method == null) {
            throw new NullMethodException();
        }

        final List<IfStmt> statements = new ArrayList<>();

        if (method.getBody().isEmpty()) {
            return statements;
        }

        final Optional<IfStmt> ifStmt = method.getBody()
                .get()
                .getStatements()
                .stream()
                .filter(IfStmt.class::isInstance)
                .map(IfStmt.class::cast).findFirst();

        ifStmt.ifPresent(i -> {
            statements.add(i);
            statements.addAll(getInnerIfStatements(i));
        });

        return statements;
    }

    private static Collection<IfStmt> getInnerIfStatements(IfStmt statement) {
        if (statement == null) {
            throw new NullIfStmtException();
        }

        final List<IfStmt> inner = statement.getChildNodes()
                .stream()
                .filter(IfStmt.class::isInstance)
                .map(IfStmt.class::cast)
                .toList();

        final List<IfStmt> statements = new ArrayList<>(inner);

        for (IfStmt singleInner : inner) {
            statements.addAll(getInnerIfStatements(singleInner));
        }

        return statements;
    }

    public static Optional<LiteralExpr> getLiteralExpr(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(LiteralExpr.class::isInstance)
                .map(LiteralExpr.class::cast)
                .findFirst();
    }

    public static Optional<VariableDeclarator> getVariableDeclarationInNode(Node node, String returnName) {
        if (node == null) {
            throw new NullNodeException();
        }
        Assert.notNull(returnName, "Return name cannot be null");

        if (node instanceof VariableDeclarationExpr varDclrExpr) {
            return varDclrExpr.getVariables().stream()
                    .filter(v -> v.getNameAsString().equals(returnName))
                    .findFirst();
        }

        return node.getChildNodes().stream()
                .map(c -> getVariableDeclarationInNode(c, returnName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(VariableDeclarator.class::cast)
                .findFirst();
    }

    public static Optional<ClassOrInterfaceType> getMethodReturnClassType(MethodDeclaration method) {
        return Optional.ofNullable(method)
                .map(MethodDeclaration::getType)
                .filter(ClassOrInterfaceType.class::isInstance)
                .map(ClassOrInterfaceType.class::cast);
    }

    public static boolean doesNodeUsesVar(Node node, VariableDeclarator var) {
        if (node == null) {
            throw new NullNodeException();
        }

        if (var == null) {
            throw new VariableDeclarationExpectedException();
        }

        if (node instanceof NameExpr) {
            return ((NameExpr) node).getNameAsString().equals(var.getNameAsString());
        }

        return node.getChildNodes().stream().anyMatch(c -> doesNodeUsesVar(c, var));
    }

    public static Collection<VariableDeclarator> getVariableDeclarations(Node node) {
        if (node == null) {
            throw new NullNodeException();
        }

        final List<VariableDeclarator> variables = new ArrayList<>();

        if (node instanceof VariableDeclarator) {
            variables.add((VariableDeclarator) node);

            return variables;
        }

        return node.getChildNodes().stream()
                .flatMap(c -> getVariableDeclarations(c).stream())
                .toList();
    }

    public static MethodDeclaration getMethodByName(ClassOrInterfaceDeclaration clazz, String method) {
        return getMethods(clazz).stream()
                .filter(m -> m.getNameAsString().equals(method))
                .findFirst()
                .orElse(null);
    }

    public static MethodDeclaration getMethodByName(CompilationUnit cUnit, String method) {
        return getMethods(cUnit).stream()
                .filter(m -> m.getNameAsString().equals(method))
                .findFirst()
                .orElse(null);
    }
}
