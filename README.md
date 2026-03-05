# 🧠 Second Brain RAG

> **Tvůj osobní AI asistent, který odpovídá výhradně z tvých vlastních dokumentů.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-6DB33F?logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring_AI-1.0_M6-6DB33F?logo=spring)](https://docs.spring.io/spring-ai/reference/)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![Supabase](https://img.shields.io/badge/Supabase-pgvector-3ECF8E?logo=supabase)](https://supabase.com/)

Second Brain RAG je full-stack **Retrieval-Augmented Generation** aplikace. Nahraj své dokumenty (`.txt`, `.md`), a AI ti z nich bude odpovídat — s historií konverzací a vlastním vektorovým vyhledáváním.

---

## ✨ Klíčové funkce

| Funkce | Popis |
|--------|-------|
| 🔍 **Sémantické vyhledávání** | Dokumenty se ukládají jako vektory (pgvector) a vyhledávají se podle významové podobnosti |
| 💬 **RAG Chat** | LLM (GPT-4o) odpovídá **pouze** na základě tvého kontextu — žádné halucinace |
| 📝 **Chat History** | Perzistentní paměť konverzací uložená v PostgreSQL |
| 📎 **File Upload** | Nahrávání `.txt` a `.md` souborů s automatickým chunkingem (max 5 MB) |
| 🗑️ **Knowledge Management** | Prohlížení a mazání dokumentů přímo z UI |
| 🌙 **Dark Mode UI** | Moderní React frontend ve stylu ChatGPT |

---

## 🏗️ Architektura

Projekt striktně dodržuje **Hexagonální architekturu** (Ports & Adapters):

```
src/main/kotlin/
├── domain/              ← Jádro: modely + rozhraní (porty)
│   ├── VectorDocument, ChatMessage
│   ├── VectorDocumentPort, ChatPort
│   ├── ChatHistoryPort, DocumentSplitterPort
│
├── application/         ← Use Cases: orchestrace business logiky
│   ├── DocumentService        (RAG: retrieve → generate → save history)
│   ├── IngestionService       (chunking + bulk save)
│   └── FileIngestionService   (validace souboru + delegace)
│
└── infrastructure/      ← Adaptéry: Spring AI, JPA, REST
    ├── SpringAiChatAdapter          (ChatClient → GPT-4o)
    ├── VectorDocumentAdapter        (pgvector VectorStore)
    ├── JpaChatHistoryAdapter        (JPA → chat_messages)
    ├── SpringAiDocumentSplitterAdapter
    └── web/
        ├── DocumentController       (REST API)
        └── WebConfig                (CORS)
```

**Proč?** Domain vrstva nemá žádnou závislost na Spring AI, JPA ani webu → snadné testování, snadná výměna LLM providera.

---

## 🛠️ Tech Stack

| Vrstva | Technologie |
|--------|------------|
| **Backend** | Kotlin 2.1, Spring Boot 3.4, Spring AI 1.0-M6 |
| **LLM** | OpenAI GPT-4o (přes `ChatClient`) |
| **Vektorová DB** | Supabase PostgreSQL + pgvector |
| **Frontend** | React 19, TypeScript, Vite 7, Tailwind CSS 4 |
| **Testování** | Kotest (BehaviorSpec) + MockK |

---

## 🚀 Spuštění

### Prerekvizity
- Java 21+
- Node.js 20+
- Účet na [Supabase](https://supabase.com/) s povoleným pgvector rozšířením
- OpenAI API klíč

### 1. Backend

```bash
# Nastav env proměnné
export DB_BRAIN_RAG_PASSWORD=tvoje_supabase_heslo
export OPENAI_API_KEY=sk-...

# Spusť Spring Boot
./gradlew bootRun
```

Backend poběží na `http://localhost:8080`.

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend poběží na `http://localhost:5173` s automatickým proxy na backend.

---

## 📡 REST API

| Metoda | Endpoint | Popis |
|--------|----------|-------|
| `POST` | `/api/documents` | Uložení dokumentu |
| `POST` | `/api/documents/ingest` | Ingestování textu s chunkingem |
| `POST` | `/api/documents/upload` | Upload `.txt`/`.md` souboru |
| `GET` | `/api/documents` | Seznam všech dokumentů |
| `DELETE` | `/api/documents/{id}` | Smazání dokumentu |
| `GET` | `/api/documents/search?query=...` | Sémantické vyhledávání |
| `GET` | `/api/documents/chat?query=...&conversationId=...` | RAG chat |
| `GET` | `/api/documents/conversations` | Seznam konverzací |
| `GET` | `/api/documents/chat/history?conversationId=...` | Historie konverzace |

---

## 🧪 Testy

```bash
./gradlew test
```

Unit testy pokrývají orchestraci v aplikační vrstvě:
- **`DocumentServiceTest`** — mockování portů, ověření RAG flow
- **`IngestionServiceTest`** — ověření chunkingu a bulk uložení

---

## 📁 Struktura projektu

```
second-brain-rag/
├── src/main/kotlin/...     # Kotlin backend (Hexagonální architektura)
├── src/test/kotlin/...     # Kotest + MockK testy
├── frontend/               # React + Vite + Tailwind CSS 4
├── ARCHITECTURE.md         # Popis architektonických rozhodnutí
├── MISSION_CONTROL.md      # Interní tracker stavu projektu
└── build.gradle.kts        # Gradle Kotlin DSL
```

---

## 📜 Licence

Tento projekt je vytvořen pro osobní učení a experimentování s RAG architekturou.

---

<p align="center">
  <em>Vytvořeno s ❤️, Kotlinem a trochou AI magie.</em>
</p>
