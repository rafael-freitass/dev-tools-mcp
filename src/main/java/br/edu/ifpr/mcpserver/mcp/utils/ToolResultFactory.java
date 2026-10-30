package br.edu.ifpr.mcpserver.mcp.utils;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

public final class ToolResultFactory {
    private ToolResultFactory() { }

    public static CallToolResult success(String text) {
        return CallToolResult.builder().addTextContent(text).build();
    }

    public static CallToolResult error(String message) {
        return CallToolResult.builder().addTextContent(message).isError(true).build();
    }
}