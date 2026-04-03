package br.com.magnus.detectionandrefactoring.consumer.refactor.methods.cinneide;

import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideRefactoringTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static fixtures.Cinneide.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CinneideRefactoringToolTest {

    private CinneideRefactoringTool tool;

    @BeforeEach
    void setup() {
        tool = new CinneideRefactoringTool();
    }

    @Test
    @DisplayName("Should apply factory method transformation")
    void shouldApplyFactoryMethodTransformation() {
        var files = createFactoryMethodSample();
        var result = tool.applyFactoryMethod(files, "Creator", "Product", "ProductInterface", "AbstractCreator", "createProduct");

        assertTrue(result.stream().anyMatch(file -> file.getFileNameWithoutExtension().equals("ProductInterface")));
        assertTrue(result.stream().anyMatch(file -> file.getFileNameWithoutExtension().equals("AbstractCreator")));
        var creator = result.stream().filter(file -> file.getFileNameWithoutExtension().equals("Creator")).findFirst().orElseThrow();
        assertTrue(creator.getCompilationUnit().toString().contains("extends AbstractCreator"));
        assertTrue(creator.getCompilationUnit().toString().contains("createProduct"));
    }

    @Test
    @DisplayName("Should apply singleton transformation")
    void shouldApplySingletonTransformation() {
        var files = createSingletonSample();
        var result = tool.applySingleton(files, "Service", "AbstractService");

        assertTrue(result.stream().anyMatch(file -> file.getFileNameWithoutExtension().equals("AbstractService")));
        var abstractService = result.stream().filter(file -> file.getFileNameWithoutExtension().equals("AbstractService")).findFirst().orElseThrow();
        assertTrue(abstractService.getCompilationUnit().toString().contains("static AbstractService getInstance()"));
        var client = result.stream().filter(file -> file.getFileNameWithoutExtension().equals("Client")).findFirst().orElseThrow();
        assertTrue(client.getCompilationUnit().toString().contains("AbstractService.getInstance()"));
    }

    @Test
    @DisplayName("Should apply abstract factory transformation")
    void shouldApplyAbstractFactoryTransformation() {
        var files = createAbstractFactorySample();
        var result = tool.applyAbstractFactory(files, Set.of("ProductA", "ProductB"), "ConcreteFactory", "AbstractFactory");

        assertTrue(result.stream().anyMatch(file -> file.getFileNameWithoutExtension().equals("ConcreteFactory")));
        assertTrue(result.stream().anyMatch(file -> file.getFileNameWithoutExtension().equals("AbstractFactory")));
        var client = result.stream().filter(file -> file.getFileNameWithoutExtension().equals("ClientAF")).findFirst().orElseThrow();
        assertTrue(client.getCompilationUnit().toString().contains("AbstractFactory.getInstance().createProductA()"));
        assertTrue(client.getCompilationUnit().toString().contains("AbstractFactory.getInstance().createProductB()"));
    }

    @Test
    @DisplayName("Should apply strategy transformation")
    void shouldApplyStrategyTransformation() {
        var files = createStrategySample();
        var result = tool.applyStrategy(files, "PricingContext", Set.of("percentage", "discount"), "PricingStrategy");

        assertTrue(result.stream().anyMatch(file -> file.getFileNameWithoutExtension().equals("PricingStrategy")));
        assertTrue(result.stream().anyMatch(file -> file.getFileNameWithoutExtension().equals("PricingStrategyInterface")));
        var contextClass = result.stream().filter(file -> file.getFileNameWithoutExtension().equals("PricingContext")).findFirst().orElseThrow();
        assertTrue(contextClass.getCompilationUnit().toString().contains("private final PricingStrategyInterface delegation"));
        assertTrue(contextClass.getCompilationUnit().toString().contains("delegation.percentage"));
    }

    @Test
    @DisplayName("Should apply bridge transformation")
    void shouldApplyBridgeTransformation() {
        var files = createBridgeSample();
        var result = tool.applyBridge(files, Set.of("BridgeClient"), "Implementor", "ImplementorBridge");

        assertTrue(result.stream().anyMatch(file -> file.getFileNameWithoutExtension().equals("ImplementorBridge")));
        var bridge = result.stream().filter(file -> file.getFileNameWithoutExtension().equals("ImplementorBridge")).findFirst().orElseThrow();
        assertTrue(bridge.getCompilationUnit().toString().contains("implements Implementor"));
        var client = result.stream().filter(file -> file.getFileNameWithoutExtension().equals("BridgeClient")).findFirst().orElseThrow();
        assertTrue(client.getCompilationUnit().toString().contains("new ImplementorBridge(new ConcreteImplementor())"));
        assertEquals(4, result.size());
    }
}
