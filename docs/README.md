# 🏗️ Desafio Fullstack Integrado

## 🎯 Objetivo
Criar solução completa em camadas (DB, EJB, Backend, Frontend), corrigindo bug em EJB e entregando aplicação funcional.

## 📦 Estrutura
- db/: scripts schema e seed
- ejb-module/: serviço EJB com bug a ser corrigido
- backend-module/: backend Spring Boot
- frontend/: app Angular
- docs/: instruções e critérios
- .github/workflows/: CI

## ✅ Tarefas do candidato
1. Executar db/schema.sql e db/seed.sql
2. Corrigir bug no BeneficioEjbService
3. Implementar backend CRUD + integração com EJB
4. Desenvolver frontend Angular consumindo backend
5. Implementar testes
6. Documentar (Swagger, README)
7. Submeter via fork + PR

## 🐞 Bug no EJB
- Transferência não verifica saldo, não usa locking, pode gerar inconsistência
- Espera-se correção com validações, rollback, locking/optimistic locking

## 📊 Critérios de avaliação
- Arquitetura em camadas (20%)
- Correção EJB (20%)
- CRUD + Transferência (15%)
- Qualidade de código (10%)
- Testes (15%)
- Documentação (10%)
- Frontend (10%)

Este projeto é a solução que consiste em uma arquitetura de N-camadas, integrando um backend Spring Boot, e um módulo de negócio EJB, com persistência em banco de dados.

O objetivo principal foi corrigir um bug crítico de concorrência e validação no serviço EJB e implementar os endpoints de API (CRUD + Transferência) e testes unitários.

---

## 🎯 Correção do Bug (Lógica de Negócio)

O `BeneficioEjbService` original continha um bug crítico em seu método `transfer`:

* **Sem Validação:** O método não validava se o beneficiário de origem possuía saldo suficiente, permitindo saldos negativos.
* **Sem Gerenciamento de Concorrência:** O método não utilizava nenhum tipo de *locking*. Isso poderia levar a *lost updates* (atualizações perdidas) se duas transações tentassem transferir do mesmo beneficiário ao mesmo tempo, resultando em um saldo final inconsistente.

### 💡 A Solução Implementada

Para corrigir este bug, foi implementada uma estratégia de **Bloqueio Otimista (Optimistic Locking)**:

1.  **`@Version`:** A entidade `Beneficio.java` (que mapeia a tabela `BENEFICIO`) foi anotada com `@Version`. Esta anotação utiliza a coluna `VERSION` do banco de dados para rastrear as alterações.
2.  **Validações:** Foram adicionadas validações explícitas no `BeneficioEjbService` para garantir que o saldo seja suficiente e que o valor da transferência seja positivo, lançando uma `EJBException` em caso de falha.
3.  **Captura de Exceção:** O método `transfer` agora captura a `OptimisticLockException`. Se duas transações concorrentes tentarem modificar o mesmo benefício, apenas a primeira terá sucesso. A segunda falhará, lançando esta exceção, que é então tratada e informada ao usuário de forma amigável, garantindo o *rollback* e a consistência dos dados.

---

## 💻 Tecnologias Utilizadas

* **Java 17**
* **Maven** (Gerenciador de Dependências)
* **Jakarta EE 10** (Especificação de EJB, JPA, etc.)
* **Spring Boot 3** (Para a camada de API REST)
* **JPA / Hibernate** (Para a camada de persistência)
* **Lombok** (Para redução de boilerplate)
* **JUnit 5 / Mockito** (Para testes unitários)
* **WildFly** (Servidor de Aplicação para EJB e `.war`)

---

## 🚀 Como Executar o Projeto

Para executar esta aplicação, é necessário um Servidor de Aplicação (como WildFly) e um banco de dados (ex: MySQL).

### 1. Pré-requisitos

* JDK 17 instalado e configurado.
* Maven instalado e configurado.
* Download e extração do [Servidor WildFly](https://www.wildfly.org/downloads/) (versão "Jakarta EE Full & Web Distribution").
* Um banco de dados de sua escolha (MySQL, etc.).

### 2. Configuração do Banco de Dados

1.  Crie um banco de dados (ex: `desafio_db`).
2.  Execute o script `db/schema.sql` para criar a tabela `BENEFICIO`.
3.  Execute o script `db/seed.sql` para popular o banco com dados iniciais.

### 3. Configuração do WildFly

1.  **Inicie o WildFly:** Navegue até a pasta `wildfly/bin` e execute `standalone.bat` (Windows).
2.  **Configurar o DataSource:**
    * Acesse o Console de Administração (normalmente `http://localhost:9990`).
    * Vá até `Configuration` > `Subsystems` > `Datasources & Drivers` > `Datasources`.
    * Crie um novo DataSource (ex: `BeneficioDS`) que aponte para o banco de dados que você configurou no passo 2 (fornecendo a URL JDBC, usuário e senha).
    * **Importante:** O JNDI Name do DataSource deve ser `java:jboss/datasources/BeneficioDS`.
3.  **Configurar a Persistência EJB:**
    * É necessário criar o arquivo `ejb-module/src/main/resources/META-INF/persistence.xml` para que o EJB saiba qual DataSource usar.
    ```xml
    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <persistence xmlns="[https://jakarta.ee/xml/ns/persistence](https://jakarta.ee/xml/ns/persistence)"
                 xmlns:xsi="[http://www.w3.org/2001/XMLSchema-instance](http://www.w3.org/2001/XMLSchema-instance)"
                 xsi:schemaLocation="[https://jakarta.ee/xml/ns/persistence](https://jakarta.ee/xml/ns/persistence) [https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd](https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd)"
                 version="3.0">
      <persistence-unit name="beneficio-pu">
        <jta-data-source>java:jboss/datasources/BeneficioDS</jta-data-source>
        <properties>
          <property name="hibernate.dialect" value="org.hibernate.dialect.PostgreSQLDialect"/> <property name="hibernate.hbm2ddl.auto" value="validate"/>
          <property name="hibernate.show_sql" value="true"/>
          <property name="hibernate.format_sql" value="true"/>
        </properties>
      </persistence-unit>
    </persistence>
    ```

### 4. Build do Projeto

Na pasta raiz do projeto (`bip-teste-integrado`), execute o comando Maven para construir ambos os módulos:

```bash
mvn clean package
Isso gerará dois arquivos na pasta target/ de seus respectivos módulos:

ejb-module/target/ejb-module-1.0-SNAPSHOT.jar

backend-module/target/backend.war (usamos <finalName>backend</finalName>)

5. Deploy da Aplicação
Pare o servidor WildFly.

Copie os dois arquivos (ejb-module-1.0-SNAPSHOT.jar e backend.war) para a pasta de deploy do WildFly: wildfly/standalone/deployments/

Inicie o WildFly novamente.

🧪 Testes
Testes Unitários (EJB)
Para executar os testes unitários do EJB (que validam a lógica de transferência e saldo), navegue até a pasta do EJB e execute o Maven:

Bash

cd ejb-module
mvn test
Teste da API (Swagger)
Uma vez que a aplicação esteja rodando no WildFly, você pode acessar a documentação da API gerada pelo Swagger:

URL do Swagger UI: http://localhost:8080/backend/swagger-ui.html

A partir desta página, é possível testar todos os endpoints (CRUD e Transferência) diretamente pelo navegador.

URL Base da API: http://localhost:8080/backend/api/v1/beneficios
