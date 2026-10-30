package br.edu.ifpr.mcpserver.mcp.tools;
import java.util.Map;
import br.edu.ifpr.mcpserver.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcpserver.mcp.utils.ToolResultFactory;
import br.edu.ifpr.mcpserver.services.UuidService;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class GenerateUuidTool implements McpToolDefinition {
    private final UuidService uuidService = new UuidService();

    @Override
    public void register(SyncSpecification<SingleSessionSyncSpecification> mcp) {
        Tool tool = Tool.builder()
                .name("generate_uuid")
                .description("Gera um UUID aleatório versão 4 e retorna sua representação canônica em letras minúsculas. Não recebe argumentos.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(),
                        "additionalProperties", false
                )).build();

        mcp.toolCall(tool, (exchange, request) -> {
            String uuid = uuidService.generate();
            return ToolResultFactory.success(uuid);
        });
    }
}
