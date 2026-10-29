package br.edu.ifpr.mcpserver.mcp.tools;
import java.util.Map;
import br.edu.ifpr.mcpserver.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcpserver.services.Base64Service;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class EncodeBase64Tool implements McpToolDefinition {
    private final Base64Service base64Service = new Base64Service();

    @Override
    public void register(SyncSpecification<SingleSessionSyncSpecification> mcp) {
        Tool tool = Tool.builder()
                .name("encode_base64")
                .description("Codifica um texto em Base64.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "text", Map.of(
                                        "type", "string",
                                        "description", "Texto que será codificado."
                                )
                        ),
                        "required", new String[]{"text"}
                )).build();

        mcp.toolCall(tool, (exchange, request) -> {
            String text = (String) request.arguments().get("text");

            try {
                String encoded = base64Service.encode(text);

                return CallToolResult.builder().addTextContent(encoded).build();
            } catch (IllegalArgumentException e) {
                return CallToolResult.builder().addTextContent(e.getMessage()).isError(true).build();
            }
        });
    }
}