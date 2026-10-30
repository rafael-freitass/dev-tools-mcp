package br.edu.ifpr.mcpserver.services;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.core.JacksonException;

public class JsonService {
    private final JsonMapper jsonMapper = new JsonMapper();
    private final ObjectWriter prettyWriter = jsonMapper.writerWithDefaultPrettyPrinter();

    public boolean validate(String json) {
        return validateWithDetails(json).valid();
    }

    public ValidationResult validateWithDetails(String json) {
        if (json == null || json.isBlank()) {
            return new ValidationResult(false, "JSON vazio.");
        }

        try {
            JsonNode node = jsonMapper.readTree(json);
            return node == null
                    ? new ValidationResult(false, "JSON vazio.")
                    : new ValidationResult(true, "JSON válido.");
        } catch (JacksonException e) {
            return new ValidationResult(false, formatParseError(e));
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
        } catch (JacksonException e) {
            throw new IllegalArgumentException(formatParseError(e), e);
        }
    }

    private String formatParseError(JacksonException exception) {
        String detail = exception.getMessage();
        if (detail == null || detail.isBlank()) {
            detail = "JSON inválido.";
        }

        String location = exception.getLocation() == null
                ? ""
                : " (linha " + exception.getLocation().getLineNr()
                        + ", coluna " + exception.getLocation().getColumnNr() + ")";
        return "JSON inválido" + location + ": " + detail;
    }

    public record ValidationResult(boolean valid, String message) { }
}
