package br.edu.ifpr.mcpserver.services;

import java.util.UUID;

public class UuidService {
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
