package br.edu.ifpr.mcpserver.mcp.tools;
import java.util.Map;
import br.edu.ifpr.mcpserver.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcpserver.mcp.utils.ToolResultFactory;
import br.edu.ifpr.mcpserver.services.Base64Service;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class EncodeBase64Tool implements McpToolDefinition {
    private final Base64Service base64Service = new Base64Service();

    @Override
    public void register(SyncSpecification<SingleSessionSyncSpecification> mcp) {
        Tool tool = Tool.builder()
                .name("encode_base64")
                .description("Codifica texto usando Base64 padrão RFC 4648 e bytes UTF-8. Texto vazio é permitido; o resultado é somente o conteúdo codificado, sem quebras de linha.")
                .inputSchema(Map.of(
                        "type", "object",
                        "additionalProperties", false,
                        "properties", Map.of(
                                "text", Map.of(
                                        "type", "string",
                                        "description", "Texto Unicode a codificar em UTF-8. Texto vazio é permitido."
                                )
                        ),
                        "required", new String[]{"text"}
                )).build();

        mcp.toolCall(tool, (exchange, request) -> {
            String text = (String) request.arguments().get("text");

            try {
                String encoded = base64Service.encode(text);

                return ToolResultFactory.success(encoded);
            } catch (IllegalArgumentException e) {
                return ToolResultFactory.error(e.getMessage());
            }
        });
    }
}
