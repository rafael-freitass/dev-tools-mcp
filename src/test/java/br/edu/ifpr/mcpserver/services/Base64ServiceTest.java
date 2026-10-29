package br.edu.ifpr.mcpserver.services;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Base64ServiceTest {
    private final Base64Service service = new Base64Service();

    @Test
    void shouldEncodeText() {
        String result = service.encode("Hello");

        assertEquals("SGVsbG8=", result);
    }

    @Test
    void shouldDecodeBase64() {
        String result = service.decode("SGVsbG8=");

        assertEquals("Hello", result);
    }

    @Test
    void shouldEncodeTextWithAccents() {
        String result = service.encode("Olá, mundo!");

        assertEquals("T2zDoSwgbXVuZG8h", result);
    }

    @Test
    void shouldRejectInvalidBase64() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.decode("isso não é base64!")
        );
    }

    @Test
    void shouldRejectEmptyBase64() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.decode("")
        );
    }
}