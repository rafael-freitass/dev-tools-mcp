package br.edu.ifpr.mcpserver.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Sha256ServiceTest {
    private final Sha256Service service = new Sha256Service();

    @Test
    void shouldCalculateSha256ForText() {
        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                service.calculate("abc")
        );
    }

    @Test
    void shouldCalculateSha256ForEmptyText() {
        assertEquals(
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                service.calculate("")
        );
    }

    @Test
    void shouldEncodeTextAsUtf8BeforeCalculatingSha256() {
        assertEquals(
                "5251c3c68010bca32f4dbedb142270f720fa84a0599816524c85d50c3f589365",
                service.calculate("Olá")
        );
    }

    @Test
    void shouldRejectNullText() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate(null));
    }
}
