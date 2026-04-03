package fixtures;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.AbstractSyntaxTree;

import java.util.ArrayList;
import java.util.List;

public class Cinneide {

    public static List<JavaFile> createFactoryMethodSample() {
        var product = """
                package cinneide;
                public class Product {
                    public String value() { return "ok"; }
                }
                """;
        var creator = """
                package cinneide;
                public class Creator {
                    public Product build() { return new Product(); }
                }
                """;
        return new ArrayList<>(List.of(
                JavaFile.builder().name("Product.java").path("cinneide/").originalClass(product).parsed(AbstractSyntaxTree.parseSingle(product)).build(),
                JavaFile.builder().name("Creator.java").path("cinneide/").originalClass(creator).parsed(AbstractSyntaxTree.parseSingle(creator)).build()
        ));
    }

    public static List<JavaFile> createSingletonSample() {
        var service = """
                package cinneide;
                public class Service {
                    public String run() { return "ok"; }
                }
                """;
        var client = """
                package cinneide;
                public class Client {
                    public Service create() { return new Service(); }
                }
                """;
        return new ArrayList<>(List.of(
                JavaFile.builder().name("Service.java").path("cinneide/").originalClass(service).parsed(AbstractSyntaxTree.parseSingle(service)).build(),
                JavaFile.builder().name("Client.java").path("cinneide/").originalClass(client).parsed(AbstractSyntaxTree.parseSingle(client)).build()
        ));
    }

    public static List<JavaFile> createAbstractFactorySample() {
        var productA = """
                package cinneide;
                public class ProductA {
                    public String id() { return "A"; }
                }
                """;
        var productB = """
                package cinneide;
                public class ProductB {
                    public String id() { return "B"; }
                }
                """;
        var client = """
                package cinneide;
                public class ClientAF {
                    public ProductA createA() { return new ProductA(); }
                    public ProductB createB() { return new ProductB(); }
                }
                """;
        return new ArrayList<>(List.of(
                JavaFile.builder().name("ProductA.java").path("cinneide/").originalClass(productA).parsed(AbstractSyntaxTree.parseSingle(productA)).build(),
                JavaFile.builder().name("ProductB.java").path("cinneide/").originalClass(productB).parsed(AbstractSyntaxTree.parseSingle(productB)).build(),
                JavaFile.builder().name("ClientAF.java").path("cinneide/").originalClass(client).parsed(AbstractSyntaxTree.parseSingle(client)).build()
        ));
    }

    public static List<JavaFile> createStrategySample() {
        var context = """
                package cinneide;
                public class PricingContext {
                    public int percentage(int value) { return value / 10; }
                    public int discount(int value) { return value - 5; }
                }
                """;
        return new ArrayList<>(List.of(
                JavaFile.builder().name("PricingContext.java").path("cinneide/").originalClass(context).parsed(AbstractSyntaxTree.parseSingle(context)).build()
        ));
    }

    public static List<JavaFile> createBridgeSample() {
        var iface = """
                package cinneide;
                public interface Implementor {
                    String op();
                }
                """;
        var impl = """
                package cinneide;
                public class ConcreteImplementor implements Implementor {
                    public String op() { return "ok"; }
                }
                """;
        var client = """
                package cinneide;
                public class BridgeClient {
                    public Implementor build() { return new ConcreteImplementor(); }
                }
                """;
        return new ArrayList<>(List.of(
                JavaFile.builder().name("Implementor.java").path("cinneide/").originalClass(iface).parsed(AbstractSyntaxTree.parseSingle(iface)).build(),
                JavaFile.builder().name("ConcreteImplementor.java").path("cinneide/").originalClass(impl).parsed(AbstractSyntaxTree.parseSingle(impl)).build(),
                JavaFile.builder().name("BridgeClient.java").path("cinneide/").originalClass(client).parsed(AbstractSyntaxTree.parseSingle(client)).build()
        ));
    }
}
