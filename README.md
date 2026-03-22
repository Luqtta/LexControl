# LexControl

Sistema web para controle de honorários advocatícios com foco em segurança, isolamento de dados e uso em produção. Permite registrar clientes, valores contratuais, pagamentos, despesas e acompanhar indicadores financeiros consolidados.

## Visão geral

O sistema oferece:

- Cadastro e gestão de clientes
- Registro de honorários contratuais
- Controle de pagamentos recebidos
- Registro de despesas
- Acompanhamento de cumprimento de sentença
- Dashboard financeiro consolidado
- Autenticação por usuário com isolamento total de dados
- API REST segura com JWT

## Stack

Backend:

- Java 21
- Quarkus 3
- JPA / Hibernate ORM
- MicroProfile JWT (SmallRye)
- PostgreSQL

Frontend:

- React 18
- Vite
- TypeScript
- Tailwind CSS
- React Router
- TanStack Query
- Axios

Infraestrutura:

- Railway (backend + banco)
- Vercel (frontend)

## Estrutura do repositório

    backend/   API Quarkus
    frontend/  Aplicação React

## Requisitos

- Java 21
- Node.js 20
- PostgreSQL 14+
- Docker (opcional)

## Variáveis de ambiente

### Backend

| Variável | Descrição |
|----------|-----------|
| DB_URL | JDBC URL do PostgreSQL |
| DB_USER | Usuário do banco |
| DB_PASSWORD | Senha do banco |
| JWT_PRIVATE_KEY | Chave privada RSA em formato PEM (multilinha, sem aspas) |
| JWT_PUBLIC_KEY | Chave pública RSA em formato PEM (multilinha, sem aspas) |
| CORS_ORIGINS | Origins permitidas separadas por vírgula |
| PORT | Porta HTTP do backend |

Observações importantes:

- As chaves JWT devem estar em formato PEM multilinha real
- Não utilizar aspas nem `\n`
- O backend utiliza assinatura RS256
- HS256 não é suportado na configuração atual

### Frontend

| Variável | Descrição |
|----------|-----------|
| VITE_API_URL | URL pública do backend |

## Execução local (Docker)

    docker-compose up --build

Serviços:

- Backend: http://localhost:8080
- Frontend: http://localhost:5173
- PostgreSQL: localhost:5432

## Execução local (sem Docker)

### Backend

    cd backend
    ./mvnw quarkus:dev

### Frontend

    cd frontend
    npm install
    npm run dev

## Banco de dados

O schema é gerenciado automaticamente pelo Hibernate.

## API REST

### Autenticação

- POST /auth/register
- POST /auth/login

Resposta do login:

    {
      "token": "...",
      "expiresAt": "...",
      "user": { ... }
    }

### Clients

- GET /clients
- POST /clients
- GET /clients/{id}
- PUT /clients/{id}
- DELETE /clients/{id}

### Transactions

- GET /transactions
- POST /transactions
- PUT /transactions/{id}
- DELETE /transactions/{id}

### Dashboard

- GET /dashboard/summary

### Health Check

- GET /q/health

## Segurança

- Senhas com hash BCrypt
- Autenticação via JWT RS256 (chaves RSA)
- Tokens com expiração configurável
- Isolamento de dados por usuário autenticado
- Validação de entrada com Bean Validation
- Proteção contra IDOR filtrando por usuário
- Headers de segurança HTTP
- Stack traces não expostos em produção

## Deploy no Railway (backend)

1. Importar o repositório no Railway
2. Definir Root Directory como `backend`
3. Adicionar PostgreSQL gerenciado
4. Configurar variáveis de ambiente:

    DB_URL=jdbc:postgresql://<host>:<port>/<database>
    DB_USER=<user>
    DB_PASSWORD=<password>
    CORS_ORIGINS=<url-do-frontend>
    JWT_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----
    ...
    -----END PRIVATE KEY-----
    JWT_PUBLIC_KEY=-----BEGIN PUBLIC KEY-----
    ...
    -----END PUBLIC KEY-----

Importante:

- Não usar aspas
- Não usar `\n`
- Colar as chaves exatamente como no arquivo PEM

5. Build Command:

    ./mvnw package -DskipTests

6. Start Command:

    java -jar target/quarkus-app/quarkus-run.jar

## Deploy na Vercel (frontend)

1. Importar o diretório `frontend`
2. Configurar variável:

    VITE_API_URL=https://<url-do-backend>

3. Build Command:

    npm run build

4. Output Directory:

    dist

## Observações

- Todas as rotas sensíveis exigem autenticação
- O backend não depende de estado de sessão
- Compatível com escalonamento horizontal
