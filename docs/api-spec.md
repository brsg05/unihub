# Especificação da API REST - UniHub

## Autenticação

A autenticação é baseada em JSON Web Tokens (JWT).

*   **`POST /api/auth/register`**: Registra um novo usuário.
    *   Request Body: `RegisterRequest`
    *   Response Body: `MessageResponse` (sucesso) ou `ErrorResponse` (erro)
*   **`POST /api/auth/login`**: Autentica um usuário existente.
    *   Request Body: `LoginRequest`
    *   Response Body: `JwtResponse` (contendo o token) ou `ErrorResponse` (erro)

### Contratos JSON (Auth)

**RegisterRequest:**
```json
{
  "username": "novo_usuario",
  "email": "usuario@example.com",
  "password": "senha123"
}
```

**LoginRequest:**
```json
{
  "username": "usuario_existente",
  "password": "senha123"
}
```

**JwtResponse:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c3VhcmlvX2V4aXN0ZW50ZSIsImlhdCI6MTYxNjQ1NjYyMSwiZXhwIjoxNjE2NTQzMDIxfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
  "type": "Bearer",
  "id": 1,
  "username": "usuario_existente",
  "email": "usuario@example.com",
  "roles": ["ROLE_USER"]
}
```

**MessageResponse:**
```json
{
  "message": "Usuário registrado com sucesso!"
}
```

## Usuários (Endpoints protegidos - ADMIN)

*   **`GET /api/users`**: Lista todos os usuários (ADMIN).
    *   Response Body: `List<UserDto>`
*   **`PUT /api/users/{userId}/role`**: Atualiza o papel de um usuário (ADMIN).
    *   Request Body: `UpdateUserRoleRequest` (`{ "role": "ADMIN" }`)
    *   Response Body: `UserDto`

### Contratos JSON (Users)

**UserDto:**
```json
{
  "id": 1,
  "username": "usuario1",
  "email": "usuario1@example.com",
  "role": "USER"
}
```

**UpdateUserRoleRequest:**
```json
{
  "role": "ADMIN" // ou "USER"
}
```

## Professores

*   **`GET /api/professores`**: Lista todos os professores (Público).
    *   Query Params: `page`, `size`, `sortBy=[nomeCompleto|notaGeral]`, `direction=[ASC|DESC]`, `periodo`
    *   Response Body: `Page<ProfessorPublicDto>`
*   **`GET /api/professores/top`**: Lista os top X professores (Público).
    *   Query Params: `limit` (default 5)
    *   Response Body: `List<ProfessorPublicDto>`
*   **`GET /api/professores/search`**: Busca professores por nome (Público).
    *   Query Params: `nome`
    *   Response Body: `List<ProfessorPublicDto>`
*   **`GET /api/professores/{id}`**: Detalhes de um professor (Público).
    *   Response Body: `ProfessorDetailDto`
*   **`POST /api/professores`**: Cria um novo professor (ADMIN).
    *   Request Body: `ProfessorRequest`
    *   Response Body: `ProfessorDto`
*   **`PUT /api/professores/{id}`**: Atualiza um professor (ADMIN).
    *   Request Body: `ProfessorRequest`
    *   Response Body: `ProfessorDto`
*   **`DELETE /api/professores/{id}`**: Deleta um professor (ADMIN).
    *   Response: `204 No Content`
*   **`POST /api/professores/{professorId}/cadeiras`**: Associa uma cadeira a um professor (ADMIN).
    *   Request Body: `{ "cadeiraId": 1 }`
    *   Response: `ProfessorDto`
*   **`DELETE /api/professores/{professorId}/cadeiras/{cadeiraId}`**: Desassocia uma cadeira de um professor (ADMIN).
    *   Response: `ProfessorDto`

### Contratos JSON (Professores)

**ProfessorRequest:** (para POST e PUT)
```json
{
  "nomeCompleto": "Dr. Nome Sobrenome",
  "photoUrl": "http://example.com/foto.jpg",
  "cadeiraIds": [1, 2]
}
```

**ProfessorDto:** (resposta padrão para admin)
```json
{
  "id": 1,
  "nomeCompleto": "Dr. Nome Sobrenome",
  "photoUrl": "http://example.com/foto.jpg",
  "notaGeral": null, // Calculado e atualizado por trigger ou batch
  "cadeiras": [
    { "id": 1, "nome": "Cálculo I", "cargaHoraria": 90, "isEletiva": false },
    { "id": 2, "nome": "Física I", "cargaHoraria": 60, "isEletiva": false }
  ]
}
```

**ProfessorPublicDto:** (para listagem pública)
```json
{
  "id": 1,
  "nomeCompleto": "Dr. Nome Sobrenome",
  "photoUrl": "http://example.com/foto.jpg",
  "notaGeral": 4.5
}
```

**ProfessorDetailDto:** (para página do professor)
```json
{
  "id": 1,
  "nomeCompleto": "Dr. Nome Sobrenome",
  "photoUrl": "http://example.com/foto.jpg",
  "notaGeral": 4.5,
  "cadeirasLecionadas": [
      { "id": 1, "nome": "Cálculo I"}
  ],
  "avaliacoesPorCriterio": [
    {
      "criterioId": 1,
      "criterioNome": "Didática",
      "notaMedia": 4.8,
      "principalComentario": {
        "id": 101,
        "texto": "Excelente didática, aulas muito claras!",
        "score": 15
      }
    },
    {
      "criterioId": 2,
      "criterioNome": "Assiduidade",
      "notaMedia": 4.2,
      "principalComentario": null
    }
  ]
}
```

## Cadeiras

*   **`GET /api/cadeiras`**: Lista todas as cadeiras (Público).
    *   Query Params: `page`, `size`
    *   Response Body: `Page<CadeiraDto>`
*   **`GET /api/cadeiras/{id}`**: Detalhes de uma cadeira (Público).
    *   Response Body: `CadeiraDto`
*   **`POST /api/cadeiras`**: Cria uma nova cadeira (ADMIN).
    *   Request Body: `CadeiraRequest`
    *   Response Body: `CadeiraDto`
*   **`PUT /api/cadeiras/{id}`**: Atualiza uma cadeira (ADMIN).
    *   Request Body: `CadeiraRequest`
    *   Response Body: `CadeiraDto`
*   **`DELETE /api/cadeiras/{id}`**: Deleta uma cadeira (ADMIN).
    *   Response: `204 No Content`

### Contratos JSON (Cadeiras)

**CadeiraRequest:**
```json
{
  "nome": "Inteligência Artificial",
  "cargaHoraria": 60,
  "isEletiva": true
}
```

**CadeiraDto:**
```json
{
  "id": 3,
  "nome": "Inteligência Artificial",
  "cargaHoraria": 60,
  "isEletiva": true
}
```

## Critérios

*   **`GET /api/criterios`**: Lista todos os critérios (Público).
    *   Response Body: `List<CriterioDto>`
*   **`GET /api/criterios/{id}`**: Detalhes de um critério (Público).
    *   Response Body: `CriterioDto`
*   **`POST /api/criterios`**: Cria um novo critério (ADMIN).
    *   Request Body: `CriterioRequest`
    *   Response Body: `CriterioDto`
*   **`PUT /api/criterios/{id}`**: Atualiza um critério (ADMIN).
    *   Request Body: `CriterioRequest`
    *   Response Body: `CriterioDto`
*   **`DELETE /api/criterios/{id}`**: Deleta um critério (ADMIN).
    *   Response: `204 No Content`
*   **`GET /api/criterios/{criterioId}/professores/{professorId}`**: Página de um critério para um professor específico (Público).
    *   Response Body: `CriterioProfessorDetailDto`

### Contratos JSON (Critérios)

**CriterioRequest:**
```json
{
  "nome": "Clareza nas Explicações"
}
```

**CriterioDto:**
```json
{
  "id": 1,
  "nome": "Didática"
}
```

**CriterioProfessorDetailDto:**
```json
{
  "criterioId": 1,
  "criterioNome": "Didática",
  "professorId": 1,
  "professorNome": "Dr. Nome Sobrenome",
  "historicoAvaliacoes": [
    {
      "avaliacaoId": 201,
      "periodo": "2023.1",
      "nota": 5,
      "comentario": {
          "id": 101,
          "texto": "Excelente!",
          "score": 10
      }
    },
    {
      "avaliacaoId": 205,
      "periodo": "2022.2",
      "nota": 4,
      "comentario": null
    }
  ],
  "principaisComentarios": [
      { "id": 101, "texto": "Excelente!", "score": 10 },
      { "id": 105, "texto": "Muito bom.", "score": 8 }
  ]
}
```

## Avaliações

*   **`POST /api/avaliacoes`**: Submete uma nova avaliação (USER logado).
    *   Request Body: `AvaliacaoRequest`
    *   Response Body: `AvaliacaoDto` (ou `MessageResponse` de sucesso)
*   **`GET /api/professores/{professorId}/cadeiras/{cadeiraId}/avaliacoes`**: Lista avaliações para um professor em uma cadeira (Público, mas pode ser filtrado/limitado).
    *   Query Params: `periodo`
    *   Response Body: `List<AvaliacaoPublicDto>` (anonimizado)

### Contratos JSON (Avaliações)

**AvaliacaoRequest:**
```json
{
  "professorId": 1,
  "cadeiraId": 1,
  "periodo": "2023.2",
  "notasCriterios": [
    { "criterioId": 1, "nota": 5 },
    { "criterioId": 2, "nota": 4 }
  ],
  "comentarios": [
    { "criterioId": 1, "texto": "Ótima didática!" },
    { "criterioId": 2, "texto": "Sempre presente." }
  ]
}
```

**AvaliacaoDto:** (visão do backend, não expor `usuarioId` diretamente no frontend sem necessidade)
```json
{
  "id": 101,
  "data": "2023-10-27T10:00:00Z",
  "periodo": "2023.2",
  "usuarioId": 5, // Não expor no frontend público
  "professorId": 1,
  "cadeiraId": 1,
  "notasCriterios": [
    { "criterio": { "id": 1, "nome": "Didática" }, "nota": 5 },
    { "criterio": { "id": 2, "nome": "Assiduidade" }, "nota": 4 }
  ],
  "comentarios": [
    { "criterio": { "id": 1, "nome": "Didática" }, "texto": "Ótima didática!" }
  ]
}
```

**AvaliacaoPublicDto:** (para listagem pública na página do professor/critério)
```json
{
  "id": 101,
  "data": "2023-10-27T10:00:00Z",
  "periodo": "2023.2",
  "notasCriterios": [
    { "criterioNome": "Didática", "nota": 5 },
    { "criterioNome": "Assiduidade", "nota": 4 }
  ],
  "comentarios": [
    { "criterioNome": "Didática", "texto": "Ótima didática!", "score": 10 }
  ]
}
```

## Comentários

*   **`POST /api/comentarios/{comentarioId}/vote`**: Vota em um comentário (USER logado).
    *   Request Body: `ComentarioVoteRequest` (`{ "type": "UPVOTE" }` ou `{ "type": "DOWNVOTE" }`)
    *   Response Body: `ComentarioDto` (atualizado com novo score)

### Contratos JSON (Comentários)

**ComentarioVoteRequest:**
```json
{
  "type": "UPVOTE" // ou "DOWNVOTE"
}
```

**ComentarioDto:**
```json
{
  "id": 201,
  "texto": "Muito bom professor!",
  "avaliacaoId": 101,
  "criterioId": 1,
  "votosPositivos": 15,
  "votosNegativos": 2,
  "score": 13 // (votosPositivos - votosNegativos)
}
```

## Error Handling

Respostas de erro seguirão o formato:

```json
{
  "timestamp": "2023-10-27T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Mensagem de erro detalhada.",
  "path": "/api/endpoint_com_erro"
}
``` 