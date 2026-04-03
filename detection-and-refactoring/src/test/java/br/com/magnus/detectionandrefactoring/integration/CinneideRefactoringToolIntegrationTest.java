package br.com.magnus.detectionandrefactoring.integration;

import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideRefactoringTool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static fixtures.Cinneide.createAbstractFactorySample;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CinneideRefactoringToolIntegrationTest {

    private final CinneideRefactoringTool tool = new CinneideRefactoringTool();

    @Test
    @DisplayName("Should execute composed mini transformations through abstract factory flow")
    void shouldExecuteComposedMiniTransformationsThroughAbstractFactoryFlow() {
        var files = createAbstractFactorySample();
        var result = tool.applyAbstractFactory(files, Set.of("ProductA", "ProductB"), "ConcreteFactory", "AbstractFactory");

        assertTrue(result.stream().anyMatch(file -> file.getFileNameWithoutExtension().equals("ConcreteFactory")));
        assertTrue(result.stream().anyMatch(file -> file.getFileNameWithoutExtension().equals("AbstractFactory")));
        assertTrue(result.stream().anyMatch(file -> file.getFileNameWithoutExtension().equals("ProductAInterface")));
        assertTrue(result.stream().anyMatch(file -> file.getFileNameWithoutExtension().equals("ProductBInterface")));
        var abstractFactory = result.stream().filter(file -> file.getFileNameWithoutExtension().equals("AbstractFactory")).findFirst().orElseThrow();
        var client = result.stream().filter(file -> file.getFileNameWithoutExtension().equals("ClientAF")).findFirst().orElseThrow();
        assertTrue(abstractFactory.getCompilationUnit().toString().contains("getInstance"));
        assertTrue(client.getCompilationUnit().toString().contains("AbstractFactory.getInstance()"));
    }
}
