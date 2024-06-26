package br.com.magnus.detectionandrefactoring.refactor.methods.zaiferisVE;

import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.NodeConverter;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Getter
@NoArgsConstructor
public class FragmentsSplitter {

    private final List<Node> beforeFragment = new ArrayList<>();

    private Node node = null;

    private final List<Node> afterFragment = new ArrayList<>();

    public static FragmentsSplitter splitByMethod(MethodDeclaration method) {
        var fragment = new FragmentsSplitter();
        final BlockStmt blockStmt = AstHandler.getBlockStatement(method)
                .orElseThrow(() -> new IllegalArgumentException("Method has no body"));

        var hasSuper = false;
        for (Node child : blockStmt.getChildNodes()) {

            if (AstHandler.childHasDirectSuperCall(child)) {
                hasSuper = true;
                fragment.node = child;
                continue;
            }

            fragment.addToFragment(hasSuper, child);
        }

        if (fragment.node == null) {
            log.warn("Fragment Splitter node is null, in method splitByMethod");
        }
        return fragment;
    }

    public static FragmentsSplitter splitByMethodAndMethodCall(MethodDeclaration method, MethodCallExpr methodCall) {
        var fragment = new FragmentsSplitter();
        final BlockStmt blockStmt = AstHandler.getBlockStatement(method)
                .orElseThrow(() -> new IllegalArgumentException("Method has no body"));

        boolean hasMethodCall = false;
        for (Node child : blockStmt.getChildNodes()) {

            if (AstHandler.doesNodeContainMatchingMethodCall(child, methodCall)) {
                hasMethodCall = true;
                fragment.node = child;
                continue;
            }

            fragment.addToFragment(hasMethodCall, child);
        }

        if (fragment.node == null) {
            log.warn("Fragment Splitter node is null");
        }
        return fragment;
    }

    private void addToFragment(boolean hasSuper, Node node) {
        if (hasSuper) {
            this.afterFragment.add(node);
            return;
        }
        this.beforeFragment.add(node);
    }

    public boolean hasSpecificNode() {
        return this.node != null;
    }

    public List<VariableDeclarationExpr> getVariablesOnBeforeFragmentsMethodClass() {
        final var variables = this.getBeforeFragment()
                .stream()
                .flatMap(n -> AstHandler.extractVariableDclrFromNode(n).stream())
                .toList();

        final var methodCall = AstHandler.getMethodCallExpr(node).stream().findFirst();

        if (methodCall.isEmpty()) {

            log.info("Method call not found - {}", NodeConverter.toString(node));

            return List.of();
        }

        final var referencedVariables = new ArrayList<VariableDeclarationExpr>();
        for (var variable : variables) {
            if (AstHandler.variableIsPresentInMethodCall(variable, methodCall.get())) {
                referencedVariables.add(variable);
                continue;
            }

            if (this.afterFragmentContainsVariable(variable)) {
                referencedVariables.add(variable);
            }
        }
        return referencedVariables;
    }

    public Optional<SuperReturnVar> getSuperReturnVariable() {
        if (this.node == null || this.node.getChildNodes() == null || this.node.getChildNodes().isEmpty()) {
            return Optional.empty();
        }

        if (this.node.getChildNodes().getFirst() instanceof VariableDeclarationExpr) {
            return Optional.of(new SuperReturnVar((VariableDeclarationExpr) this.node.getChildNodes().getFirst()));
        } else if (this.node.getChildNodes().getFirst() instanceof AssignExpr assignment) {
            return Optional.of(new SuperReturnVar(assignment));
        }
        return Optional.empty();
    }

    private boolean afterFragmentContainsVariable(VariableDeclarationExpr var) {
        return this.getAfterFragment().stream().anyMatch(n -> AstHandler.nodeHasSimpleName(AstHandler.getVariableName(var), n));
    }

    private Type getTypeOfVar(NameExpr nameExpr) {

        final Collection<VariableDeclarationExpr> declarations = Stream.of(this.beforeFragment.stream(), Stream.of(this.node), this.afterFragment.stream())
                .flatMap(s -> s)
                .map(AstHandler::extractVariableDclrFromNode)
                .flatMap(Collection::stream)
                .toList();

        return declarations.stream()
                .map(VariableDeclarationExpr::getVariables)
                .flatMap(Collection::stream)
                .filter(v -> v.getNameAsString().equals(nameExpr.getNameAsString()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Variable name is not the same"))
                .getType();
    }

    public List<Statement> getBeforeStatements() {
        return this.beforeFragment.stream()
                .filter(Statement.class::isInstance)
                .map(Statement.class::cast)
                .toList();
    }

    public List<Statement> getAfterStatements() {
        return this.afterFragment.stream()
                .filter(Statement.class::isInstance)
                .map(Statement.class::cast)
                .toList();
    }

    @Getter
    public class SuperReturnVar {

        private final Type type;

        private final SimpleName name;

        public SuperReturnVar(VariableDeclarationExpr varDclrExpr) {
            this.type = varDclrExpr.getVariable(0).getType();
            this.name = varDclrExpr.getVariable(0).getName();
        }

        public SuperReturnVar(AssignExpr assignExpr) {
            final NameExpr nameExpr = assignExpr.getChildNodes().stream().filter(NameExpr.class::isInstance).map(NameExpr.class::cast).findFirst().get();

            this.type = getTypeOfVar(nameExpr);
            this.name = nameExpr.getName();
        }

    }
}
