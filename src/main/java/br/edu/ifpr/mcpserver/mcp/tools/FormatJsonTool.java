package br.edu.ifpr.mcpserver.mcp.tools;
import java.util.Map;
import br.edu.ifpr.mcpserver.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcpserver.services.JsonService;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class FormatJsonTool implements McpToolDefinition {
    private final JsonService jsonService = new JsonService();

    @Override
    public void register(SyncSpecification<SingleSessionSyncSpecification> mcp) {
        Tool tool = Tool.builder()
                .name("format_json")
                .description("Formata um texto JSON com indentação e quebras de linha.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "json", Map.of(
                                        "type", "string",
                                        "description", "Texto JSON que será formatado."
                                )
                        ),
                        "required", new String[]{"json"}
                )).build();

        mcp.toolCall(tool, (exchange, request) -> {
            String json = (String) request.arguments().get("json");

            try {
                String formattedJson = jsonService.format(json);

                return CallToolResult.builder().addTextContent(formattedJson).build();
            } catch (IllegalArgumentException e) {
                return CallToolResult.builder().addTextContent(e.getMessage()).isError(true).build();
            }
        });
    }
}