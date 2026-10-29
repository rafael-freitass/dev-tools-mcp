package br.edu.ifpr.mcpserver.services;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JsonServiceTest {
    private final JsonService jsonService = new JsonService();

    @Test
    void shouldValidateCorrectJson() {
        assertTrue(jsonService.validate("{\"nome\":\"Rafael\",\"idade\":22}"));
    }

    @Test
    void shouldRejectInvalidJson() {
        assertFalse(jsonService.validate("{nome: Rafael}"));
    }

    @Test
    void shouldRejectEmptyJson() {
        assertFalse(jsonService.validate(""));
    }

    @Test
    void shouldFormatJson() {
        String json = "{\"nome\":\"Rafael\",\"idade\":22}";

        String formatted = jsonService.format(json);

        assertTrue(formatted.contains("\n"));
        assertTrue(formatted.contains("\"nome\""));
        assertTrue(formatted.contains("\"idade\""));
    }

    @Test
    void shouldThrowExceptionWhenFormattingInvalidJson() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jsonService.format("{nome: Rafael}")
        );
    }
}