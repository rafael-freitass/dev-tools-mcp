package br.edu.ifpr.mcpserver.mcp.tools;
import java.util.Map;
import br.edu.ifpr.mcpserver.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcpserver.mcp.utils.ToolResultFactory;
import br.edu.ifpr.mcpserver.services.Base64Service;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class DecodeBase64Tool implements McpToolDefinition {
    private final Base64Service base64Service = new Base64Service();

    @Override
    public void register(SyncSpecification<SingleSessionSyncSpecification> mcp) {
        Tool tool = Tool.builder()
                .name("decode_base64")
                .description("Decodifica Base64 padrão RFC 4648 e interpreta os bytes resultantes como UTF-8; bytes UTF-8 inválidos são substituídos pelo caractere de substituição Unicode. Entrada vazia ou Base64 malformado retorna erro; o resultado é somente o texto decodificado.")
                .inputSchema(Map.of(
                        "type", "object",
                        "additionalProperties", false,
                        "properties", Map.of(
                                "base64", Map.of(
                                        "type", "string",
                                        "description", "Texto em Base64 padrão RFC 4648, sem espaços ou quebras de linha."
                                )
                        ),
                        "required", new String[]{"base64"}
                )).build();

        mcp.toolCall(tool, (exchange, request) -> {
            String base64 = (String) request.arguments().get("base64");

            try {
                String decoded = base64Service.decode(base64);

                return ToolResultFactory.success(decoded);
            } catch (IllegalArgumentException e) {
                return ToolResultFactory.error(e.getMessage());
            }
        });
    }
}