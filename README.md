# Melicommerce - API de Comparação de Itens (Java / Spring Boot)

Um backend RESTful simples — feito em Java com Spring Boot — que expõe os dados de produtos para um recurso de comparação de itens. Projetado seguindo boas práticas de camadas, DTOs, tratamento de erros consistente e persistência em memória (H2) para fins de demonstração e testes.

---

## Sumário

- Visão geral
- Principais decisões arquiteturais
- Endpoints (descrição completa)
    - GET /products
    - GET /products/{id}
    - POST /products
    - PUT /products/{id}
    - DELETE /products/{id}
    - GET /products/compare?ids=1,2,3
- Modelos (ProductDTO, CustomError)
- Tratamento de erros (ControllerAdvice)
- Validações
- Paginação e ordenação
- Banco de dados (H2) e script de seed
- Configuração e execução (local / Docker)
- Testes (estratégia e exemplos)
- Possíveis melhorias e considerações de produção
- Documentação (Swagger / OpenAPI) — como acessar
- Anexos: exemplos de requests/responses e cURL

---

## Visão geral

Esta API fornece acesso a um catálogo de produtos e um endpoint específico para comparar itens a partir de uma lista de IDs. As informações disponibilizadas por produto contemplam:

- id: Long
- name: String
- description: String (texto)
- price: Double
- imgUrl: String (URL da imagem)
- rating: Double
- specifications: String (texto livre com especificações)

A persistência é feita em H2 (memória) usando Spring Data JPA. O projeto separa camadas de Controller, Service, Repository e DTO para facilitar manutenção, testes e evolução.

---

## Decisões arquiteturais (resumo)

- Framework: Spring Boot (REST, Spring Data JPA)
- Persistência: H2 (in-memory) para demos/integração rápida
- DTOs: ProductDTO para separar entidade de resposta e garantir validações
- Transactions: anotações @Transactional no Service para consistência
- Tratamento global de exceções: ControllerAdvice (ControllerExceptionHandler) que retorna um `CustomError` padronizado
- Validação: Jakarta Validation (anotações em ProductDTO)
- Endpoints paginados via `Pageable` (Spring Data) para /products
- Scripts de inicialização: `data.sql` (ou `import.sql`) para popular banco quando a aplicação inicia

---

## Modelos

ProductDTO (camada de entrada/saída)
```json
{
  "id": 1,
  "name": "PC Gamer",
  "description": "Lorem ipsum dolor sit amet...",
  "price": 1200.0,
  "imgUrl": "https://.../4-big.jpg",
  "rating": 4.6,
  "specifications": "Intel i5, 16GB RAM, RTX 3060, SSD 512GB"
}
```

CustomError (erro padronizado retornado pelo ControllerAdvice)
```json
{
  "timestamp": "2025-12-29T12:34:56.789Z",
  "status": 404,
  "error": "Recurso não encontrado",
  "path": "/products/999"
}
```

---

## Endpoints (detalhado)

Base: /products

1) GET /products
- Descrição: Retorna página de produtos (Page<ProductDTO>)
- Parâmetros de query (padrão Spring `Pageable`):
    - `page` (int, default 0)
    - `size` (int, default 20)
    - `sort` (ex.: `sort=price,desc` ou `sort=name,asc`)
- Resposta de sucesso:
    - Status: 200 OK
    - Body: JSON com estrutura `Page` (content, pageable, totalElements, totalPages, etc.)
- Exemplo:
    - GET /products?page=0&size=10&sort=price,asc

2) GET /products/{id}
- Descrição: Recupera um produto pelo seu ID.
- Parâmetros de path:
    - `id` (Long)
- Respostas possíveis:
    - 200 OK -> body: ProductDTO
    - 404 NOT FOUND -> body: CustomError (quando não existe)
- Exemplo:
    - GET /products/3

3) POST /products
- Descrição: Insere novo produto.
- Body esperado: ProductDTO (sem id)
- Validações aplicadas (ProductDTO):
    - `name`: obrigatório, 3-80 chars
    - `description`: obrigatório, mínimo 10 chars
    - `price`: positivo (> 0)
- Respostas:
    - 201 CREATED -> Location header com `/products/{id}` e body com ProductDTO criado
    - 400 BAD REQUEST -> quando payload inválido (validação jakarta) ou campos em formato incorreto
