package br.edu.ifpr.mcpserver.mcp.tools;
import java.util.Map;
import br.edu.ifpr.mcpserver.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcpserver.services.Base64Service;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class DecodeBase64Tool implements McpToolDefinition {
    private final Base64Service base64Service = new Base64Service();

    @Override
    public void register(SyncSpecification<SingleSessionSyncSpecification> mcp) {
        Tool tool = Tool.builder()
                .name("decode_base64")
                .description("Decodifica um texto em Base64 para seu conteúdo original.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "base64", Map.of(
                                        "type", "string",
                                        "description", "Texto em Base64 que será decodificado."
                                )
                        ),
                        "required", new String[]{"base64"}
                )).build();

        mcp.toolCall(tool, (exchange, request) -> {
            String base64 = (String) request.arguments().get("base64");

            try {
                String decoded = base64Service.decode(base64);

                return CallToolResult.builder().addTextContent(decoded).build();
            } catch (IllegalArgumentException e) {
                return CallToolResult.builder().addTextContent(e.getMessage()).isError(true).build();
            }
        });
    }
}