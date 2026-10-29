package br.edu.ifpr.mcpserver.mcp.tools;
import java.util.Map;
import br.edu.ifpr.mcpserver.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcpserver.services.Sha256Service;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class CalculateSha256Tool implements McpToolDefinition {
    private final Sha256Service sha256Service = new Sha256Service();

    @Override
    public void register(SyncSpecification<SingleSessionSyncSpecification> mcp) {
        Tool tool = Tool.builder()
                .name("calculate_sha256")
                .description("Calcula o hash SHA-256 de um texto.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "text", Map.of(
                                        "type", "string",
                                        "description", "Texto cujo hash SHA-256 será calculado."
                                )
                        ),
                        "required", new String[]{"text"}
                )).build();

        mcp.toolCall(tool, (exchange, request) -> {
            String text = (String) request.arguments().get("text");

            try {
                String hash = sha256Service.calculate(text);
                return CallToolResult.builder().addTextContent(hash).build();
            } catch (IllegalArgumentException e) {
                return CallToolResult.builder().addTextContent(e.getMessage()).isError(true).build();
            }
        });
    }
}