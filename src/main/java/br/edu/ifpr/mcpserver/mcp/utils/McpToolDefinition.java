package br.edu.ifpr.mcpserver.mcp.utils;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;

public interface McpToolDefinition {
    void register(
            io.modelcontextprotocol.server.McpServer.SyncSpecification<SingleSessionSyncSpecification> mcp)
            throws Exception;
}