# 🧠 Second Brain RAG - Mission Control

## 🎯 Cíl projektu
Vytvořit inteligentního asistenta, který umí odpovídat na dotazy na základě mých vlastních dokumentů uložených v Supabase.

## 📊 Aktuální Stav
- [x] Projekt inicializován (Kotlin, Spring Boot 3.4)
- [x] Hexagonální architektura nastavena
- [x] Propojení se Supabase + pgvector hotovo
- [x] Implementováno ukládání a sémantické vyhledávání (Retrieval)
- [x] Implementace LLM Chat endpointu přes `ChatClient` a porty
- [x] Backendové jádro RAG je plně funkční a otestované manuálně přes HTTP clienta (Alíkův test prošel!)
- [x] Stabilizace kódu pomocí Unit testů v Kotest/MockK
- [x] Implementace Chat History (perzistentní paměť konverze přes PostgreSQL)

## 🔒 Phase 2: Security
- [x] JWT Authentication
- [x] Multi-tenant data isolation
- [x] DB Users & BCrypt
- [x] RBAC (Role-Based Access Control)
- [x] Audit Logging

## ⚙️ Phase 3: Automation & Enhancements - ROADMAP
- [ ] Google Drive Integration
- [ ] Reranking (Cohere/BGE)

## 💻 Phase 4: UI (Frontend) - ROADMAP
- [x] CORS for frontend
- [ ] React/Vite Chat Frontend - **IN PROGRESS**

## 🛠️ Tech Stack
- **Backend:** Kotlin, Spring AI, Spring Security, Spring Data JPA
- **Database:** Supabase (PostgreSQL + pgvector)
- **Frontend:** React, Vite, TailwindCSS
- **LLM:** OpenAI (GPT-4o)
- **Testing:** Kotest, MockK

## 📝 Notes
- UUIDs are strictly validated across adapters.
- Open-in-view warnings addressed in Spring JPA.

## ⚠️ Known Issues
- **Semantic Gap:** AI might struggle with specific names or terms in complex legal texts.
- **PDF Layouts:** Complex tables or specific layouts from PDFs sometimes lose their format after chunking.
- **Async Context Passing:** Security Context (and TenantID) must be explicitly passed into async methods (@Async) to avoid losing them on the new thread.
