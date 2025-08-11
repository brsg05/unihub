# Unihub

Unihub is a web application designed for university students and staff to manage and share information about courses, professors, and evaluations.

## Project Structure

The project is divided into two main parts:

*   **`backend`**: A Java Spring Boot application providing the RESTful API.
*   **`frontend`**: An Angular application for the user interface.

## Prerequisites

### Backend (Java Spring Boot)

*   Java Development Kit (JDK) - Version 17 or later
*   Apache Maven - Version 3.6 or later
*   A running PostgreSQL database instance.

### Frontend (Angular)

*   Node.js - Version 18.x or later (includes npm)
*   Angular CLI - Version 17.x or later

## Setup and Running

### Backend

1.  **Database Configuration**:
    *   Navigate to `backend/src/main/resources/`.
    *   Rename `application-example.properties` to `application.properties`.
    *   Update `application.properties` with your PostgreSQL database connection details (URL, username, password). Ensure the database schema/database specified in the URL exists.

2.  **Build and Run**:
    *   Open a terminal in the `backend` directory.
    *   Run `mvn spring-boot:run` to start the backend server.
    *   The API will typically be available at `http://localhost:8080`.
    *   API documentation (Swagger UI) can be accessed at `http://localhost:8080/swagger-ui/index.html`.

### Frontend

1.  **Install Dependencies**:
    *   Open a terminal in the `frontend` directory.
    *   Run `npm install` to download project dependencies.

2.  **Run Development Server**:
    *   Run `ng serve` or `npm start`.
    *   The application will typically be available at `http://localhost:4200`.

## Key Features

*   User authentication and role-based authorization (User, Admin).
*   **Admin Dashboard**:
    *   Manage Users (view users, update roles).
    *   Manage Courses (`Cadeiras`): CRUD operations.
    *   Manage Criteria (`Critérios`): CRUD operations for evaluation criteria.
    *   Manage Professors (`Professores`): CRUD operations.
*   **Public Pages**:
    *   View lists of professors, courses.
    *   View detailed professor pages, including courses they teach and overall ratings per criterion.
    *   View detailed course pages.
    *   View evaluation history for a specific criterion of a professor.
*   **User Features**:
    *   Submit evaluations for professors on specific courses and criteria.
    *   View evaluations and comments.

## API Endpoints

The backend exposes RESTful API endpoints under `/api`. Key controllers include:

*   `/api/users`: User management (Admin only) and Authentication (login, register).
*   `/api/professores`: Professor management and public listing.
*   `/api/cadeiras`: Course management and public listing.
*   `/api/criterios`: Criteria management and public listing.
*   `/api/avaliacoes`: Submission and retrieval of evaluations.
*   `/api/comentarios`: Comment management (currently includes voting).

Refer to the Swagger UI (`/swagger-ui/index.html` on the running backend) for detailed API documentation.

## Technologies Used

### Backend

*   Java 17
*   Spring Boot 3.x
    *   Spring Web
    *   Spring Data JPA
    *   Spring Security
*   PostgreSQL
*   Maven
*   Lombok
*   Swagger/OpenAPI (Springdoc)
*   Mockito
*   JUnit

### Frontend

*   Angular 17.x
*   TypeScript
*   Angular Material
*   RxJS
*   HTML, SCSS

## Diagrama de Entidades e Relacionamentos (ERD)

