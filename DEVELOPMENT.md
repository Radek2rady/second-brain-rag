# Development Guide & Agentic Workflow

Welcome to the Enterprise Architecture of the Second Brain RAG system! This document describes our Agentic Workflow and our strategy for iterative search optimization.

## 1. Agentic Workflow

We employ an **Agentic Workflow** where AI agents (like Antigravity) function as autonomous but collaborative engineering partners. The workflow consists of three primary phases:

### A. PLANNING
- **Goal:** Analyze the current state, determine requirements, and propose a concrete technical design.
- **Process:** The agent actively researches the codebase, inspects existing architecture (e.g., Hexagonal Architecture ports and adapters), and authors an `implementation_plan.md`.
- **Review:** We do not proceed to execution without explicit User Approval of the implementation plan, ensuring alignment on critical design choices like security mechanisms (e.g., Spring Security integration for multi-tenancy).

### B. EXECUTION
- **Goal:** Implement the approved plan safely and incrementally.
- **Process:** The agent modifies the codebase using targeted tools (e.g., AST-aware replacements or regex searches), ensuring precise edits. Adapters and Services are updated systematically while respecting the bounds of the Hexagonal Architecture.
- **Iteration:** If unexpected complexities arise, the agent may fall back to PLANNING to redefine the approach.

### C. VERIFICATION
- **Goal:** Validate the correctness of the execution.
- **Process:** Extensive automated testing (JUnit/Kotest), Spring Boot context verification, and manual UI/API tests are performed.
- **Delivery:** A `walkthrough.md` is generated, serving as a transparent proof-of-work detailing what was accomplished and verified.

---

## 2. Iterative Search Optimization

Our RAG system relies heavily on our ability to precisely retrieve the correct context from the `vector_store`. We optimize search iteratively in the following ways:

### A. Hybrid Search Strategy
We combine **Semantic Search** (via pgvector and Spring AI) with **Full-Text Search** (PostgreSQL `tsvector` and `GIN` indices) to maximize recall:
- Semantic search captures the *meaning* and *intent* of the query.
- Full-Text Search ensures exact keyword matches (especially critical for legal texts, paragraphs like "§ 2165", or specific product names).

### B. Metadata Filtering & Multi-Tenancy (Data Isolation)
As we evolve into an Enterprise Architecture, search must be strictly partitioned. User A must never see User B's documents, even if they use the same keywords or a 0.00 similarity threshold.
- **Tenant Isolation:** Every document ingested is tagged with a `tenantId` (extracted from the JWT subject) in its metadata map (`metadata["tenantId"] = tenantId`).
- **Stateless Security:** We use Spring Security with a custom `JwtFilter` to establish the security context statelessly.
- **Explicit Context Passing:** To avoid issues with asynchronous processing (`@Async` threads losing the `SecurityContext`), the `tenantId` is explicitly extracted in the REST Controller and passed down as an argument through the Application Services and into the Domain Ports.
- **Search-Time Filtering:** 
  - `VectorDocumentAdapter` uses Spring AI's `.filterExpression("tenantId == '...'")` to push down predicates directly to the pgvector store.
  - `FullTextSearchAdapter` incorporates JSONB metadata checks (`AND metadata->>'tenantId' = ?`) into its JDBC queries to filter at the database level before ranking.

### C. Fallback Mechanisms
To guarantee robustness, our Full-Text search employs dynamic fallbacks:
1. **TSQuery:** Tries to build a sophisticated `to_tsquery` matching linguistic roots.
2. **ILIKE Fallback:** If TSQuery yields 0 results (e.g., due to unusual query structure), the system autonomously falls back to a multi-token `ILIKE` substring search to rescue potential matches.

By continuously refining these SQL queries, adjusting AI similarity thresholds, and strictly enforcing tenant metadata, we ensure the search engine is enterprise-ready, fast, and secure.
