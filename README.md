[README.md](https://github.com/user-attachments/files/34666167/README.md)
# Chamados API

API REST para gerenciamento de chamados de suporte técnico, desenvolvida com **Spring Boot**. Permite registrar solicitações, consultar atendimentos e identificar situações prioritárias.

Os dados são armazenados **em memória** (coleções Java), sem banco de dados, JPA, Service, Repository ou autenticação. Ao reiniciar a aplicação, os dados voltam ao estado inicial.

> 🎥 **Demonstração em vídeo:** os testes de todos os endpoints estão gravados em um vídeo localizado na **pasta raiz deste repositório**. Assista a ele para ver as requisições e respostas em funcionamento


## Pré-requisitos

- JDK 17 ou superior
- Maven 3.8+ (ou o Maven Wrapper, caso o projeto o inclua)


```

## Como executar

```bash
# clonar o repositório
git clone https://github.com/Igortpfalcao/TrabalhoLinguagensAplica-oComercial/
cd chamados-api

# subir a aplicação
mvn spring-boot:run
```

A API ficará disponível em **http://localhost:8080**.


## Modelo de dados

Cada chamado possui os seguintes campos:

| Campo        | Tipo     | Descrição                                      |
|--------------|----------|------------------------------------------------|
| `id`         | Long     | Identificador único, gerado automaticamente    |
| `titulo`     | String   | Descrição resumida da solicitação              |
| `solicitante`| String   | Pessoa responsável pela abertura               |
| `setor`      | String   | Departamento de origem                         |
| `prioridade` | Enum     | `BAIXA`, `MEDIA` ou `ALTA`                     |
| `status`     | Enum     | `ABERTO`, `EM_ANDAMENTO` ou `RESOLVIDO`        |

Exemplo de chamado em JSON:

```json
{
  "id": 2,
  "titulo": "Servidor de arquivos fora do ar",
  "solicitante": "Carlos Lima",
  "setor": "TI",
  "prioridade": "ALTA",
  "status": "EM_ANDAMENTO"
}
```

## Dados iniciais

A aplicação inicia com **12 chamados fictícios**, distribuídos entre os setores TI, Financeiro, RH, Comercial e Logística, e entre todas as prioridades e status possíveis.

## Endpoints

| Método | Rota                                | Descrição                                          |
|--------|-------------------------------------|----------------------------------------------------|
| GET    | `/chamados`                         | Lista todos os chamados                            |
| GET    | `/chamados/{id}`                    | Consulta um chamado pelo ID                        |
| GET    | `/chamados/setor?nome=financeiro`   | Filtra chamados por setor                          |
| GET    | `/chamados/prioridade?nivel=alta`   | Filtra chamados por prioridade                     |
| GET    | `/chamados/busca?texto=impressora`  | Busca chamados pelo título                         |
| GET    | `/chamados/criticos`                | Lista chamados de alta prioridade não resolvidos   |
| GET    | `/chamados/resumo`                  | Retorna a quantidade de chamados por status        |
| POST   | `/chamados`                         | Cadastra um novo chamado                           |

## Exemplos de requisições

### Listar todos os chamados

```bash
curl http://localhost:8080/chamados
```

### Consultar por ID

```bash
curl http://localhost:8080/chamados/1
```

### Filtrar por setor

```bash
curl "http://localhost:8080/chamados/setor?nome=financeiro"
```

### Filtrar por prioridade

```bash
curl "http://localhost:8080/chamados/prioridade?nivel=alta"
```

### Buscar pelo título

```bash
curl "http://localhost:8080/chamados/busca?texto=impressora"
```

### Listar chamados críticos

```bash
curl http://localhost:8080/chamados/criticos
```

### Resumo por status

```bash
curl http://localhost:8080/chamados/resumo
```

Resposta com os dados iniciais:

```json
{
  "abertos": 5,
  "emAndamento": 3,
  "resolvidos": 3,
  "total": 12
}
```

### Cadastrar um novo chamado

```bash
curl -i -X POST http://localhost:8080/chamados \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Monitor sem imagem",
    "solicitante": "Maria Silva",
    "setor": "Comercial",
    "prioridade": "ALTA"
  }'