```mermaid
erDiagram
    USUARIO {
        BIGINT id PK
        VARCHAR username UK
        VARCHAR password
        VARCHAR email UK
        VARCHAR role "Enum: USER, ADMIN"
    }

    PROFESSOR {
        BIGINT id PK
        VARCHAR nomeCompleto
        VARCHAR photoUrl
        DECIMAL notaGeral "Calculada"
    }

    CADEIRA {
        BIGINT id PK
        VARCHAR nome
        INT cargaHoraria
        BOOLEAN isEletiva
    }

    PROFESSOR_CADEIRAS {
        BIGINT professor_id FK
        BIGINT cadeira_id FK
    }

    CRITERIO {
        BIGINT id PK
        VARCHAR nome UK
    }

    AVALIACAO {
        BIGINT id PK
        TIMESTAMP data
        VARCHAR periodo "Ex: 2023.1"
        BIGINT usuario_id FK
        BIGINT professor_id FK
        BIGINT cadeira_id FK
    }

    NOTA_CRITERIO {
        BIGINT id PK
        BIGINT avaliacao_id FK
        BIGINT criterio_id FK
        INT nota "1-5 or 1-10"
    }

    COMENTARIO {
        BIGINT id PK
        TEXT texto
        BIGINT avaliacao_id FK "Avaliação à qual o comentário está ligado"
        BIGINT criterio_id FK "Critério específico do comentário"
        INT votosPositivos
        INT votosNegativos
    }

    USUARIO ||--o{ AVALIACAO : realiza
    PROFESSOR ||--o{ AVALIACAO : é_avaliado_em
    CADEIRA ||--o{ AVALIACAO : referente_a
    PROFESSOR }o--o{ PROFESSOR_CADEIRAS : leciona
    CADEIRA }o--o{ PROFESSOR_CADEIRAS : é_lecionada_por
    AVALIACAO ||--|{ NOTA_CRITERIO : possui_notas_por
    CRITERIO ||--o{ NOTA_CRITERIO : é_avaliado_via
    AVALIACAO ||--o{ COMENTARIO : pode_ter_comentario_sobre
    CRITERIO ||--o{ COMENTARIO : pode_ter_comentario_especifico_sobre
    ADMINISTRADOR (USUARIO) }|--|{ CRITERIO : gerencia
    ADMINISTRADOR (USUARIO) }|--|{ PROFESSOR : gerencia

```

## Descrições das Entidades

*   **Usuário:**
    *   `id`: Identificador único.
    *   `username`: Nome de usuário para login (único).
    *   `password`: Senha criptografada.
    *   `email`: Email do usuário (único).
    *   `role`: Papel do usuário no sistema (`USER` ou `ADMIN`).
*   **Professor:**
    *   `id`: Identificador único.
    *   `nomeCompleto`: Nome completo do professor.
    *   `photoUrl`: URL para a foto do professor.
    *   `notaGeral`: Média aritmética das notas de todos os critérios avaliados para este professor (calculado).
*   **Cadeira:** (Disciplina)
    *   `id`: Identificador único.
    *   `nome`: Nome da cadeira.
    *   `cargaHoraria`: Carga horária da cadeira.
    *   `isEletiva`: Indica se a cadeira é eletiva.
*   **Professor_Cadeiras:** Tabela de junção para o relacionamento N-M entre Professor e Cadeira.
    *   `professor_id`: Chave estrangeira para Professor.
    *   `cadeira_id`: Chave estrangeira para Cadeira.
*   **Critério:**
    *   `id`: Identificador único.
    *   `nome`: Nome do critério de avaliação (ex: Didática, Assiduidade). Criado apenas por ADMIN.
*   **Avaliação:**
    *   `id`: Identificador único.
    *   `data`: Data e hora da avaliação.
    *   `periodo`: Período acadêmico da avaliação (ex: "2023.1").
    *   `usuario_id`: Chave estrangeira para Usuário que realizou a avaliação (mantido anônimo no frontend).
    *   `professor_id`: Chave estrangeira para Professor avaliado.
    *   `cadeira_id`: Chave estrangeira para Cadeira relacionada à avaliação.
*   **NotaCritério:**
    *   `id`: Identificador único.
    *   `avaliacao_id`: Chave estrangeira para Avaliação.
    *   `criterio_id`: Chave estrangeira para Critério.
    *   `nota`: Nota atribuída ao critério específico nesta avaliação.
*   **Comentário:**
    *   `id`: Identificador único.
    *   `texto`: Conteúdo do comentário.
    *   `avaliacao_id`: Chave estrangeira para Avaliação.
    *   `criterio_id`: Chave estrangeira para o Critério ao qual o comentário se refere.
    *   `votosPositivos`: Número de votos positivos no comentário.
    *   `votosNegativos`: Número de votos negativos no comentário.

## Fluxos de Usuário Principais

1.  **Registro de Novo Usuário:**
    *   Usuário acessa a página de registro.
    *   Preenche formulário (username, email, senha).
    *   Sistema valida os dados e cria a conta com `role=USER`.
    *   Usuário é redirecionado para login ou página principal.
