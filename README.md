# 🧠 Second Brain RAG

> **Your personal AI assistant that answers exclusively from your own documents.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-6DB33F?logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring_AI-1.0_M6-6DB33F?logo=spring)](https://docs.spring.io/spring-ai/reference/)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![Supabase](https://img.shields.io/badge/Supabase-pgvector-3ECF8E?logo=supabase)](https://supabase.com/)

Second Brain RAG is a full-stack **Retrieval-Augmented Generation** application. Upload your documents (`.txt`, `.md`, `.pdf`), and the AI will answer your questions based on them — featuring conversation history and a custom advanced vector search pipeline.

---

## ✨ Key Features

| Feature | Description |
|--------|-------|
| 🔍 **Hybrid Semantic Search** | Documents are stored as vectors (pgvector) and text representations. Searches use a unique combination of LLM Query Expansion, Vector Similarity, and Exact Match Full-Text. |
| 💬 **RAG Chat** | The LLM (GPT-4o) responds **only** based on your context — zero hallucinations. |
| 📝 **Chat History** | Persistent conversation memory stored in PostgreSQL. |
| 📎 **File Upload** | Upload `.txt`, `.md`, and `.pdf` files with automatic normalization and chunking. |
| 🗑️ **Knowledge Management** | View and manage indexed documents directly from the UI. |
| 🌙 **Dark Mode UI** | Modern React frontend inspired by ChatGPT. |

---

## 🏗️ Architecture

The project strictly follows **Hexagonal Architecture** (Ports & Adapters):

```
src/main/kotlin/
├── domain/              ← Core: models + interfaces (ports)
│   ├── VectorDocument, ChatMessage
│   ├── VectorDocumentPort, ChatPort
│   ├── ChatHistoryPort, DocumentSplitterPort
│
├── application/         ← Use Cases: business logic orchestration
│   ├── DocumentService        (RAG: retrieve → generate → save history)
│   ├── HybridSearchService    (Parallel Vector + FullText execution)
│   └── LegalQueryExpander     (LLM-based precise semantic translator)
│
└── infrastructure/      ← Adapters: Spring AI, JPA, REST
    ├── SpringAiChatAdapter          (ChatClient → GPT-4o)
    ├── VectorDocumentAdapter        (pgvector VectorStore)
    ├── FullTextSearchAdapter        (PostgreSQL tsvector & ILIKE)
    └── web/
        ├── DocumentController       (REST API)
        └── WebConfig                (CORS)
```

**Why?** The Domain layer has zero dependencies on Spring AI, JPA, or the web framework → enabling effortless testing and easy swapping of the LLM provider.
*For a detailed look at the RAG Search Pipeline, see [ARCHITECTURE.md](ARCHITECTURE.md).*

---

## 🛠️ Tech Stack

| Layer | Technology |
|--------|------------|
| **Backend** | Kotlin 2.1, Spring Boot 3.4, Spring AI 1.0-M6 |
| **LLM** | OpenAI GPT-4o (via `ChatClient`) |
| **Vector DB** | Supabase PostgreSQL + pgvector |
| **Frontend** | React 19, TypeScript, Vite 7, Tailwind CSS 4 |
| **Testing** | Kotest (BehaviorSpec) + MockK |

---

## 🚀 How to Run

### Prerequisites
- Java 21+
- Node.js 20+
- A [Supabase](https://supabase.com/) account with the pgvector extension enabled
- OpenAI API key

### 1. Backend

```bash
# Set environment variables
export DB_BRAIN_RAG_PASSWORD=your_supabase_password
export OPENAI_API_KEY=sk-...

# Run Spring Boot
./gradlew bootRun
```

The Backend will run on `http://localhost:8080`.

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

The Frontend will run on `http://localhost:5173` with an automatic proxy to the backend.

---

## 📡 REST API

For full API documentation, see [API_DOCS.md](API_DOCS.md).

---

## 🧪 Tests

```bash
./gradlew test
```

Unit tests cover application layer orchestration:
- **`DocumentServiceTest`** — Port mocking, RAG flow verification.
- **`HybridSearchServiceTest`** — Parallel full-text and vector fallback/merge logic verification.
- **`IngestionServiceTest`** — Chunking and bulk save verification.

---

## 📜 License

This project was created for personal learning and experimentation with advanced RAG architectures.

---

<p align="center">
  <em>Built with ❤️, Kotlin, and a touch of AI magic.</em>
</p>
