# Plano de melhorias — DevTools MCP

## Objetivo

Evoluir o projeto de uma coleção pequena de utilitários para um servidor MCP útil no fluxo de desenvolvimento: ferramentas confiáveis e bem descritas, resources que expliquem o catálogo e seus limites, prompts que orientem tarefas comuns e integração clara com clientes MCP.

O plano está dividido em etapas que podem ser implementadas e revisadas em commits separados. Os títulos sugeridos são mensagens de commit; não representam commits já realizados.

## Estado atual

- Servidor MCP Java com transporte STDIO.
- Seis ferramentas: validar/formatar JSON, codificar/decodificar Base64, calcular SHA-256 e gerar UUID.
- Cliente de terminal que lista e executa as ferramentas.
- Serviços separados dos adaptadores MCP e testes unitários/de integração existentes.
- Ainda não há resources nem prompts registrados.
- As ferramentas retornam principalmente texto simples. O servidor anuncia capabilities de tools e logging.

## Princípios para a evolução

1. **Utilidade antes de quantidade:** priorizar tarefas frequentes de desenvolvimento e fluxos que combinem ferramentas existentes.
2. **Contratos claros:** schemas, descrições, resultados e erros devem ser previsíveis para pessoas e modelos.
3. **Segurança por padrão:** qualquer futura ferramenta que leia arquivos ou execute comandos precisa de limites explícitos, validação de entrada e comportamento seguro.
4. **MCP sem promessas falsas:** tools executam ações, resources fornecem contexto e prompts sugerem fluxos. O host/modelo decide quando chamar ferramentas; o servidor não consegue obrigar todo cliente a chamar a DevTools primeiro.
5. **Cada etapa verificável:** atualizar testes e documentação junto com a funcionalidade correspondente.

## Etapas e commits sugeridos

### 1. Consolidar contratos e qualidade das ferramentas atuais

**Commit sugerido:** `refactor: padroniza contratos e respostas das ferramentas`

**Objetivo:** deixar a base atual consistente antes de adicionar capacidades.

**Escopo:**

	- Revisar nomes, descrições e schemas de entrada de todas as tools; descrever formato, codificação e comportamento para entradas vazias/ inválidas.
	- Padronizar respostas de sucesso e erro, usando conteúdo estruturado MCP quando isso melhorar a integração, sem perder mensagens úteis para clientes simples.
	- Tornar erros de JSON mais acionáveis (por exemplo, linha e coluna), se o Jackson disponível permitir expor esses dados de forma estável.
	- Evitar captura excessivamente ampla de exceções e distinguir entrada inválida de falha inesperada.
	- Manter os serviços independentes do protocolo MCP.

**Concluído quando:** todas as tools têm contratos explícitos, os casos de erro relevantes estão cobertos e os testes existentes continuam passando.

### 2. Adicionar resources de catálogo e orientação

**Commit sugerido:** `feat: adiciona resources de catálogo e guias`

**Objetivo:** tornar capacidades e limites do servidor consultáveis pelo cliente MCP.

**Resources iniciais sugeridos:**

- `devtools://catalog`: catálogo das ferramentas, finalidade, argumentos, exemplos e erros esperados.
- `devtools://guide/json`: exemplos de validação e formatação e explicação dos erros.
- `devtools://guide/encoding`: diferenças entre Base64, hash SHA-256 e UUID, incluindo usos adequados.
- `devtools://examples`: entradas e saídas pequenas, reproduzíveis e sem dados sensíveis.

**Escopo:**

- Registrar resources e anunciar a capability correspondente no servidor.
- Preferir conteúdo estático e versionado no projeto nesta primeira fase.
- Usar MIME types apropriados e URIs estáveis; manter o conteúdo em arquivos ou classes fáceis de revisar.
- Não incluir segredos, dados pessoais reais ou instruções que prometam segurança criptográfica onde ela não existe.

**Concluído quando:** um cliente/Inspector consegue listar e ler os resources, e testes confirmam URI, metadados e conteúdo esperado.

### 3. Adicionar prompts reutilizáveis

**Commit sugerido:** `feat: adiciona prompts para fluxos de desenvolvimento`

**Objetivo:** oferecer fluxos prontos que orientem o modelo a usar as tools adequadas.

**Prompts iniciais sugeridos:**

- `analisar_json`, com argumento `json`: instruir o modelo a validar e, se válido, formatar usando as tools disponíveis; se inválido, reportar o erro sem inventar correções.
- `codificar_ou_decodificar_base64`, com texto e direção: selecionar a tool correspondente e explicar o resultado.
- `gerar_identificadores`, com quantidade limitada: chamar `generate_uuid` repetidamente ou, se implementada depois, uma tool de geração em lote.
- `usar_devtools_primeiro`: orientar o modelo a verificar e usar as tools do servidor quando a solicitação corresponder às capacidades anunciadas; caso contrário, responder normalmente e declarar a limitação relevante.

**Escopo:**

- Registrar prompts e anunciar a capability de prompts.
- Definir argumentos com nomes, descrições e requisitos claros.
- Não afirmar que um prompt força a chamada de ferramenta: hosts podem exibir ou executar prompts de formas diferentes.
- Manter prompts focados, curtos e testáveis; evitar duplicar todo o conteúdo dos resources.

**Concluído quando:** prompts aparecem e podem ser obtidos em clientes compatíveis, com argumentos renderizados corretamente e fluxo coerente com as ferramentas existentes.

### 4. Melhorar o cliente de terminal para explorar MCP

**Commit sugerido:** `feat: permite explorar resources e prompts no cliente`

**Objetivo:** tornar resources e prompts demonstráveis sem depender de outro host.

**Escopo:**

