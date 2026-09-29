# API de Transferências Financeiras com Idempotência

API REST para transferências entre contas correntes bancárias, desenvolvida como desafio técnico simulando processos seletivos de bancos e fintechs. O foco não é CRUD simples — é garantir **consistência transacional sob concorrência**, **idempotência de requisições duplicadas** e **tratamento de erros padronizado**.

## Stack

- Java 21
- Spring Boot 4.1.1
- PostgreSQL 16
- Flyway (versionamento de schema)
- Testcontainers (testes de integração com banco real)
- springdoc-openapi / Swagger
- Docker + Docker Compose

## Como executar

### Pré-requisitos
- Docker e Docker Compose instalados

### Passos

1. Clone o repositório:
```bash
git clone https://github.com/KevinRychlewski/DesafioItauFinancias.git
cd DesafioItauFinancias/DesafioItauFinancia
```

2. Crie um arquivo `.env` na raiz do projeto (mesmo nível do `docker-compose.yml`) com o seguinte conteúdo:
```bash   
  DB_NAME=desafio_itau
  DB_USERNAME=postgres
  DB_PASSWORD=escolha_uma_senha
```
3. Suba a aplicação e o banco com um único comando:
```bash
docker-compose up --build
```

4. A API estará disponível em `http://localhost:8080`.

5. Documentação interativa (Swagger UI):http://localhost:8080/swagger-ui.html


## Como rodar a suíte de testes

Os testes usam Testcontainers, que precisa do Docker rodando na máquina.

```bash
./mvnw test
```

Isso executa:
- **Testes unitários** do `TransferService` (com Mockito, sem tocar banco) — cobrindo valor inválido, contas iguais, conta não encontrada, conta inativa e saldo insuficiente.
- **Teste de integração de concorrência** — dispara duas transferências simultâneas (threads reais) sobre a mesma conta, usando um Postgres real via Testcontainers, validando que apenas uma tem sucesso e o saldo final nunca fica negativo.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/accounts` | Cria uma conta com saldo inicial |
| GET | `/accounts/{id}` | Consulta uma conta pelo ID |
| POST | `/transfers` | Executa uma transferência entre contas (requer header `Idempotency-Key`) |

## Estratégia de concorrência: por que lock pessimista

O requisito mais crítico do desafio é impedir que duas transferências simultâneas, disputando o mesmo saldo, resultem em débito duplicado (ex: duas transferências de R$ 100 saindo de uma conta com R$ 100).

**Escolhi lock pessimista (`@Lock(LockModeType.PESSIMISTIC_WRITE)`)** em vez de otimista (`@Version`), pelos seguintes motivos:

- Embora conflitos de concorrência sejam estatisticamente raros numa conta pessoal comum — o que teoricamente favoreceria lock otimista pela performance em baixa contenção — priorizei **correção garantida sobre performance máxima**. Com lock pessimista, a segunda transação nunca enxerga saldo desatualizado: ela simplesmente aguarda a primeira terminar.
- Lock otimista exigiria tratamento explícito de exceção de conflito de versão (e possivelmente lógica de retry), aumentando a superfície de bugs sutis num sistema financeiro, onde o custo de um erro é dinheiro real.
- O custo de espera do lock pessimista é aceitável no volume esperado de uma operação bancária individual.

### Prevenção de deadlock

Como uma transferência sempre bloqueia duas contas (origem e destino), duas transferências opostas rodando ao mesmo tempo (A→B e B→A) poderiam travar uma esperando a outra indefinidamente. Para evitar isso, as contas são sempre bloqueadas na mesma ordem (por comparação de UUID), independentemente de qual é origem ou destino na requisição. Isso garante que, entre duas transações concorrentes disputando as mesmas contas, uma sempre "vence" a corrida e a outra apenas aguarda — nunca as duas ficam mutuamente bloqueadas.

## Idempotência

Toda requisição de transferência deve incluir um header `Idempotency-Key` (UUID gerado pelo cliente). Se a mesma chave for reenviada, a API retorna a resposta original sem duplicar o débito/crédito.

Mecanismo: a chave é persistida junto com a resposta serializada (JSON) na tabela `idempotency_records`, com constraint `UNIQUE`. Se duas requisições com a mesma chave chegarem simultaneamente, apenas uma consegue persistir; a outra recebe a violação de constraint do banco, tem sua própria transação revertida automaticamente (`@Transactional`), busca o registro já salvo pela concorrente, e devolve essa resposta como sucesso — já que a operação de fato ocorreu, só não através da própria execução.

## Tratamento de erros

Erros de negócio (`InvalidAmountException`, `AccountNotFoundException`, `InsufficientBalanceException`, `SameAccountTransferException`, `InactiveAccountException`, `DuplicateCpfException`) são capturados por um `@RestControllerAdvice` global e convertidos em respostas no formato **RFC 7807 (Problem Details)**, sem vazamento de stack trace:

```json
{
  "title": "Saldo insuficiente",
  "detail": "Saldo insuficiente na conta de origem",
  "status": 422,
  "instance": "/transfers"
}
```

| Exceção | Status |
|---|---|
| `InvalidAmountException` | 400 |
| `AccountNotFoundException` | 404 |
| `DuplicateCpfException` | 409 |
| `InsufficientBalanceException` / `SameAccountTransferException` / `InactiveAccountException` | 422 |

## Exemplos de uso (curl)

### Criar conta
```bash
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"cpf": "12345678900", "saldo": 500.00}'
```

### Consultar conta
```bash
curl http://localhost:8080/accounts/{id}
```

### Transferência (fluxo de sucesso)
```bash
curl -X POST http://localhost:8080/transfers \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 123e4567-e89b-12d3-a456-426614174000" \
  -d '{
    "sourceAccountId": "uuid-origem",
    "destinationAccountId": "uuid-destino",
    "amount": 150.00
  }'
```

### Transferência repetida (testando idempotência)
Repetir exatamente o mesmo comando acima, com a **mesma** `Idempotency-Key`. A resposta será idêntica à primeira (mesmo `timestamp`), e o saldo das contas não será alterado novamente.

## Limitações conhecidas e próximos passos

Decisões de escopo tomadas conscientemente, fora do que o desafio exigia:

- **Sem autenticação**: o desafio não pediu login/JWT, focando em concorrência e idempotência. Em produção, os endpoints precisariam de autenticação.
- **Sem persistência de histórico de transferências**: não existe entidade `Transfer` salva no banco. Em produção, seria necessária para auditoria e extrato.
- **Sem validação de formato de CPF**: o campo aceita qualquer string. Validação de dígito verificador não foi implementada por não ser requisito do desafio.

## Estrutura do projeto

O desenvolvimento seguiu uma branch por funcionalidade (entidade, repository, service, controller, lock, exceções, idempotência, testes, Swagger, Docker), cada uma commitada e mergeada individualmente na `main` — histórico completo disponível nos commits do repositório.