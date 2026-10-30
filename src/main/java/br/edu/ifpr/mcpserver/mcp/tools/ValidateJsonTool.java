package br.edu.ifpr.mcpserver.mcp.tools;
import java.util.Map;
import br.edu.ifpr.mcpserver.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcpserver.mcp.utils.ToolResultFactory;
import br.edu.ifpr.mcpserver.services.JsonService;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ValidateJsonTool implements McpToolDefinition {
    private final JsonService jsonService = new JsonService();

    @Override
    public void register(SyncSpecification<SingleSessionSyncSpecification> mcp) {
        Tool tool = Tool.builder()
                .name("validate_json")
                .description("Valida um documento JSON. Aceita qualquer valor JSON (objeto, array, string, número, booleano ou null); texto vazio é inválido. Retorna um diagnóstico com a posição do erro quando disponível.")
                .inputSchema(Map.of(
                        "type", "object",
                        "additionalProperties", false,
                        "properties", Map.of(
                                "json", Map.of(
                                        "type", "string",
                                        "description", "Documento JSON em texto. Deve conter exatamente um valor JSON completo."
                                )
                        ),
                        "required", new String[]{"json"}
                )).build();

        mcp.toolCall(tool, (exchange, request) -> {
            String json = (String) request.arguments().get("json");
            JsonService.ValidationResult result = jsonService.validateWithDetails(json);
            return ToolResultFactory.success(result.message());
        });
    }
}