```

Resposta (`HTTP 201 Created`):

```
Location: /chamados/13
```

```json
{
  "id": 13,
  "titulo": "Monitor sem imagem",
  "solicitante": "Maria Silva",
  "setor": "Comercial",
  "prioridade": "ALTA",
  "status": "ABERTO"
}
```

## Regras de negócio

- **Buscas sem diferenciar maiúsculas de minúsculas:** os filtros por setor, prioridade e título ignoram caixa e acentos. Por exemplo, `nivel=ALTA`, `nivel=alta` e `nivel=Alta` retornam o mesmo resultado, assim como `nome=FINANCEIRO` e `nome=financeiro`.
- **`/chamados/criticos`:** combina prioridade e status, retornando apenas chamados com prioridade `ALTA` e status diferente de `RESOLVIDO` (ou seja, `ABERTO` ou `EM_ANDAMENTO`).
- **`/chamados/resumo`:** retorna um JSON com as quantidades de chamados abertos, em andamento, resolvidos e o total geral.
- **`POST /chamados`:**
  - Recebe um JSON no corpo da requisição.
  - Gera um identificador único automaticamente.
  - Retorna **HTTP 201 Created** com o chamado criado e o cabeçalho `Location`.
  - Os campos `titulo`, `solicitante`, `setor` e `prioridade` são obrigatórios.
  - O campo `status` é opcional e, se omitido, assume `ABERTO`.

## Tratamento de erros

| Situação                                         | Código HTTP     |
|--------------------------------------------------|-----------------|
| ID inexistente em `GET /chamados/{id}`           | `404 Not Found` |
| Prioridade inválida em `/chamados/prioridade`    | `400 Bad Request` |
| Parâmetro obrigatório ausente (`nome`, `nivel`, `texto`) | `400 Bad Request` |
| Campo obrigatório vazio ou enum inválido no POST | `400 Bad Request` |

## Conceitos importantes

### 1. Como o Spring Boot recebe e processa requisições HTTP

Ao adicionar a dependência `spring-boot-starter-web`, o Spring Boot já traz um **servidor web embutido (Tomcat)**, por isso não é preciso instalar nenhum servidor separado: basta executar a aplicação e ela passa a escutar a porta `8080`.

O caminho de uma requisição, por exemplo `GET /chamados/criticos`, é o seguinte:

1. O **Tomcat** recebe a requisição e a entrega ao `DispatcherServlet`, o componente central do Spring MVC.
2. O `DispatcherServlet` procura, com `@GetMapping`, `@PostMapping` etc., aquele que corresponde ao **verbo HTTP e ao caminho** da requisição. Rotas literais como `/chamados/setor` têm preferência sobre rotas com variável como `/chamados/{id}`, o que evita conflitos.
3. Os parâmetros do método são preenchidos automaticamente:
   - `@PathVariable` → valor presente na URL (`/chamados/5`);
   - `@RequestParam` → parâmetros de consulta (`?nome=financeiro`);
   - `@RequestBody` → corpo JSON da requisição (usado no POST).
4. O método do controller é executado e devolve um objeto Java.
5. O retorno é convertido em JSON e enviado ao cliente com o status HTTP adequado (`200`, `201`, `400`, `404`...).

Se algo der errado (ID inexistente, JSON inválido, campo obrigatório ausente), o Spring responde com o status de erro correspondente.

### 2. Função do Controller

O **Controller** é a camada que faz a ponte entre o mundo HTTP e o código Java da aplicação. Na classe `ChamadoController`:

- `@RestController` indica que a classe responde requisições web e que os retornos dos métodos viram **corpo da resposta** (JSON), e não nomes de páginas HTML. Equivale a `@Controller` + `@ResponseBody`.
- `@RequestMapping("/chamados")` define o prefixo comum de todas as rotas da classe.
- `@GetMapping` e `@PostMapping` ligam cada método a um verbo HTTP e a um caminho específico.

Suas responsabilidades aqui são:

- **receber** a requisição e extrair os parâmetros, o ID ou o corpo JSON;
- **validar** os dados de entrada (`@Valid` no POST);
- **aplicar as regras** de filtro, busca e contagem sobre a lista de chamados;
- **montar a resposta**, escolhendo o status correto (por exemplo, `201 Created` com o cabeçalho `Location` no cadastro).

> Como o enunciado dispensa as camadas *Service* e *Repository*, o controller também guarda a lista em memória e executa a lógica. Em uma aplicação maior, o ideal é que o controller apenas receba a requisição e delegue a regra de negócio a um *Service*, e o acesso aos dados a um *Repository*.

### 3. Conversão entre objetos Java e JSON

Quem faz essa conversão é a biblioteca **Jackson**, incluída automaticamente pelo `spring-boot-starter-web`. O Spring a utiliza por meio de um `HttpMessageConverter` (`MappingJackson2HttpMessageConverter`) e o processo acontece nos dois sentidos:

**Desserialização (JSON → objeto Java), na entrada.** No `POST /chamados`, a anotação `@RequestBody` faz o Jackson ler o JSON enviado e montar um objeto `NovoChamadoRequest`:

```json
{ "titulo": "Monitor sem imagem", "solicitante": "Maria Silva", "setor": "Comercial", "prioridade": "ALTA" }
```
```java
NovoChamadoRequest req  // titulo, solicitante, setor, prioridade preenchidos
```

**Serialização (objeto Java → JSON), na saída.** O objeto retornado pelo método é transformado em JSON, e o cabeçalho `Content-Type: application/json` é definido automaticamente:

| Retorno do método        | JSON gerado                                  |
|--------------------------|----------------------------------------------|
| `Chamado` (record)       | objeto `{ "id": 1, "titulo": "...", ... }`   |
| `List<Chamado>`          | array `[ {...}, {...} ]`                     |
| `Map<String, Long>`      | objeto `{ "abertos": 5, "total": 12, ... }`  |

Detalhes relevantes deste projeto:

- Os nomes dos campos JSON correspondem aos nomes dos atributos do record (`Chamado`).
- **Enums** (`Prioridade`, `Status`) são convertidos de/para texto. A propriedade `spring.jackson.mapper.accept-case-insensitive-enums=true` permite enviar `"alta"`, `"ALTA"` ou `"Alta"` no JSON.
- Se o JSON for malformado ou tiver um valor de enum inexistente, a desserialização falha e o Spring responde com `400 Bad Request`, sem sequer chamar o método do controller.

### 4. Por que os chamados não permanecem após reiniciar a aplicação

Os chamados ficam guardados em uma `CopyOnWriteArrayList`, atributo do `ChamadoController`. Essa lista existe **somente na memória RAM da JVM**, enquanto a aplicação está em execução. Por isso:

- **Ao encerrar a aplicação, o processo Java termina** e toda a memória que ele usava é liberada, incluindo a lista e os chamados cadastrados via POST.
- **Ao iniciar novamente**, o Spring cria um novo controller, o construtor executa `carregarDadosIniciais()` e a lista volta a conter apenas os 12 chamados fictícios. O contador de IDs também recomeça do zero.

Consequências práticas: os dados cadastrados são válidos apenas durante a execução, e duas instâncias da aplicação rodando ao mesmo tempo teriam listas independentes, sem compartilhar informações.




