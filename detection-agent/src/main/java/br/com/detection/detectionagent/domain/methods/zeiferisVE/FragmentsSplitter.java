package br.com.detection.detectionagent.domain.methods.zeiferisVE;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.NodeConverter;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class FragmentsSplitter {

    @Getter
    private final List<Node> beforeFragment = new ArrayList<>();

    private Node node = null;

    @Getter
    private final List<Node> afterFragment = new ArrayList<>();

    private final AstHandler astHandler = new AstHandler();

    public FragmentsSplitter(MethodDeclaration m) {
        final BlockStmt blockStmt = astHandler.getBlockStatement(m)
                .orElseThrow(() -> new IllegalArgumentException("Method has no body"));

        var superWasFound = false;
        for (Node child : blockStmt.getChildNodes()) {

            if (this.astHandler.childHasDirectSuperCall(child)) {
                superWasFound = true;
                this.node = child;
                continue;
            }

            this.addToFragment(superWasFound, child);
        }

        if (this.node == null) {
            log.warn("Fragment Splitter node is null");
        }
    }

    public FragmentsSplitter(MethodDeclaration m, MethodCallExpr methodCall) {
        final BlockStmt blockStmt = astHandler.getBlockStatement(m)
                .orElseThrow(() -> new IllegalArgumentException("Method has no body"));

        boolean methodCallWasFound = false;
        for (Node child : blockStmt.getChildNodes()) {

            if (this.astHandler.doesNodeContainMatchingMethodCall(child, methodCall)) {
                methodCallWasFound = true;
                this.node = child;
                continue;
            }

            this.addToFragment(methodCallWasFound, child);
        }

        if (this.node == null) {
            log.warn("Fragment Splitter node is null");
        }
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

    public List<VariableDeclarationExpr> getVariablesOnBeforeFragmentsMethodCalss() {
        final var variables = this.getBeforeFragment()
                .stream()
                .flatMap(n -> this.astHandler.extractVariableDclrFromNode(n).stream())
                .toList();

        final var methodCall = this.astHandler.getMethodCallExpr(node).stream().findFirst();

        if (methodCall.isEmpty()) {

            log.info("Method call not found - {}", NodeConverter.toString(node));

            return List.of();
        }

        final var referencedVariables = new ArrayList<VariableDeclarationExpr>();
        for (var variable : variables) {
            if (this.astHandler.variableIsPresentInMethodCall(variable, methodCall.get())) {
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
        if (this.node.getChildNodes() == null || this.node.getChildNodes().isEmpty()) {
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
        return this.getAfterFragment().stream().anyMatch(n -> this.astHandler.nodeHasSimpleName(this.astHandler.getVariableName(var), n));
    }

    private Type getTypeOfVar(NameExpr nameExpr) {

        final Collection<VariableDeclarationExpr> declarations = Stream.of(this.beforeFragment.stream(), Stream.of(this.node), this.afterFragment.stream())
                .flatMap(s -> s)
                .map(this.astHandler::extractVariableDclrFromNode)
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
