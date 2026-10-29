package br.edu.ifpr.mcpserver.main;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DevToolsMcpServerIT {
    private static final String SERVER_JAR_PATH = "server/devtools-mcp_server.jar";
    private static McpSyncClient client;

    @BeforeAll
    static void startServerAndConnectClient() {
        File serverJar = new File(SERVER_JAR_PATH);
        assertTrue(serverJar.isFile(), "O JAR do servidor deve ser criado pelo perfil Maven 'server'.");

        ServerParameters serverParameters = ServerParameters.builder("java")
                .args("-jar", serverJar.getAbsolutePath())
                .build();
        StdioClientTransport transport = new StdioClientTransport(
                serverParameters,
                new JacksonMcpJsonMapper(new JsonMapper())
        );

        client = McpClient.sync(transport).build();
        client.initialize();
    }

    @AfterAll
    static void closeClient() {
        if (client != null) {
            client.closeGracefully();
        }
    }

    @Test
    void shouldInitializeAndExposeAllTools() {
        ListToolsResult result = client.listTools();
        Set<String> toolNames = result.tools().stream()
                .map(Tool::name)
                .collect(Collectors.toSet());

        assertEquals(6, result.tools().size());
        assertEquals(Set.of(
                "validate_json",
                "format_json",
                "encode_base64",
                "decode_base64",
                "calculate_sha256",
                "generate_uuid"
        ), toolNames);
        assertTrue(result.tools().stream().allMatch(tool ->
                tool.description() != null && !tool.description().isBlank()));
    }

    @Test
    void shouldValidateAndFormatJson() {
        CallToolResult validationResult = callTool("validate_json", Map.of("json", "{\"ok\":true}"));
        assertFalse(Boolean.TRUE.equals(validationResult.isError()));
        assertEquals("JSON válido.", textOf(validationResult));

        CallToolResult formatResult = callTool("format_json", Map.of("json", "{\"ok\":true}"));
        assertFalse(Boolean.TRUE.equals(formatResult.isError()));
        assertTrue(textOf(formatResult).contains("\n"));
    }

    @Test
    void shouldReportInvalidJsonFormattingAsToolError() {
        CallToolResult result = callTool("format_json", Map.of("json", "{invalido}"));

        assertTrue(Boolean.TRUE.equals(result.isError()));
        assertNotNull(textOf(result));
    }

    @Test
    void shouldReportInvalidJsonWithoutToolError() {
        CallToolResult result = callTool("validate_json", Map.of("json", "{invalido}"));

        assertFalse(Boolean.TRUE.equals(result.isError()));
        assertEquals("JSON inválido.", textOf(result));
    }

    @Test
    void shouldEncodeAndDecodeBase64() {
        CallToolResult encodedResult = callTool("encode_base64", Map.of("text", "Olá, mundo!"));
        assertFalse(Boolean.TRUE.equals(encodedResult.isError()));
        assertEquals("T2zDoSwgbXVuZG8h", textOf(encodedResult));

        CallToolResult decodedResult = callTool("decode_base64", Map.of("base64", textOf(encodedResult)));
        assertFalse(Boolean.TRUE.equals(decodedResult.isError()));
        assertEquals("Olá, mundo!", textOf(decodedResult));
    }

    @Test
    void shouldReportInvalidBase64AsToolError() {
        CallToolResult result = callTool("decode_base64", Map.of("base64", "inválido!"));

        assertTrue(Boolean.TRUE.equals(result.isError()));
        assertTrue(textOf(result).contains("Base64 válido"));
    }

    @Test
    void shouldReportEmptyBase64AsToolError() {
        CallToolResult result = callTool("decode_base64", Map.of("base64", "   "));

        assertTrue(Boolean.TRUE.equals(result.isError()));
        assertTrue(textOf(result).contains("Base64 não pode ser vazio"));
    }

    @Test
    void shouldCalculateSha256() {
        CallToolResult result = callTool("calculate_sha256", Map.of("text", "abc"));

        assertFalse(Boolean.TRUE.equals(result.isError()));
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", textOf(result));
    }

    @Test
    void shouldGenerateUuidVersionFour() {
        CallToolResult result = callTool("generate_uuid", Map.of());

        assertFalse(Boolean.TRUE.equals(result.isError()));
        UUID uuid = UUID.fromString(textOf(result));
        assertEquals(4, uuid.version());
        assertEquals(2, uuid.variant());
    }

    private static CallToolResult callTool(String name, Map<String, Object> arguments) {
        CallToolRequest request = CallToolRequest.builder(name).arguments(arguments).build();

        return client.callTool(request);
    }

    private static String textOf(CallToolResult result) {
        assertFalse(result.content().isEmpty(), "A resposta da tool deve conter texto.");
        assertTrue(result.content().getFirst() instanceof TextContent,
                "A resposta da tool deve ser conteúdo textual.");
        return ((TextContent) result.content().getFirst()).text();
    }
}
