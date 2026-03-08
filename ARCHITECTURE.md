# Architecture

This project follows **Hexagonal Architecture** (also known as Ports and Adapters) principles, designed for flexibility, testability, and clear separation of concerns.

## Package Structure

- **domain**: Contains the core business logic, entities, and repository interfaces (ports).
- **application**: Contains use cases/services that orchestrate domain objects to fulfill business requirements. It acts as the API for the application core.
- **infrastructure**: Contains the implementation of the adapters. This includes database repositories (Spring Data JPA implementations mapping to domain ports), REST controllers, external client integrations, and Spring configuration.

## 🔄 Unique RAG Pipeline Data Flow

The search and retrieval engine implements a sophisticated, multi-layered RAG (Retrieval-Augmented Generation) pipeline specifically optimized for legal domain queries (e.g., Czech Civil Code).

### 1. Preprocessing (Data Ingestion)
- The pipeline begins when a user uploads PDFs or text files.
- `PdfParsingAdapter` and `SpringAiDocumentSplitterAdapter` extract the text.
- **Text Normalization**: All invisible Unicode characters are stripped, and multiple whitespaces/tabs are collapsed to single spaces to maximize semantic quality before chunking.
- The chunks are then saved simultaneously into **Full-Text** (`tsvector` in PostgreSQL) and **Vector** (pgvector in Supabase) representations.

### 2. Legal Query Expansion (Dynamic Translation)
- When a user asks a question (e.g., *"How to evict a tenant who doesn't pay?"*), the raw query is intercepted by the `LegalQueryExpander`.
- This component invokes the LLM (`ChatClient`) with a strict Czech system prompt (acting as an expert legal assistant).
- The LLM translates the layman's query into precise Czech legal terminology (e.g., *"výpověď z nájmu bytu pro hrubé porušení povinností nájemce"*).
- If the expansion fails or times out, the system gracefully falls back to the original query.

### 3. Hybrid Search
- The translated *Expanded Query* is forwarded to the `HybridSearchService`, which launches two asynchronous/parallel searches:
  - **Robust Full-Text Search**: Uses `FullTextSearchAdapter`. It implements custom TSQuery structures (e.g., handling synonyms with OR operators like `(bazar | (použitá & věc))`) and utilizes resilient `ILIKE` fallback patterns to catch exact legal paragraphs despite whitespace variations.
  - **Semantic Vector Search**: Uses `VectorDocumentAdapter`, querying the pgvector database using a lowered similarity threshold (`0.65`) to capture a broader context of related legal concepts.

### 4. Merging and Reranking
- The `HybridSearchService` combines both sets of results.
- **Deduplication**: Results are deduplicated based on the document ID.
- **Prioritization**: Full-text results are placed at the top of the list because exact paragraph or keyword matches generally have higher legal relevance than pure semantic similarities.

### 5. LLM Integration (`org.springframework.ai`)
- The final merged context list is passed to the `SpringAiChatAdapter` alongside the original request.
- The ChatGPT model compiles the final answer following strict constraints—enforcing citations referencing the exact source documents and matching paragraphs, while avoiding hallucinations.
- The synthesized response is then returned to the frontend.
