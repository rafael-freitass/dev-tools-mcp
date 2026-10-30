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
    void shouldRejectNullJson() {
        assertFalse(jsonService.validate(null));
    }

    @Test
    void shouldRejectWhitespaceOnlyJson() {
        assertFalse(jsonService.validate("   \n\t"));
    }

    @Test
    void shouldReturnParseDiagnosticForInvalidJson() {
        JsonService.ValidationResult result = jsonService.validateWithDetails("{\"name\": }");

        assertFalse(result.valid());
        assertTrue(result.message().startsWith("JSON inválido"), result.message());
        assertTrue(result.message().matches("(?s).*linha 1, coluna \\d+.*"), result.message());
    }

    @Test
    void shouldDescribeEmptyJsonAsInvalid() {
        assertEquals("JSON vazio.", jsonService.validateWithDetails("  ").message());
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
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jsonService.format("{nome: Rafael}")
        );
        assertTrue(exception.getMessage().startsWith("JSON inválido"));
    }

    @Test
    void shouldThrowExceptionWhenFormattingNullJson() {
        assertThrows(IllegalArgumentException.class, () -> jsonService.format(null));
    }

    @Test
    void shouldThrowExceptionWhenFormattingWhitespaceOnlyJson() {
        assertThrows(IllegalArgumentException.class, () -> jsonService.format("   \n\t"));
    }
}
