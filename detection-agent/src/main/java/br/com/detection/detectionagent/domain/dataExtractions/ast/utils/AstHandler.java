package br.com.detection.detectionagent.domain.dataExtractions.ast.utils;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions.*;
import br.com.detection.detectionagent.methods.dataExtractions.forks.DataHandler;
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
import com.github.javaparser.ast.type.Type;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AstHandler {

    public Collection<FieldDeclaration> getDeclaredFields(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(FieldDeclaration.class::isInstance)
                .map(FieldDeclaration.class::cast)
                .collect(Collectors.toList());
    }

    public Optional<ObjectCreationExpr> getObjectCreationExpr(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(ObjectCreationExpr.class::isInstance)
                .map(ObjectCreationExpr.class::cast)
                .findFirst();
    }

    public Optional<ReturnStmt> getReturnStmt(IfStmt ifStmt) {
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

    public Optional<NameExpr> getNameExpr(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(NameExpr.class::isInstance)
                .map(NameExpr.class::cast)
                .findFirst();
    }

    public Optional<SimpleName> getVariableSimpleName(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(SimpleName.class::isInstance)
                .map(SimpleName.class::cast)
                .findFirst();
    }

    public Optional<SimpleName> getSimpleName(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .orElseThrow(NullNodeException::new)
                .stream()
                .filter(SimpleName.class::isInstance)
                .map(SimpleName.class::cast)
                .findFirst();
    }

    public Optional<ClassOrInterfaceType> getParentType(CompilationUnit cUnit) {
        return this.getClassOrInterfaceDeclaration(cUnit)
                .flatMap(classOrInterfaceDeclaration -> classOrInterfaceDeclaration
                        .getChildNodes()
                        .stream()
                        .filter(ClassOrInterfaceType.class::isInstance)
                        .map(ClassOrInterfaceType.class::cast)
                        .findFirst());
    }

    public Optional<ClassOrInterfaceType> getParentType(ClassOrInterfaceDeclaration classDclr) {
        return Optional.ofNullable(classDclr)
                .map(Node::getParentNode)
                .orElseThrow(NoClassOrInterfaceDeclarationException::new)
                .stream()
                .filter(ClassOrInterfaceType.class::isInstance)
                .map(ClassOrInterfaceType.class::cast)
                .findFirst();
    }

    public Optional<CompilationUnit> getParent(CompilationUnit cUnit, Collection<CompilationUnit> allClasses) {
        final Optional<ClassOrInterfaceType> parentDef = this.getParentType(cUnit);

        if (parentDef.isPresent()) {

            var typeName = this.getSimpleName(parentDef.get())
                    .orElseThrow(SimpleNameException::new);

            for (CompilationUnit parent : allClasses) {
                final var declaration = this.getClassOrInterfaceDeclaration(parent);
                var isClassNameEqualsTypeName = declaration.map(dcl -> this.getSimpleName(dcl)
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

    public PackageDeclaration getPackageDeclaration(CompilationUnit cUnit) {
        return Optional.ofNullable(cUnit)
                .map(Node::getChildNodes)
                .orElseThrow(NullCompilationUnitException::new)
                .stream()
                .filter(PackageDeclaration.class::isInstance)
                .map(PackageDeclaration.class::cast)
                .findFirst()
                .orElseThrow(NoPackageDeclarationException::new);
    }

    public Optional<ClassOrInterfaceDeclaration> getClassOrInterfaceDeclaration(CompilationUnit cUnit) {
        return Optional.ofNullable(cUnit)
                .map(Node::getChildNodes)
                .orElseThrow(NoClassOrInterfaceException::new)
                .stream()
                .filter(ClassOrInterfaceDeclaration.class::isInstance)
                .map(ClassOrInterfaceDeclaration.class::cast)
                .findFirst();
    }

    public Collection<MethodDeclaration> getMethods(CompilationUnit cUnit) {
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

    public Optional<BlockStmt> getBlockStatement(Node n) {
        return Optional.ofNullable(n)
                .map(Node::getChildNodes)
                .flatMap(it -> it.stream()
                        .filter(BlockStmt.class::isInstance)
                        .map(BlockStmt.class::cast)
                        .findFirst());
    }

    public Optional<ExpressionStmt> getExpressionStatement(Node node) {
        if (node == null || node instanceof BlockStmt || node instanceof ClassOrInterfaceDeclaration) {
            return Optional.empty();
        }
        if (node instanceof ExpressionStmt) {
            return Optional.of((ExpressionStmt) node);
        }
        return this.getExpressionStatement(node.getParentNode().orElse(null));
    }

    public Collection<SuperExpr> getSuperCalls(Node node) {
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

        superCalls.addAll(node.getChildNodes().stream().flatMap(cn -> this.getSuperCalls(cn).stream())
                .toList());

        return superCalls;
    }

    public MethodDeclaration retrieveOverriddenMethod(CompilationUnit parent,
                                                      MethodDeclaration overridingMethod) {

        final String childMethodName = this.getSimpleName(overridingMethod)
                .orElseThrow(SimpleNameException::new)
                .asString();

        for (MethodDeclaration parentMethod : this.getMethods(parent)) {
            final String simpleName = this.getSimpleName(parentMethod)
                    .orElseThrow(SimpleNameException::new)
                    .asString();

            if (childMethodName.equals(simpleName) && this.methodsParamsMatch(overridingMethod, parentMethod)) {
                return parentMethod;
            }
        }
        return null;
    }

    public boolean methodsParamsMatch(MethodDeclaration m1, MethodDeclaration m2) {
        if (m1 == null || m2 == null) {
            throw new NullMethodException();
        }

        for (int i = 0; i < m1.getParameters().size(); i++) {

            if (!listHasPos(i, m2.getParameters())
                    || !m1.getParameters().get(i).getType().equals(m2.getParameters().get(i).getType())) {
                return false;
            }
        }
        return true;
    }

    public boolean childHasDirectSuperCall(Node node) {
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
                    .anyMatch(this::childHasDirectSuperCall);
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

        return node.getChildNodes().stream().anyMatch(this::childHasDirectSuperCall);
    }

    public boolean nodeHasReturnStatement(Node node) {
        return nodeHasClazz(node, ReturnStmt.class);
    }

    public boolean nodeThrowsException(Node node) {
        return nodeHasClazz(node, ThrowStmt.class);
    }

    // Recursion order was changed, the node instance of was the second condition
    public boolean nodeHasClazz(Node node, Class<?> clazz) {
        if (clazz.isInstance(node)) {
            return true;
        }

        if (node.getChildNodes() == null || node.getChildNodes().isEmpty()) {
            return false;
        }

        return node.getChildNodes().stream().anyMatch(n -> this.nodeHasClazz(n, clazz));
    }

    // Recursion order was changed, the node instance of was the second condition
    public Collection<VariableDeclarationExpr> extractVariableDclrFromNode(Node node) {
        if (node instanceof VariableDeclarationExpr) {
            final List<VariableDeclarationExpr> variables = new ArrayList<>();
            variables.add((VariableDeclarationExpr) node);
            return variables;
        }

        if (node.getChildNodes() == null || node.getChildNodes().isEmpty()) {
            return new ArrayList<>();
        } else {
            return node.getChildNodes().stream().flatMap(cn -> this.extractVariableDclrFromNode(cn).stream())
                    .collect(Collectors.toList());
        }
    }

    public boolean variableIsPresentInMethodCall(VariableDeclarationExpr var, MethodCallExpr methodCall) {
        var simpleNameList = methodCall.getChildNodes().stream()
                .filter(NameExpr.class::isInstance)
                .map(NameExpr.class::cast)
                .map(NameExpr::getName)
                .toList();

        for (SimpleName paramName : simpleNameList) {
            if (this.getVariableName(var).equals(paramName)) {
                return true;
            }
        }
        return false;
    }

    public SimpleName getVariableName(VariableDeclarationExpr var) {
        return var.getChildNodes().stream().filter(VariableDeclarator.class::isInstance)
                .map(VariableDeclarator.class::cast).findFirst().get().getName();
    }

    public boolean nodeHasSimpleName(SimpleName name, Node node) {

        if (node instanceof SimpleName && node.equals(name)) {
            return true;
        }

        if (node.getChildNodes() == null || node.getChildNodes().isEmpty()) {
            return false;
        }

        return node.getChildNodes().stream().anyMatch(n -> this.nodeHasSimpleName(name, n));
    }

    public <T extends Node> Collection<T> getByNodeType(Node node, Class<T> type) {
        final Collection<T> methodCalls = new ArrayList<>();

        if (type.isInstance(node)) {
            methodCalls.add(type.cast(node));
        } else if (node.getChildNodes().isEmpty()) {
            return methodCalls;
        }

        methodCalls.addAll(node.getChildNodes().stream().map(n -> this.getByNodeType(node, type))
                .flatMap(Collection::stream).collect(Collectors.toList()));

        return methodCalls;
    }

    public Collection<MethodCallExpr> getMethodCallExpr(Node node) {
        final Collection<MethodCallExpr> methodCalls = new ArrayList<>();

        if (node == null) {
            return methodCalls;
        } else if (node instanceof MethodCallExpr) {
            methodCalls.add((MethodCallExpr) node);
        } else if (node.getChildNodes() == null || node.getChildNodes().isEmpty()) {
            return methodCalls;
        }

        methodCalls.addAll(node.getChildNodes().stream().map(this::getMethodCallExpr).flatMap(Collection::stream)
                .collect(Collectors.toList()));

        return methodCalls;
    }

    public boolean variablesAreEqual(VariableDeclarationExpr var1, VariableDeclarationExpr var2) {
        return this.getVariableSimpleName(var1).equals(this.getVariableSimpleName(var2));
    }

    public boolean nodeHasSameMethodCall(Node node, MethodCallExpr methodCall) {

        final Collection<MethodCallExpr> methodCalls = this.getMethodCallExpr(node);

        return methodCalls.stream().anyMatch(m -> this.methodCallsMatch(m, methodCall));
    }

    private boolean listHasPos(int position, NodeList<?> list) {
        return (list.size() - 1) >= position;
    }

    public boolean methodCallsMatch(MethodCallExpr mc1, MethodCallExpr mc2) {
        if (!mc1.getName().equals(mc2.getName())) {
            return false;
        }

        for (int i = 0; i < mc1.getArguments().size(); i++) {
            if (!listHasPos(i, mc2.getArguments()) || !mc1.getArguments().get(i).equals(mc2.getArguments().get(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean unitsMatch(CompilationUnit c1, CompilationUnit c2) {
        return this.unitsMatch(c1, this.getClassOrInterfaceDeclaration(c2), c2.getPackageDeclaration());
    }

    public boolean unitsMatch(CompilationUnit c1, Optional<ClassOrInterfaceDeclaration> classOrInterface2,
                              Optional<PackageDeclaration> package2) {
        final String p1 = c1.getPackageDeclaration().map(PackageDeclaration::getNameAsString).orElse("");
        final String p2 = package2.map(PackageDeclaration::getNameAsString).orElse("");

        final String type1 = this.getClassOrInterfaceDeclaration(c1).map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElse("");
        final String type2 = classOrInterface2.map(ClassOrInterfaceDeclaration::getNameAsString).orElse("");

        return p1.equals(p2) && !type1.equals("") && type1.equals(type2);
    }

    public Collection<IfStmt> getIfStatements(MethodDeclaration method) {

        final List<IfStmt> statements = new ArrayList<>();

        if (!method.getBody().isPresent()) {
            return statements;
        }

        final Optional<IfStmt> ifStmt = method.getBody().get().getStatements().stream().filter(IfStmt.class::isInstance)
                .map(IfStmt.class::cast).findFirst();

        ifStmt.ifPresent(i -> {
            statements.add(i);
            statements.addAll(this.getInnerIfStatements(i));
        });

        return statements;
    }

    private Collection<IfStmt> getInnerIfStatements(IfStmt statement) {
        final List<IfStmt> statements = new ArrayList<>();

        final List<IfStmt> inner = statement.getChildNodes().stream().filter(IfStmt.class::isInstance)
                .map(IfStmt.class::cast).collect(Collectors.toList());

        statements.addAll(inner);

        for (IfStmt singleInner : inner) {
            statements.addAll(this.getInnerIfStatements(singleInner));
        }

        return statements;
    }

    public Optional<LiteralExpr> getLiteralExpr(Node node) {
        return node.getChildNodes().stream().filter(LiteralExpr.class::isInstance).map(LiteralExpr.class::cast)
                .findFirst();
    }

    public Optional<VariableDeclarator> getVariableDeclarationInNode(Node node, String returnName) {

        if (node instanceof VariableDeclarationExpr) {

            final VariableDeclarationExpr varDclrExpr = (VariableDeclarationExpr) node;

            return varDclrExpr.getVariables().stream().filter(v -> v.getNameAsString().equals(returnName)).findFirst();
        }

        return node.getChildNodes().stream().map(c -> this.getVariableDeclarationInNode(c, returnName))
                .filter(Optional::isPresent).map(Optional::get).map(VariableDeclarator.class::cast).findFirst();
    }

    public Optional<ClassOrInterfaceType> getReturnTypeClassOrInterfaceDeclaration(MethodDeclaration method) {
        return Optional.of(method.getType()).filter(ClassOrInterfaceType.class::isInstance)
                .map(ClassOrInterfaceType.class::cast);
    }

    public boolean nodeUsesVar(Node node, VariableDeclarator var) {

        if (node instanceof NameExpr) {
            return ((NameExpr) node).getNameAsString().equals(var.getNameAsString());
        }

        return node.getChildNodes().stream().anyMatch(c -> this.nodeUsesVar(c, var));
    }

    public Collection<VariableDeclarator> getVariableDeclarations(Node node) {

        final List<VariableDeclarator> variables = new ArrayList<>();

        if (node instanceof VariableDeclarator) {
            variables.add((VariableDeclarator) node);

            return variables;
        }

        return node.getChildNodes().stream().flatMap(c -> this.getVariableDeclarations(c).stream())
                .collect(Collectors.toList());
    }

    public static Collection<ObjectCreationExpr> getInstance(CompilationUnit cu, DataHandler dataHandler) {
        List<ObjectCreationExpr> instance = new ArrayList<>();
        cu.findAll(ObjectCreationExpr.class).stream().forEach(f -> {
            Type element = f.getType();
            if (element.isReferenceType()) {
                if (dataHandler.getParsedFileByName(f.getType().toString()) != null
                        && !f.getType().toString().contains("Exception")) {
                    instance.add(f);
                }
            }
        });
        return instance;
    }

}
