package br.edu.ifpr.mcpserver.mcp.tools;
import java.util.Map;
import br.edu.ifpr.mcpserver.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcpserver.services.JsonService;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ValidateJsonTool implements McpToolDefinition {
    private final JsonService jsonService = new JsonService();

    @Override
    public void register(SyncSpecification<SingleSessionSyncSpecification> mcp) {
        Tool tool = Tool.builder()
                .name("validate_json")
                .description("Verifica se um texto contém um JSON válido.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "json", Map.of(
                                        "type", "string",
                                        "description", "Texto JSON que será validado."
                                )
                        ),
                        "required", new String[]{"json"}
                )).build();

        mcp.toolCall(tool, (exchange, request) -> {
            String json = (String) request.arguments().get("json");
            boolean valid = jsonService.validate(json);
            String message = valid ? "JSON válido." : "JSON inválido.";

            return CallToolResult.builder().addTextContent(message).build();
        });
    }
}