2.  **Login de Usuário:**
    *   Usuário acessa a página de login.
    *   Fornece username/email e senha.
    *   Sistema valida as credenciais.
    *   Se válido, um token JWT é gerado e retornado. Usuário é redirecionado.
3.  **Avaliar Professor (Usuário Logado):**
    *   Usuário navega para a página de um professor ou lista de professores.
    *   Seleciona um professor e uma cadeira que ele leciona.
    *   Acessa o formulário de avaliação para o período corrente.
    *   Preenche as notas para todos os critérios obrigatórios.
    *   Opcionalmente, adiciona comentários para cada critério.
    *   Submete a avaliação. O sistema registra a avaliação de forma anônima (associação com usuário apenas no backend).
4.  **Visualizar Avaliações de Professor (Público):**
    *   Qualquer visitante acessa a página de um professor.
    *   Visualiza a `notaGeral` do professor.
    *   Vê uma tabela com cada `Critério`, sua nota média e o principal comentário (ordenado por score `votosPositivos - votosNegativos`).
    *   Pode navegar para a página de um critério específico para ver mais detalhes e comentários.
5.  **Administração de Professores (Admin Logado):**
    *   Admin acessa o Dashboard de Administração.
    *   Pode Criar, Ler, Atualizar e Deletar (CRUD) Professores.
    *   Pode associar Cadeiras a Professores.
6.  **Administração de Critérios (Admin Logado):**
    *   Admin acessa o Dashboard de Administração.
    *   Pode Criar, Ler, Atualizar e Deletar (CRUD) Critérios de avaliação.
7.  **Atribuir Papel de Admin (Admin Logado):**
    *   Admin acessa a seção de gerenciamento de usuários no Dashboard.
    *   Pode alterar o `role` de um usuário para `ADMIN`.

## Estrutura do Repositório (Planejada)

```
/
├── backend/ (Java Spring Boot)
│   ├── src/
│   │   └── main/
│   │       ├── java/com/unihub/app/
│   │       │   ├── config/
│   │       │   ├── controller/
│   │       │   ├── dto/
│   │       │   ├── entity/
│   │       │   ├── exception/
│   │       │   ├── repository/
│   │       │   ├── service/
│   │       │   └── UniHubApplication.java
│   │       └── resources/
│   │           ├── application.properties
│   │           └── static/
│   │           └── templates/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/ (Angular)
│   ├── src/
│   │   ├── app/
│   │   │   ├── admin/
│   │   │   ├── auth/
│   │   │   ├── professor/
│   │   │   ├── shared/
│   │   │   ├── app-routing.module.ts
│   │   │   ├── app.component.html
│   │   │   ├── app.component.ts
│   │   │   └── app.module.ts
│   │   ├── assets/
│   │   ├── environments/
│   │   ├── index.html
│   │   ├── main.ts
│   │   └── styles.css
│   ├── angular.json
│   ├── package.json
│   ├── tsconfig.json
│   └── Dockerfile
├── db/
│   ├── migrations/
│   │   └── V1__create_tables.sql
│   └── seeds/
│       └── V1__initial_data.sql
├── docs/
│   └── api-spec.md (Ou integrado ao Swagger/OpenAPI no backend)
├── docker-compose.yml
└── README.md
```

## Tecnologias

*   **Backend:** Java Spring Boot (Spring Data JPA, Spring Security com JWT)
*   **Frontend:** Angular (TypeScript, RxJS, Angular Material)
*   **Banco de Dados:** PostgreSQL
*   **Deploy:** Docker & Docker Compose

## Instruções de Deploy (Preliminar)

1.  Clone o repositório.
2.  Certifique-se de ter Docker e Docker Compose instalados.
3.  Configure as variáveis de ambiente necessárias (ex: `POSTGRES_USER`, `POSTGRES_PASSWORD`, `JWT_SECRET`) em um arquivo `.env` na raiz do projeto ou diretamente no `docker-compose.yml` para desenvolvimento.
4.  Execute `docker-compose up --build -d` na raiz do projeto.
5.  Acesse o frontend em `http://localhost:4200` (porta padrão do Angular).
6.  Acesse o backend em `http://localhost:8080` (porta padrão do Spring Boot).

---
*Esta documentação será expandida e refinada conforme o desenvolvimento avança.* 
