# melicommerce
# API de Comparação de Produtos

## Endpoints Principais

- `GET /products/compare?ids=1,2,3` — Retorna detalhes dos produtos para comparação.

## Setup

1. Clone o projeto.
2. Execute com Spring Boot (`mvn spring-boot:run`).
3. Acesse o H2 em `/h2-console` (usuário: `sa`, senha: em branco).

## Decisões Arquiteturais

- O endpoint de comparação aceita qualquer produto, sem restrição por categoria.
- Utiliza banco H2 em memória para facilitar testes e desenvolvimento.
- Segue boas práticas REST e tratamento de erros.