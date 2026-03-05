# Architecture

This project follows **Hexagonal Architecture** (also known as Ports and Adapters) principles.

## Package Structure

- **domain**: Contains the core business logic, entities, and repository### 5. LLM Integration (`org.springframework.ai`)
- Zajišťuje generativní AI (LLM Chat) přes `ChatModel` (např. OpenAI GPT-4o).
- Skládá prompt z instrukcí, kontextu získaného z vektorové databáze (Retrieval) a dotazu uživatele.

## 🔄 Data Flow (RAG Chat)
1. Klient (Frontend) odešle dotaz na `/api/documents/chat`.
2. `DocumentController` předá dotaz do `DocumentService`.
3. `DocumentService` se zeptá `VectorDocumentPort` na podobné dokumenty (Retrieval).
4. `VectorDocumentAdapter` se přes `VectorStore` dotáže Supabase pgvector a vrátí kontext.
5. `DocumentService` složí prompt (systémová instrukce + kontext + dotaz) a zavolá `ChatModel`.
6. LLM (OpenAI) vygeneruje odpověď a service ji vrátí zpět kontroleru.
- **application**: Contains use cases/services that orchestrate domain objects to fulfill business requirements. It acts as the API for the application core.
- **infrastructure**: Contains the implementation of the adapters. This includes database repositories (Spring Data JPA implementations mapping to domain ports), REST controllers, external client integrations, and Spring configuration.
