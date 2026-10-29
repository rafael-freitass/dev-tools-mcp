package br.edu.ifpr.mcpserver.main;
import java.util.List;

import br.edu.ifpr.mcpserver.mcp.tools.ValidateJsonTool;
import br.edu.ifpr.mcpserver.mcp.tools.FormatJsonTool;
import br.edu.ifpr.mcpserver.mcp.tools.EncodeBase64Tool;
import br.edu.ifpr.mcpserver.mcp.tools.DecodeBase64Tool;
import br.edu.ifpr.mcpserver.mcp.tools.CalculateSha256Tool;
import br.edu.ifpr.mcpserver.mcp.tools.GenerateUuidTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.edu.ifpr.mcpserver.mcp.utils.McpToolDefinition;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.Implementation;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import tools.jackson.databind.json.JsonMapper;

public class DevToolsMcpServer {
    private static final Logger LOG = LoggerFactory.getLogger(DevToolsMcpServer.class);

    private McpSyncServer server;

    private void registerTools(SyncSpecification<SingleSessionSyncSpecification> mcp) throws Exception {
        List<McpToolDefinition> tools = List.of(
                new ValidateJsonTool(),
                new FormatJsonTool(),
                new EncodeBase64Tool(),
                new DecodeBase64Tool(),
                new CalculateSha256Tool(),
                new GenerateUuidTool()
        );

        for (McpToolDefinition tool : tools) {
            if (tool != null) {
                tool.register(mcp);
            }
        }
    }

    private SyncSpecification<SingleSessionSyncSpecification>
    configMcpServer() {
        JacksonMcpJsonMapper jsonMapper = new JacksonMcpJsonMapper(new JsonMapper());
        StdioServerTransportProvider transportProvider = new StdioServerTransportProvider(jsonMapper);
        ServerCapabilities capabilities = ServerCapabilities.builder().tools(true).logging().build();
        Implementation implementation = Implementation.builder("DevTools MCP", "1.0.0").build();

        return McpServer.sync(transportProvider).serverInfo(implementation).capabilities(capabilities);
    }

    public DevToolsMcpServer() throws Exception {
        SyncSpecification<SingleSessionSyncSpecification> mcp = configMcpServer();

        registerTools(mcp);

        this.server = mcp.build();

        LOG.info("DevTools MCP iniciado via STDIO.");
    }

    public static void main(String[] args) throws Exception {
        new DevToolsMcpServer();
    }
}