- Listar e ler resources por URI.
- Listar prompts, solicitar seus argumentos e exibir as mensagens resultantes.
- Melhorar a navegação e tratar servidores que não anunciam uma capability.
- Manter compatibilidade com o fluxo atual de listar e chamar tools.

**Concluído quando:** o cliente de terminal consegue demonstrar tools, resources e prompts registrados no servidor e lida com entradas inválidas sem encerrar inesperadamente.

### 5. Expandir utilitários de alto valor

**Commit sugerido:** `feat: adiciona utilitários comuns para desenvolvimento`

**Objetivo:** aumentar utilidade diária sem transformar o servidor em uma coleção sem foco.

**Candidatas para avaliar e priorizar:**

- Codificação/decodificação de URL.
- Conversão de timestamps ISO-8601 e epoch, com timezone explícito.
- Teste de expressões regulares com exemplos e mensagens claras.
- Comparação ou normalização de JSON, se houver um caso de uso definido.

**Escopo:**

- Escolher poucas ferramentas com contratos pequenos e comportamento determinístico.
- Implementar cada regra em serviço testável, adaptar ao MCP e documentar exemplos/limites.
- Evitar ferramentas de segurança ambíguas: por exemplo, decodificar um JWT não equivale a validar assinatura ou confiança.

**Concluído quando:** cada ferramenta nova tem schema, descrição, testes de casos normais e de borda, documentação e ao menos um exemplo útil.

### 6. Definir limites de segurança e configuração

**Commit sugerido:** `feat: estabelece limites seguros para ferramentas locais`

**Objetivo:** preparar a evolução para ferramentas que interajam com o ambiente do usuário.

**Escopo:**

- Antes de implementar leitura/escrita de arquivos, definir diretórios permitidos, tamanho máximo, tratamento de caminhos relativos/simbólicos e política para arquivos sensíveis.
- Antes de executar comandos, usar uma lista explícita de operações suportadas; não criar uma ferramenta de shell arbitrário como padrão.
- Separar operações somente de leitura das que alteram arquivos ou executam processos.
- Definir timeouts, limites de saída e mensagens claras para bloqueios/erros.
- Tornar limites configuráveis sem permitir que uma configuração insegura seja ativada silenciosamente.

**Concluído quando:** há uma política documentada e testes demonstram que caminhos fora da raiz, entradas excessivas e comandos não permitidos são recusados.

> Esta etapa prepara a segurança; não implica que ferramentas de filesystem ou execução de comandos devam ser adicionadas automaticamente. A decisão deve partir de casos de uso concretos.

### 7. Avaliar ferramentas de contexto do projeto

**Commit sugerido:** `feat: adiciona inspeção segura do projeto`

**Objetivo:** aproximar a DevTools de pedidos reais de programação, após a etapa de segurança.

**Possíveis ferramentas, começando por operações somente de leitura:**

- Procurar arquivos/texto dentro de uma raiz explicitamente configurada.
- Ler um arquivo com limite de tamanho e caminho validado.
- Consultar estado e diff do Git em modo somente leitura.
- Executar testes/build usando operações predefinidas, com timeout e saída limitada, se esse caso de uso for aprovado no desenho técnico.

**Concluído quando:** a raiz de trabalho é explícita, limites são aplicados, operações têm resultados previsíveis e testes cobrem tentativas de sair da raiz e saídas grandes.

### 8. Documentar integração e orientação para o host

**Commit sugerido:** `docs: documenta configuração MCP e uso recomendado`

**Objetivo:** ajudar a IA conectada a descobrir e usar o servidor, sem sugerir que o servidor controla o host.

**Escopo:**

- Atualizar README com tabela de tools, resources, prompts, exemplos e configuração para clientes suportados.
- Documentar que a escolha de chamar tools é do host/modelo.
- Incluir instruções recomendadas para o host, por exemplo: “para tarefas cobertas pela DevTools, consulte as ferramentas disponíveis e use-as antes de responder”.
- Explicar como verificar a conexão com MCP Inspector e como diagnosticar logs/STDIO.
- Manter exemplos de configuração sem caminhos específicos da máquina do autor.

**Concluído quando:** uma pessoa consegue compilar, conectar, listar tools/resources/prompts e executar um exemplo seguindo apenas a documentação.

### 9. Revisão final e versão de demonstração

**Commit sugerido:** `docs: fecha roteiro de demonstração da DevTools MCP`

**Objetivo:** fechar uma versão coerente para demonstração/entrega acadêmica.

**Escopo:**

- Revisar consistência entre nomes, schemas, README, exemplos e comportamento real.
- Executar testes unitários e de integração e verificar o servidor no Inspector.
- Adicionar um roteiro curto de demonstração: ler catálogo, executar um prompt de análise JSON, observar chamadas das tools e ler um guia resource.
- Registrar limitações conhecidas e próximos passos.

**Concluído quando:** build/testes e roteiro estão reproduzíveis, sem contradições na documentação.

## Ordem recomendada

Implementar na ordem acima. As etapas 1–4 formam o núcleo de MCP com tools, resources e prompts; as etapas 5–7 ampliam a utilidade de forma controlada; as etapas 8–9 fecham integração e demonstração. Se o objetivo imediato for apresentar resources e prompts, as etapas 1–3 podem ser feitas antes da expansão de ferramentas.

## Critério de sucesso do projeto

Uma pessoa conecta o servidor a um cliente MCP, descobre pelo catálogo o que ele oferece, usa um prompt compatível para iniciar um fluxo, vê o modelo chamar as tools pertinentes, consulta resources de ajuda e recebe resultados previsíveis. Para solicitações fora do escopo, o host pode responder sem chamar a DevTools; isso é uma decisão do cliente/modelo, não uma garantia que o servidor MCP sozinho possa impor.
