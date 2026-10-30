package br.edu.ifpr.mcpserver.mcp.tools;
import java.util.Map;
import br.edu.ifpr.mcpserver.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcpserver.mcp.utils.ToolResultFactory;
import br.edu.ifpr.mcpserver.services.JsonService;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class FormatJsonTool implements McpToolDefinition {
    private final JsonService jsonService = new JsonService();

    @Override
    public void register(SyncSpecification<SingleSessionSyncSpecification> mcp) {
        Tool tool = Tool.builder()
                .name("format_json")
                .description("Formata um documento JSON válido com indentação e quebras de linha. Preserva o valor JSON, mas não a formatação original; texto vazio ou malformado retorna erro com diagnóstico quando disponível.")
                .inputSchema(Map.of(
                        "type", "object",
                        "additionalProperties", false,
                        "properties", Map.of(
                                "json", Map.of(
                                        "type", "string",
                                        "description", "Documento JSON completo em texto, por exemplo um objeto, array ou valor JSON primitivo."
                                )
                        ),
                        "required", new String[]{"json"}
                )).build();

        mcp.toolCall(tool, (exchange, request) -> {
            String json = (String) request.arguments().get("json");

            try {
                String formattedJson = jsonService.format(json);

                return ToolResultFactory.success(formattedJson);
            } catch (IllegalArgumentException e) {
                return ToolResultFactory.error(e.getMessage());
            }
        });
    }
}
