package br.edu.ifpr.mcpserver.services;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ObjectWriter;

public class JsonService {
    private final JsonMapper jsonMapper = new JsonMapper();
    private final ObjectWriter prettyWriter = jsonMapper.writerWithDefaultPrettyPrinter();

    public boolean validate(String json) {
        if(json == null || json.isBlank()){
            return false;
        }
        try {
            JsonNode node = jsonMapper.readTree(json);
            return node != null;
        } catch (Exception e) {
            return false;
        }
    }

    public String format(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("JSON vazio.");
        }

        try {
            JsonNode node = jsonMapper.readTree(json);

            if (node == null) {
                throw new IllegalArgumentException("JSON vazio.");
            }

            return prettyWriter.writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalArgumentException("Não foi possível formatar o JSON informado.", e);
        }
    }
}