- Exemplo de payload:
```json
{
  "name": "Novo Produto",
  "description": "Descrição rica do produto.",
  "price": 199.99,
  "imgUrl": "https://.../img.jpg",
  "rating": 4.4,
  "specifications": "Especificações em texto"
}
```

4) PUT /products/{id}
- Descrição: Atualiza um produto existente.
- Parâmetros:
    - `id` (path)
- Body: ProductDTO (validações iguais ao POST)
- Respostas:
    - 200 OK -> ProductDTO atualizado
    - 404 NOT FOUND -> se id não existir
    - 400 BAD REQUEST -> quando payload inválido

5) DELETE /products/{id}
- Descrição: Exclui produto por ID.
- Respostas:
    - 204 NO CONTENT -> exclusão bem sucedida
    - 404 NOT FOUND -> se id não existir
    - 400 BAD REQUEST -> DatabaseException (integridade referencial) quando houver dependências (por exemplo OrderItem)
- Observação: o Service checa `existsById` antes de tentar deletar e mapeia exceções de integridade para `DatabaseException`.

6) GET /products/compare?ids={ids}
- Descrição: Endpoint específico para comparação de itens. Recebe um parâmetro `ids` (string) com IDs separados por vírgula e retorna uma lista de ProductDTO correspondentes.
- Validação e comportamento:
    - `ids` é obrigatório; se ausente ou vazio -> 400 Bad Request (BadRequestException)
    - IDs devem ser números inteiros longos; parse falho -> 400 Bad Request
    - Se nenhum produto encontrado para os IDs informados -> 404 Not Found
    - Retorna os produtos encontrados na ordem indiferente — a implementação atual usa findAllById (pode não preservar ordem dos ids)
- Exemplos:
    - GET /products/compare?ids=1,3,5
    - Success: 200 OK, body: [ ProductDTO(1), ProductDTO(3), ProductDTO(5) ]
    - Errors:
        - /products/compare?ids=   -> 400 Bad Request (JSON CustomError)
        - /products/compare?ids=abc,2 -> 400 Bad Request (IDs inválidos)
        - /products/compare?ids=9999 -> 404 Not Found (nenhum produto encontrado)

---

## Tratamento de erros (ControllerExceptionHandler)

A aplicação possui um `@ControllerAdvice` com handlers específicos para as exceções customizadas:

- ResourceNotFoundException -> HTTP 404
- DatabaseException -> HTTP 400
- BadRequestException -> HTTP 400

Todos retornam o mesmo formato `CustomError`:
```json
{
  "timestamp": "2025-12-29T12:34:56.789Z",
  "status": 400,
  "error": "Mensagem amigável",
  "path": "/products/compare"
}
```

Observações:
- Erros de validação (`@Valid`) não são explicitamente tratados no ControllerAdvice fornecido; o comportamento padrão do Spring é devolver 400 com um corpo de erro padrão. Em produção recomendaria capturar `MethodArgumentNotValidException` no ControllerAdvice e formatar uma resposta com campo/erros detalhados.

---

## Regras de validação e respostas de erro típicas

Validações em ProductDTO:
- name: @NotBlank, @Size(3,80)
- description: @NotBlank, @Size(min=10)
- price: @Positive

Resposta de erro de validação (recomendado)
```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Validation failed",
  "path": "/products",
  "errors": [
    { "field": "name", "message": "Campo requerido" },
    { "field": "price", "message": "O preço deve ser positivo" }
  ]
}
```
(Esse formato exige handler para `MethodArgumentNotValidException` — boa prática implementar.)

---

## Paginação e ordenação

Como `findAll(Pageable pageable)` é usado, você pode controlar:
- page (zero-based) — ex.: `?page=0`
- size — ex.: `?size=10`
- sort — ex.: `?sort=price,desc` ou `?sort=name,asc`

Exemplo:
- GET /products?page=1&size=5&sort=rating,desc

Resposta é um objeto `Page` com:
- content: array de ProductDTO
- pageable: informações sobre página atual
- totalElements, totalPages
- etc.

---

## Banco de dados (H2) e script de seed

