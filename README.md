# Smart Store

## Sobre o Projeto

**Smart Store** é uma aplicação desenvolvida para a disciplina **DIM0162 - Engenharia de Software** que visa modernizar a gestão de supermercados de pequeno e médio porte. O projeto integra tecnologias modernasda web com potencial para implementação de inteligência artificial, como chat conversacional, para melhorar a experiência do cliente e otimizar operações de loja.

### Visão

Criar uma plataforma escalável e fácil de usar que permita gerenciar:
- Catálogo de produtos
- Chat conversacional com IA (em desenvolvimento)
- Integração com sistema de vendas (futuro)
- Motor de promoções e cashback inteligente (futuro)

---

## 🛠️ Tecnologias Utilizadas

### Backend
- **Framework**: Spring Boot 3.2.2
- **Linguagem**: Java 21
- **Banco de Dados**: H2 Database
- **ORM**: Spring Data JPA
- **Ferramenta Build**: Maven

### Frontend
- **Framework**: Angular 21.2.0
- **Linguagem**: TypeScript 5.9.2
- **Estilo**: Tailwind CSS 4.1.12
- **Gerenciador de Pacotes**: npm
- **Build Tool**: Angular CLI

---

## 📋 Pré-requisitos

- Java 21 ou superior
- Node.js 18+ e npm
- Maven 3.6+
- Git

---

## 🚀 Como Executar

### 1. Clonar o repositório
```bash
git clone https://github.com/seu-usuario/smart-store.git
cd smart-store
```

### 2. Executar o Backend
```bash
cd backend
mvn spring-boot:run
```

O backend estará disponível em `http://localhost:8080`

### 3. Executar o Frontend (em outra aba do terminal)
```bash
cd frontend
ng serve
```

O frontend estará disponível em `http://localhost:4200`

---

## Documentação da API

Uma vez que a aplicação estiver rodando, acesse a documentação Swagger em:

```
http://localhost:8080/swagger-ui.html
```

---

## Endpoints Disponíveis

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/products` | Listar todos os produtos |
| GET | `/api/products/{id}` | Obter produto por ID |
| POST | `/api/products` | Criar novo produto |
| PUT | `/api/products/{id}` | Atualizar produto |
| PATCH | `/api/products/{id}` | Atualizar produto parcialmente |
| DELETE | `/api/products/{id}` | Deletar produto |

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/carts` | Criar novo carrinho |
| GET | `/api/carts/{cartId}` | Obter detalhes do carrinho |
| POST | `/api/carts/{cartId}/items` | Adicionar item ao carrinho |
| DELETE | `/api/carts/{cartId}/items/{itemId}` | Remover item do carrinho |

---

## Funcionalidades Planejadas

### SPRINT 1:

- [x] **Setup inicial do projeto com Spring**
- [x] **CRUD de produtos**
- [x] **Frontend simplificado**

### SPRINT 2:

- [x] **Sistema de carrinho e checkout** (Implementação inicial)
- [ ] **Chat conversacional com IA**: Integração com modelos de LLM para recomendações de produtos
- [ ] **Sistema de carrinho e checkout** (Finalizado)
- [ ] **Sistema de usuário**
- [ ] **Autenticação e autorização**
- [ ] **Motor de promoções inteligentes** (Implementação inicial)

### SPRINT 3:

- [ ] **Motor de promoções inteligentes**
- [ ] **Sistema de cashback**
- [ ] **Sistema de notificações**
- [ ] **Sistema de envio de emails**
- [ ] **Cupons e promoções tradicionais**

---

## Banco de Dados

### H2 Console

Para acessar o console H2:

```
http://localhost:8080/h2-console
```

**Credenciais padrão:**
- JDBC URL: `jdbc:h2:file:./data/smartstore-db`
- Usuário: `sa`
- Senha: (deixar em branco)

### Dados Iniciais

Os dados iniciais são carregados do arquivo `products.json` na primeira execução.

---

## 👥 Autores

Desenvolvido como projeto na disciplina.

Matheus Silva;

Gregório Cunha;

Felipe Sousa.

---
