## Diagramas (C4)
Os diagramas C4 (Context, Container, Component, e Code) são utilizados para documentar a arquitetura de software de forma hierárquica e visual. Abaixo, apresentamos os três primeiros níveis para o projeto UniHub.

### Contexto
Este diagrama de alto nível mostra o UniHub como um sistema único, seus usuários e as interações com sistemas externos.

```mermaid
 C4Context
        title Diagrama de Contexto do Sistema UniHub

        Person(student, "Estudante", "Aluno de graduação ou pós-graduação que busca informações sobre disciplinas")
        Person(advertiser, "Anunciante", "Empresa que busca alcançar o público universitário")

        System(unihub, "UniHub", "Plataforma de avaliações de disciplinas")

        System_Ext(university_email, "Serviço de E-mail da Universidade", "Serviço externo para validação de e-mail de alunos")
        System_Ext(ad_platform, "Plataforma de Anúncios", "Plataforma de terceiros para veiculação de anúncios")
        System_Ext(da_partners, "Diretórios Acadêmicos", "Organizações estudantis para divulgação e engajamento")

        Rel(student, unihub, "Avalia disciplinas e professores, lê avaliações, se cadastra e autentica")
        Rel(advertiser, unihub, "Exibe anúncios na plataforma")
        Rel(unihub, university_email, "Valida o e-mail de registro do estudante para garantir autenticidade")
        Rel(unihub, ad_platform, "Integração para gerenciar e exibir anúncios")
        Rel(unihub, da_partners, "Parceria para divulgação e integração")
```

### Contêiners
Este diagrama amplia o UniHub, mostrando os principais "contêineres" tecnológicos que o compõem.

```mermaid
C4Container
        title Diagrama de Contêineres do Sistema UniHub

        Person(student, "Estudante", "Aluno de graduação ou pós-graduação")
        Person(advertiser, "Anunciante", "Empresa que busca alcançar o público universitário")

        System_Boundary(unihub, "UniHub") {
            Container(frontend, "Aplicação Web", "Angular SPA", "Single-Page Application que roda no navegador do usuário.")
            Container(backend, "API Backend", "Spring Boot", "API RESTful que gerencia a lógica de negócio, autenticação e acesso a dados.")
            ContainerDb(database, "Banco de Dados", "PostgreSQL", "Banco de dados relacional para persistir dados de usuários, avaliações, disciplinas e professores.")
        }

        Rel(student, frontend, "Acessa a interface web através do navegador")
        Rel(frontend, backend, "Consome a API RESTful para obter/enviar dados", "JSON/HTTPs")
        Rel(backend, database, "Lê e escreve dados", "JDBC/SQL")
        Rel(advertiser, frontend, "Exibe anúncios na interface da aplicação")
```

### Componentes
Este diagrama mostra a estrutura interna do contêiner "API Backend", detalhando os componentes lógicos.

```mermaid
 C4Component
        title Diagrama de Componentes do Backend do UniHub

        Container(backend, "API Backend", "Spring Boot", "API RESTful que gerencia a lógica de negócio, autenticação e acesso a dados.") {
            Component(controller, "Controller", "Spring MVC", "Expõe os endpoints da API REST.")
            Component(service, "Service", "Java", "Implementa a lógica de negócio da aplicação.")
            Component(repository, "Repository", "Spring Data JPA", "Abstrai o acesso ao banco de dados.")
            Component(domain, "Domínio/Modelo", "Java", "Representa as entidades de negócio e DTOs.")
            Component(security, "Security", "Spring Security, JWT", "Gerencia a autenticação e autorização stateless.")
        }
        ContainerDb(database, "Banco de Dados", "PostgreSQL", "Armazena informações de usuários, disciplinas e avaliações.")
        Component(frontend, "Aplicação Web", "Angular", "Consome a API.")

        Rel(frontend, controller, "Faz requisições HTTPs para a API")
        Rel(controller, service, "Invoca a lógica de negócio")
        Rel(service, repository, "Persiste e recupera dados")
        Rel(repository, database, "Interage com o banco de dados via JPA")
        Rel(controller, security, "Verifica a autenticação e autorização do usuário")
```