- A aplicação usa H2 em memória (configurável via `application.properties`).
- O projeto contém um script SQL de inicialização (por exemplo `data.sql`) com inserts para `tb_product`, `tb_category`, `tb_product_category`, `tb_user`, `tb_order`, `tb_order_item`, `tb_payment`.
- Exemplo de trecho que popula produtos:
```sql
INSERT INTO tb_product (name, price, description, img_url, rating, specifications)
VALUES ('The Lord of the Rings', 90.5, 'Lorem ipsum...', 'https://.../1-big.jpg', 4.9, 'Autor: J.R.R. Tolkien; 1216 páginas; Editora: HarperCollins');
```

H2 Console:
- Habilite em `application.properties`:
```
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.datasource.url=jdbc:h2:mem:testdb
```
- Acesse: http://localhost:8080/h2-console (JDBC URL padrão `jdbc:h2:mem:testdb`)

---

## Documentação (Swagger / OpenAPI)

A documentação automática via OpenAPI/Swagger facilita explorar a API, visualizar schemas e testar endpoints diretamente pelo navegador.

- Dependência sugerida (springdoc-openapi):
```xml
<!-- pom.xml -->
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.8.8</version>
</dependency>
```

- Propriedades opcionais (application.properties / application.yml):
```
# Exemplo (opcional)
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
# Normalmente não é necessário alterar; o UI padrão fica disponível em /swagger-ui/index.html
```

- Como acessar o Swagger UI pelo navegador:
    1. Inicie a aplicação (ex.: `mvn spring-boot:run` ou `java -jar target/melicommerce-0.0.1-SNAPSHOT.jar`).
    2. Abra seu navegador e acesse:
       ```
       http://localhost:8080/swagger-ui/index.html
       ```
       (Se alterou a porta ou o contexto da aplicação, substitua conforme necessário — ex.: `http://<host>:<port>/<context>/swagger-ui/index.html`.)
    3. No Swagger UI você poderá:
        - Ver todos os endpoints expostos (GET/POST/PUT/DELETE).
        - Ver modelos (ProductDTO, CustomError, etc.).
        - Usar o botão "Try it out" para testar chamadas diretamente do navegador.
        - Ver o JSON do OpenAPI em: `http://localhost:8080/v3/api-docs`

- Observações práticas:
    - Se o Swagger UI não aparecer, verifique se a dependência `springdoc-openapi` está presente e a aplicação subiu sem erros na inicialização.
    - Em ambientes com segurança (autenticação) pode ser necessário liberar o acesso ao caminho do swagger no filtro de segurança (ex.: WebSecurityConfigurerAdapter ou SecurityFilterChain).
    - Se estiver usando Spring Boot Devtools, às vezes o cache do navegador pode apresentar versões antigas — recarregue (Ctrl+F5) ou abra em modo incógnito.

---

## Configuração e execução

Pré-requisitos:
- Java 17+ (ou versão compatível com dependências do projeto)
- Maven 3.6+ (ou Gradle se adaptado)
- (Opcional) Docker para empacotar/rodar em container

Build e run com Maven:
```bash
# build
mvn clean package

# run
java -jar target/melicommerce-0.0.1-SNAPSHOT.jar
```

Run durante development:
```bash
mvn spring-boot:run
```

Variáveis de ambiente típicas (exemplos):
- SERVER_PORT=8080
- SPRING_PROFILES_ACTIVE=dev

Dockerfile simples (exemplo):
```dockerfile
FROM eclipse-temurin:17-jre
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

---

## Exemplos de uso (cURL)

1. Listar produtos (paginado)
```bash
curl -i "http://localhost:8080/products?page=0&size=5&sort=price,asc"
```

2. Buscar por id
```bash
curl -i http://localhost:8080/products/3
```

Resposta 200:
```json
{
  "id": 3,
  "name": "Macbook Pro",
  "description": "...",
  "price": 1250.0,
  "imgUrl": "...",
  "rating": 4.8,
  "specifications": "Apple M1, 16GB RAM, 512GB SSD, 13 Retina"
}
```

3. Criar produto
```bash
curl -i -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Novo Produto", "description":"Descrição longa", "price":199.99, "imgUrl":"https://.../img.jpg"}'
```
Resposta: 201 Created + Location: /products/{id}

4. Atualizar produto
```bash
curl -i -X PUT http://localhost:8080/products/5 \
  -H "Content-Type: application/json" \
  -d '{"name":"Nome Alterado", "description":"Desc atualizada", "price":150.0}'
