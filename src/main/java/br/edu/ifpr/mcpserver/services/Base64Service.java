package br.edu.ifpr.mcpserver.services;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Service {
    public String encode(String text) {
        if (text == null) {
            throw new IllegalArgumentException("O texto não pode ser nulo.");
        }

        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    public String decode(String base64) {
        if (base64 == null || base64.isBlank()) {
            throw new IllegalArgumentException("O Base64 não pode ser vazio.");
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("O texto informado não é um Base64 válido.", e);
        }
    }
}