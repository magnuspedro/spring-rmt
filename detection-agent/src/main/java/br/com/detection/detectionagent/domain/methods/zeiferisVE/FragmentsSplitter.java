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

import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class FragmentsSplitter {

    @Getter
    private final List<Node> beforeFragment = new ArrayList<>();

    private Node node = null;

    @Getter
    private final List<Node> afterFragment = new ArrayList<>();

    private final AstHandler astHandler = new AstHandler();

    public FragmentsSplitter(MethodDeclaration m, SuperExpr superCall) {
        final BlockStmt blockStmt = astHandler.getBlockStatement(m)
                .orElseThrow(() -> new IllegalArgumentException("Method has no body"));

        boolean superWasFound = false;
        for (Node child : blockStmt.getChildNodes()) {

            if (this.astHandler.childHasDirectSuperCall(child, superCall)) {
                superWasFound = true;
                this.node = child;
                continue;
            }

            this.addToFragment(superWasFound, child);
        }

        if (this.node == null) {
            log.warn("nulo");
        }
    }

    public FragmentsSplitter(MethodDeclaration m, MethodCallExpr methodCall) {
        final BlockStmt blockStmt = astHandler.getBlockStatement(m)
                .orElseThrow(() -> new IllegalArgumentException("Method has no body"));

        boolean methodCallWasFound = false;
        for (Node child : blockStmt.getChildNodes()) {

            if (this.astHandler.nodeHasSameMethodCall(child, methodCall)) {
                methodCallWasFound = true;
                this.node = child;
                continue;
            }

            this.addToFragment(methodCallWasFound, child);
        }

        if (this.node == null) {
            log.warn("Node is Null");
        }
    }

    private void addToFragment(boolean superWasFound, Node node) {
        if (superWasFound) {
            this.afterFragment.add(node);
        } else {
            this.beforeFragment.add(node);
        }
    }

    public Node getSpecificNode() {
        return node;
    }

    public boolean hasSpecificNode() {
        return this.node != null;
    }

    public Collection<VariableDeclarationExpr> getBeforeVariablesUsedInSpecificNodeAndBeforeFragments() {
        final Collection<VariableDeclarationExpr> variables = this.getBeforeFragment().stream().flatMap(n -> this.astHandler.extractVariableDclrFromNode(n).stream()).toList();

        final Optional<MethodCallExpr> methodCall = this.astHandler.getMethodCallExpr(node).stream().findFirst();

        if (methodCall.isEmpty()) {

            log.info("Method call not found - {}", NodeConverter.toString(node));

            return Collections.emptyList();
        }

        final List<VariableDeclarationExpr> referencedVariables = new ArrayList<>();
        for (VariableDeclarationExpr var : variables) {
            if (this.astHandler.variableIsPresentInMethodCall(var, methodCall.get())) {
                referencedVariables.add(var);
                continue;
            }

            if (this.afterFragmentContaisVariable(var)) {
                referencedVariables.add(var);
            }
        }
        return referencedVariables;
    }

    public Optional<SuperReturnVar> getSuperReturnVariable() {
        if (this.node.getChildNodes() == null || this.node.getChildNodes().isEmpty()) {
            return Optional.empty();
        }

        if (this.node.getChildNodes().get(0) instanceof VariableDeclarationExpr) {
            return Optional.of(new SuperReturnVar((VariableDeclarationExpr) this.node.getChildNodes().get(0)));
        } else if (this.node.getChildNodes().get(0) instanceof AssignExpr assignment) {
            return Optional.of(new SuperReturnVar(assignment));
        }
        return Optional.empty();
    }

    private boolean afterFragmentContaisVariable(VariableDeclarationExpr var) {
        return this.getAfterFragment().stream().anyMatch(n -> this.astHandler.nodeHasSimpleName(this.astHandler.getVariableName(var), n));
    }

    private Type getTypeOfVar(NameExpr nameExpr) {

        final Collection<VariableDeclarationExpr> declarations = Stream.of(this.beforeFragment.stream(), Stream.of(this.node), this.afterFragment.stream())
                .flatMap(s -> s)
                .map(this.astHandler::extractVariableDclrFromNode)
                .flatMap(Collection::stream)
                .toList();

        return declarations.stream()
                .flatMap(varDclr -> varDclr.getVariables().stream())
                .filter(v -> v.getNameAsString().equals(nameExpr.getNameAsString()))
                .findFirst().get()
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
