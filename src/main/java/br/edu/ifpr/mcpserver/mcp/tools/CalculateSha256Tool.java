package br.edu.ifpr.mcpserver.mcp.tools;
import java.util.Map;
import br.edu.ifpr.mcpserver.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcpserver.mcp.utils.ToolResultFactory;
import br.edu.ifpr.mcpserver.services.Sha256Service;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class CalculateSha256Tool implements McpToolDefinition {
    private final Sha256Service sha256Service = new Sha256Service();

    @Override
    public void register(SyncSpecification<SingleSessionSyncSpecification> mcp) {
        Tool tool = Tool.builder()
                .name("calculate_sha256")
                .description("Calcula SHA-256 dos bytes UTF-8 do texto e retorna 64 caracteres hexadecimais minúsculos. Texto vazio é permitido. SHA-256 não é adequado para armazenar senhas.")
                .inputSchema(Map.of(
                        "type", "object",
                        "additionalProperties", false,
                        "properties", Map.of(
                                "text", Map.of(
                                        "type", "string",
                                        "description", "Texto Unicode cujos bytes UTF-8 serão usados no cálculo. Texto vazio é permitido."
                                )
                        ),
                        "required", new String[]{"text"}
                )).build();

        mcp.toolCall(tool, (exchange, request) -> {
            String text = (String) request.arguments().get("text");

            try {
                String hash = sha256Service.calculate(text);
                return ToolResultFactory.success(hash);
            } catch (IllegalArgumentException e) {
                return ToolResultFactory.error(e.getMessage());
            }
        });
    }
}
