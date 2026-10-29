package br.edu.ifpr.mcpserver.services;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class UuidServiceTest {
    private final UuidService service = new UuidService();

    @Test
    void shouldGenerateAValidVersionFourUuid() {
        String value = service.generate();
        UUID uuid = UUID.fromString(value);

        assertEquals(4, uuid.version());
        assertEquals(2, uuid.variant());
        assertEquals(value, uuid.toString());
    }

    @Test
    void shouldGenerateDifferentUuids() {
        assertNotEquals(service.generate(), service.generate());
    }
}
