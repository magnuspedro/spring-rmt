package br.com.magnus.config.starter.projects;

import br.com.magnus.config.starter.file.JavaFile;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChangedFilesAnalyzerTest {

    @Test
    void shouldGroupLinkedChangedFilesTogether() {
        var analysis = ChangedFilesAnalyzer.analyze(List.of(
                javaFile("src/main/java/example/Checkout.java", """
                        package example;

                        public class Checkout {
                            private PaymentStrategy paymentStrategy;
                        }
                        """),
                javaFile("src/main/java/example/PaymentStrategy.java", """
                        package example;

                        public interface PaymentStrategy {
                        }
                        """)),
                new LinkedHashSet<>(List.of(
                        "src/main/java/example/Checkout.java",
                        "src/main/java/example/PaymentStrategy.java")));

        assertThat(analysis.relatedFiles("src/main/java/example/Checkout.java"))
                .containsExactly("src/main/java/example/PaymentStrategy.java");
        assertThat(analysis.groupedFiles("src/main/java/example/Checkout.java"))
                .containsExactly(
                        "src/main/java/example/Checkout.java",
                        "src/main/java/example/PaymentStrategy.java");
        assertThat(analysis.groupedFiles("src/main/java/example/PaymentStrategy.java"))
                .containsExactly(
                        "src/main/java/example/Checkout.java",
                        "src/main/java/example/PaymentStrategy.java");
    }

    @Test
    void shouldKeepIndependentChangedFilesSeparated() {
        var analysis = ChangedFilesAnalyzer.analyze(List.of(
                javaFile("src/main/java/example/IndependentOne.java", """
                        package example;

                        public class IndependentOne {
                        }
                        """),
                javaFile("src/main/java/example/IndependentTwo.java", """
                        package example;

                        public class IndependentTwo {
                        }
                        """)),
                new LinkedHashSet<>(List.of(
                        "src/main/java/example/IndependentOne.java",
                        "src/main/java/example/IndependentTwo.java")));

        assertThat(analysis.relatedFiles("src/main/java/example/IndependentOne.java")).isEmpty();
        assertThat(analysis.groupedFiles("src/main/java/example/IndependentOne.java"))
                .containsExactly("src/main/java/example/IndependentOne.java");
        assertThat(analysis.groupedFiles("src/main/java/example/IndependentTwo.java"))
                .containsExactly("src/main/java/example/IndependentTwo.java");
    }

    private JavaFile javaFile(String fullName, String content) {
        var lastSlash = fullName.lastIndexOf('/') + 1;
        return JavaFile.builder()
                .name(fullName.substring(lastSlash))
                .path(fullName.substring(0, lastSlash))
                .originalClass(content)
                .build();
    }
}
