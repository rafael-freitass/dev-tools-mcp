package br.edu.ifpr.mcpserver.main;
import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import tools.jackson.databind.json.JsonMapper;

public class DevToolsMcpClient {
    private static final Logger LOG = LoggerFactory.getLogger(DevToolsMcpClient.class);
    private static final String SERVER_JAR_NAME = "devtools-mcp_server.jar";
    private final McpSyncClient client;

    private McpSyncClient createClient() {
        File serverJar = findServerJar();

        if (!serverJar.isFile()) {
            throw new IllegalStateException(
                            "JAR do servidor não encontrado: "
                            + serverJar.getAbsolutePath()
                            + "\nExecute primeiro o perfil Maven 'server'.");
        }

        ServerParameters params = ServerParameters.builder("java")
                .args("-jar", serverJar.getAbsolutePath())
                .build();

        JacksonMcpJsonMapper jsonMapper = new JacksonMcpJsonMapper(new JsonMapper());
        StdioClientTransport transport = new StdioClientTransport(params, jsonMapper);

        transport.setStdErrorHandler(line -> LOG.info("[SERVER STDERR] {}", line));

        return McpClient.sync(transport).build();
    }

    private File findServerJar() {
        Path workingDirectory = Path.of("").toAbsolutePath();
        Path[] candidates = {
                workingDirectory.resolve("server").resolve(SERVER_JAR_NAME),
                workingDirectory.resolve("..").resolve("..").resolve("server").resolve(SERVER_JAR_NAME)
        };

        for (Path candidate : candidates) {
            File serverJar = candidate.normalize().toFile();
            if (serverJar.isFile()) {
                return serverJar;
            }
        }

        return candidates[0].normalize().toFile();
    }

    private void execute() {
        client.initialize();
        listSelectRunToolLooping();
    }

    private void close() {
        LOG.info("Fechando cliente MCP...");
        client.closeGracefully();
    }

    private void listSelectRunToolLooping() {
        try (Scanner scanner = new Scanner(System.in)) {
            ListToolsResult tools = loadTools();

            while (true) {
                showMenu(tools);

                System.out.print("> ");

                if (!scanner.hasNextLine()) {
                    break;
                }

                String command = scanner.nextLine().trim();

                if (command.isBlank()) {
                    continue;
                }

                if ("sair".equalsIgnoreCase(command)) {
                    break;
                }

                if ("listar".equalsIgnoreCase(command)) {
                    tools = loadTools();
                    continue;
                }

                Tool selectedTool = selectTool(tools.tools(), command);

                if (selectedTool == null) {
                    System.out.println("Ferramenta não encontrada.");
                    continue;
                }

                executeTool(selectedTool, scanner);
            }
        }
    }

    private ListToolsResult loadTools() {
        ListToolsResult result = client.listTools();

        LOG.info("Servidor disponibilizou {} ferramenta(s).", result.tools().size());

        return result;
    }

    private void showMenu(ListToolsResult tools) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("           DEVTOOLS MCP");
        System.out.println("========================================");

        if (tools.tools().isEmpty()) {
            System.out.println("Nenhuma ferramenta registrada.");
        } else {
            System.out.println("Ferramentas disponíveis:");

            for (int i = 0; i < tools.tools().size(); i++) {
                Tool tool = tools.tools().get(i);

                System.out.printf("%d - %s%n", i + 1, tool.name());

                if (tool.description() != null && !tool.description().isBlank()) {
                    System.out.println("  " + tool.description());
                }
            }
        }

        System.out.println();
        System.out.println("Digite o número ou o nome da ferramenta.");
        System.out.println("listar - atualizar a lista");
        System.out.println("sair   - encerrar o cliente");
    }

    private Tool selectTool(List<Tool> tools, String command) {
        try {
            int index = Integer.parseInt(command) - 1;

            if (index >= 0 && index < tools.size()) {
                return tools.get(index);
            }
        } catch (NumberFormatException ignored) {
            // O comando pode ser o nome da ferramenta.
        }

        for (Tool tool : tools) {
            if (tool.name().equalsIgnoreCase(command)) {
                return tool;
            }
        }

        return null;
    }

    private void executeTool(Tool tool, Scanner scanner) {
        System.out.println();
        System.out.println("Ferramenta: " + tool.name());

        Map<String, Object> arguments = readArguments(tool, scanner);

        CallToolRequest request = CallToolRequest.builder(tool.name()).arguments(arguments).build();

        try {
            CallToolResult result = client.callTool(request);
            showResult(result);
        } catch (Exception ex) {
            LOG.error("Erro ao executar a ferramenta '{}'.", tool.name(), ex);
            System.out.println("Falha ao executar a ferramenta.");
        }
    }

    private Map<String, Object> readArguments(Tool tool, Scanner scanner) {
        Map<String, Object> arguments = new LinkedHashMap<>();
        Map<String, Object> schema = tool.inputSchema();

        if (schema == null || schema.isEmpty()) {
            return arguments;
        }

        Object propertiesObject = schema.get("properties");

        if (!(propertiesObject instanceof Map<?, ?> properties)) {
            return arguments;
        }

        Object requiredObject = schema.get("required");

        List<?> required = requiredObject instanceof List<?> list ? list : List.of();

        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            String name = String.valueOf(entry.getKey());

            if (!(entry.getValue() instanceof Map<?, ?> property)) {
                continue;
            }

            Object descriptionValue = property.get("description");
            String description = descriptionValue == null ? "" : String.valueOf(descriptionValue);

            boolean isRequired = required.contains(name);

            while (true) {
                System.out.println();
                System.out.println(name + ": " + description);

                if (!isRequired) {
                    System.out.print("Valor (Enter para ignorar): ");
                } else {
                    System.out.print("Valor obrigatório: ");
                }

                if (!scanner.hasNextLine()) {
                    return arguments;
                }

                String value = scanner.nextLine();

                if (value.isBlank()) {
                    if (!isRequired) {
                        break;
                    }

                    System.out.println("Este parâmetro é obrigatório.");
                    continue;
                }

                arguments.put(name, value);
                break;
            }
        }

        return arguments;
    }

    private void showResult(CallToolResult result) {
        System.out.println();
        System.out.println("=============== RESULTADO ===============");
        System.out.println("Falha na execução da ferramenta: " + (result.isError() ? "Sim" : "Não"));

        result.content().forEach(content -> System.out.println(content));

        if (result.structuredContent() != null) {
            System.out.println(result.structuredContent());
        }

        System.out.println("=========================================");
    }

    public DevToolsMcpClient() {
        this.client = createClient();
    }

    public static void main(String[] args) {
        DevToolsMcpClient app = new DevToolsMcpClient();

        try {
            app.execute();
        } finally {
            app.close();
        }
    }
}
