package br.edu.ifpr.mcpserver.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class Sha256Service {
    public String calculate(String text) {
        if (text == null) {
            throw new IllegalArgumentException("O texto não pode ser nulo.");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("O algoritmo SHA-256 não está disponível.", e);
        }
    }
}
