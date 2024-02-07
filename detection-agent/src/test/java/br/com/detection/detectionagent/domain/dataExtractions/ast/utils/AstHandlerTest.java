package br.com.detection.detectionagent.domain.dataExtractions.ast.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.javaparser.ast.body.MethodDeclaration;

@ExtendWith(MockitoExtension.class)
class AstHandlerTest {

    private AstHandler astHandler;

    @BeforeEach
    void setup() {
        this.astHandler = new AstHandler();
    }

    @Test
    @DisplayName("Shoud test method get declared fields for no fields")
    public void shouldTestMethodGetDeclaredFieldsForNoFields() {
        var node = new MethodDeclaration();

        var result = this.astHandler.getDeclaredFields(node);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Shoud test method get declared fields")
    public void shoudTestMethodGetDeclaredFields() {
        var node = new MethodDeclaration();

        var result = this.astHandler.getDeclaredFields(node);

        assertTrue(result.isEmpty());
    }

}
