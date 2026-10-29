# 🛠️ DevTools MCP

Servidor e cliente [MCP (Model Context Protocol)](https://modelcontextprotocol.io/) desenvolvidos em Java. O servidor disponibiliza ferramentas para tarefas comuns de desenvolvimento; o cliente descobre as ferramentas pelo protocolo, solicita seus parâmetros e exibe os resultados.

> **Transporte:** STDIO · **Java:** 25 · **Build:** Apache Maven

## 📚 Conteúdo

- [Sobre o projeto](#-sobre-o-projeto)
- [Ferramentas disponíveis](#-ferramentas-disponíveis)
- [Organização do código](#-organização-do-código)
- [Pré-requisitos](#-pré-requisitos)
- [Testes](#-testes)
- [Build e execução](#-build-e-execução)
- [MCP Inspector](#-testar-com-o-mcp-inspector)
- [Claude Desktop](#-configurar-no-claude-desktop)
- [Tecnologias](#-tecnologias)

## 📌 Sobre o projeto

Este é um único projeto Maven que gera dois artefatos independentes:

| Aplicação | Responsabilidade | Artefato gerado |
|---|---|---|
| **Servidor MCP** | Registra e executa as ferramentas via STDIO | `server/devtools-mcp_server.jar` e `server/libs/` |
| **Cliente MCP** | Inicia o servidor, lista suas ferramentas e permite executá-las pelo terminal | `target/dist-client/devtools-mcp_client.jar` e `target/dist-client/libs/` |

O servidor usa STDIO para se comunicar com o cliente e com hosts MCP locais. Logs são enviados para `stderr`, mantendo o canal `stdout` disponível para as mensagens do protocolo.

## 🧰 Ferramentas disponíveis

Todas as ferramentas retornam conteúdo textual. Os argumentos indicados como obrigatórios são declarados no schema MCP.

| Ferramenta | Parâmetros | Resultado e erros |
|---|---|---|
| `validate_json` | `json` *(string, obrigatório)* | Retorna `JSON válido.` ou `JSON inválido.`. JSON vazio, em branco ou malformado resulta em `JSON inválido.`; isso é uma resposta normal da ferramenta, não um erro MCP. |
| `format_json` | `json` *(string, obrigatório)* | Retorna o JSON válido formatado com indentação. Entrada nula ou em branco retorna `JSON vazio.`; JSON inválido retorna `Não foi possível formatar o JSON informado.`. Em ambos os casos, marca o resultado como erro MCP. |
| `encode_base64` | `text` *(string, obrigatório)* | Retorna o texto codificado em Base64 usando UTF-8. Texto vazio é aceito; texto nulo retorna `O texto não pode ser nulo.` e erro MCP. |
| `decode_base64` | `base64` *(string, obrigatório)* | Retorna o conteúdo decodificado em UTF-8. Entrada nula ou em branco retorna `O Base64 não pode ser vazio.`; Base64 malformado retorna `O texto informado não é um Base64 válido.`. Ambos são erros MCP. |
| `calculate_sha256` | `text` *(string, obrigatório)* | Retorna o hash SHA-256 em hexadecimal minúsculo. Texto vazio é aceito; texto nulo retorna `O texto não pode ser nulo.` e erro MCP. |
| `generate_uuid` | Nenhum | Retorna um UUID aleatório versão 4. |

Em chamadas MCP, uma falha de validação da ferramenta é indicada por `isError=true` e acompanhada de texto explicativo. Argumentos obrigatórios ausentes também podem ser rejeitados pela validação do schema do protocolo.

## 🗂️ Organização do código

```text
src/
├── main/java/br/edu/ifpr/mcpserver/
│   ├── main/       # Entradas do cliente e do servidor
│   ├── mcp/tools/  # Definição e registro das ferramentas MCP
│   ├── mcp/utils/  # Contrato comum para registro de ferramentas
│   └── services/  # Regras de negócio (JSON, Base64, SHA-256 e UUID)
└── test/java/
    ├── main/       # Testes de integração do protocolo e das ferramentas
    └── services/   # Testes unitários dos serviços
```

As ferramentas adaptam as chamadas MCP para os serviços, que concentram as regras de cada funcionalidade.

## ✅ Pré-requisitos

- **JDK 25** instalado e configurado (`java -version`);
- **Apache Maven** instalado (`mvn -version`);
- Para o cliente interativo, terminal compatível com entrada padrão;
- Para o MCP Inspector, **Node.js 22.19 ou superior** e `npx`;
- Para Claude Desktop, aplicativo instalado e JDK disponível no `PATH`.

O projeto usa o MCP Java SDK, SLF4J/Logback e JUnit 6. Não usa Spring nem Lombok.

## 🧪 Testes

Executar os testes unitários dos serviços:

```bash
mvn test
```

Executar testes unitários e de integração. O perfil `server` gera o artefato necessário ao teste de integração, que inicia o servidor e chama as ferramentas por MCP:

```bash
mvn clean verify -Pserver
```

Os testes unitários têm o sufixo `Test`; o teste de integração tem o sufixo `IT` e é executado pelo Maven Failsafe durante a fase `verify`.

## 📦 Build e execução

Execute os comandos a partir da raiz do projeto.

### Gerar o servidor

```bash
mvn clean package -Pserver -DskipTests
```

O build cria `server/devtools-mcp_server.jar` e copia suas dependências para `server/libs/`.

### Gerar o cliente

```bash
mvn clean package -Pclient -DskipTests
```

O build cria `target/dist-client/devtools-mcp_client.jar` e copia suas dependências para `target/dist-client/libs/`.

### Executar o cliente

Gere primeiro o servidor e depois o cliente. O cliente inicia o servidor automaticamente; não é necessário abrir um processo separado.

Na raiz do projeto:

```bash
java -jar target/dist-client/devtools-mcp_client.jar
```

Ou no diretório de distribuição do cliente:

```bash
cd target/dist-client
java -jar devtools-mcp_client.jar
```

No menu, selecione a ferramenta pelo número ou nome. O cliente solicita os argumentos declarados por cada ferramenta. Digite `listar` para atualizar a lista ou `sair` para encerrar.

> O perfil Maven `server` limpa e recria a pasta `server/`. Se você gerar novamente o servidor depois do cliente, gere o cliente novamente também para manter os dois artefatos disponíveis após o `clean` do Maven.

### Eclipse IDE

1. Importe o projeto existente como **Maven Project**.
2. Clique com o botão direito no projeto e selecione **Run As → Maven build...**.
3. No campo **Goals**, informe um dos comandos Maven acima, por exemplo `clean verify -Pserver`.
4. Execute a configuração. Para gerar o outro artefato, crie outra configuração Maven com o perfil correspondente.

## 🔎 Testar com o MCP Inspector

O Inspector conecta-se ao servidor local usando STDIO e permite listar e chamar suas ferramentas pela interface web.

1. Gere o servidor conforme a seção anterior.
2. Na raiz do projeto, execute (substitua o caminho pelo caminho absoluto do JAR):

   **Linux/macOS:**

   ```bash
   npx @modelcontextprotocol/inspector java -jar "$PWD/server/devtools-mcp_server.jar"
   ```

   **Windows PowerShell:**

   ```powershell
   npx @modelcontextprotocol/inspector java -jar (Resolve-Path .\server\devtools-mcp_server.jar).Path
   ```

3. Abra no navegador a URL local exibida pelo Inspector.
4. Inicialize a conexão, abra a lista de ferramentas e execute uma ferramenta preenchendo seus parâmetros.

O primeiro uso do `npx` pode baixar o pacote do Inspector. Consulte a [documentação oficial do MCP Inspector](https://modelcontextprotocol.io/docs/tools/inspector) para opções adicionais.

## 🤖 Configurar no Claude Desktop

O servidor é local e usa STDIO. Gere o artefato do servidor e configure o Claude Desktop para iniciar o JAR diretamente.

1. No Claude Desktop, abra **Settings → Developer → Edit Config**. O aplicativo abre o arquivo `claude_desktop_config.json`.
2. Adicione a entrada `devtools-mcp` dentro de `mcpServers`, preservando quaisquer outras configurações existentes.
3. Troque o caminho do exemplo pelo caminho absoluto de `server/devtools-mcp_server.jar` na sua máquina.
4. Salve o arquivo e reinicie completamente o Claude Desktop.

Exemplo para Linux/macOS:

```json
{
  "mcpServers": {
    "devtools-mcp": {
      "command": "java",
      "args": ["-jar", "/caminho/absoluto/para/dev-tools-mcp/server/devtools-mcp_server.jar"],
      "env": {}
    }
  }
}
```

No Windows, use o caminho absoluto do JAR, escapando as barras invertidas no JSON, por exemplo:

```json
"args": ["-jar", "C:\\Users\\seu-usuario\\dev-tools-mcp\\server\\devtools-mcp_server.jar"]
```

Confirme que `java` pode ser encontrado pelo ambiente do Claude Desktop. Após reiniciar, as seis ferramentas devem aparecer entre as ferramentas MCP disponíveis. Consulte também a [documentação MCP da Anthropic](https://docs.anthropic.com/en/docs/mcp).

## 🧱 Tecnologias

- Java 25
- Apache Maven
- MCP Java SDK
- Transporte MCP STDIO
- JUnit 6
- SLF4J e Logback
- Jackson

---

Projeto desenvolvido como trabalho final da disciplina de Serviços Web.