```

5. Deletar produto
```bash
curl -i -X DELETE http://localhost:8080/products/5
# 204 No Content quando bem sucedido
```

6. Comparar produtos
```bash
curl -i "http://localhost:8080/products/compare?ids=1,3,6"
```

Resposta 200:
```json
[
  { "id":1, "name":"The Lord of the Rings", ... },
  { "id":3, "name":"Macbook Pro", ... },
  { "id":6, "name":"PC Gamer Ex", ... }
]
```

Erros de compare:
- ids ausente: 400 (BadRequestException)
- ids inválidos: 400
- nenhum produto encontrado: 404

---

## Testes (estratégia)

Recomenda-se cobertura com:
- Unit tests (Junit + Mockito) para Service:
    - testar findById retorna dto
    - testar update lança ResourceNotFoundException quando id não existe
    - testar delete lança DatabaseException quando existe violação
    - testar compareProductsByIds com:
        - caso válido (lista de ids)
        - ids nulos/vazios -> BadRequestException
        - ids inválidos (parse) -> BadRequestException
        - nenhum produto encontrado -> ResourceNotFoundException
- Integration tests:
    - @SpringBootTest + TestRestTemplate ou MockMvc
    - usar H2 em memória e script `data.sql` para popular dados
    - testar endpoints REST (status, payload, headers)
- Testes de contrato (opcional): usar Spring REST Docs ou Pact para documentar/garantir contratos com consumidores

Exemplo de teste unitário (esqueleto):
```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
  @Mock ProductRepository repository;
  @InjectMocks ProductService service;

  @Test
  void compareProductsByIds_invalidIds_thenThrowBadRequest() {
    assertThrows(BadRequestException.class, () -> service.compareProductsByIds("a,b,c"));
  }
}
```

---

## Boas práticas e melhorias recomendadas

Para mover do estado demo para produção, considerar:
- Adicionar autenticação/autorização (JWT ou OAuth2)
- Limitar número máximo de itens comparáveis por requisição (ex.: max 6)
- Preservar a ordem dos ids na resposta (mapear manualmente após findAllById)
- Adicionar cache (Redis) para acelerar comparações frequentes
- Implementar DTOs separados para operações de criação/atualização (CreateProductDTO / UpdateProductDTO)
- Melhorar tratamento de validação (handler para MethodArgumentNotValidException) e retornar lista de erros por campo
- Documentação automática (OpenAPI / Springdoc / Swagger)
- Logs estruturados e métricas (micrometer / Prometheus)
- Testes E2E e CI pipeline
- Transformar `specifications` em JSON estruturado para permitir comparação por atributo em vez de texto livre

---

## Observações finais

- O endpoint /products/compare foi projetado para ser simples e direto: você passa uma lista de IDs e recebe os produtos correspondentes. O Service realiza validação básica de parâmetro, converte os IDs, busca via `findAllById` e transforma entidades em `ProductDTO`.
- O tratamento centralizado de exceções garante mensagens consistentes ao cliente. Recomenda-se expandir o ControllerAdvice para cobrir exceções de validação e exceções genéricas (500) com logging apropriado.
- O schema de dados (entidades) contempla relacionamentos (Category, OrderItem) que podem causar `DataIntegrityViolationException` em operações de exclusão — por isso a tradução para `DatabaseException` é usada para informar problemas de integridade referencial.

---

## Anexos: Endpoints em resumo (tabela rápida)

- GET /products?page=&size=&sort=
    - 200 Page<ProductDTO>
- GET /products/{id}
    - 200 ProductDTO | 404 CustomError
- POST /products
    - 201 ProductDTO + Location | 400 CustomError
- PUT /products/{id}
    - 200 ProductDTO | 404 | 400
- DELETE /products/{id}
    - 204 | 404 | 400 (integridade)
- GET /products/compare?ids=1,2,3
    - 200 List<ProductDTO> | 400 (ids inválido/ausente) | 404 (nenhum produto)

---