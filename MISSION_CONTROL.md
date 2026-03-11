# 🧠 Second Brain RAG - Mission Control

## 🎯 Project Goal
Create an intelligent assistant capable of answering queries based on personal documents stored in Supabase.

## 📊 Current Status
- [x] Project initialized (Kotlin, Spring Boot 3.4)
- [x] Hexagonal architecture set up
- [x] Supabase + pgvector integration complete
- [x] Ingestion and Semantic Search (Retrieval) implemented
- [x] LLM Chat endpoint via `ChatClient` and ports implemented
- [x] Backend RAG core fully functional and manually tested
- [x] Code stabilization with Unit tests (Kotest/MockK)
- [x] Chat History (persistent conversational memory via PostgreSQL) implemented

## 🔒 Phase 2: Security & Identity - COMPLETED
- [x] JWT Authentication
- [x] Multi-tenant data isolation
- [x] Database-backed Users & BCrypt hashing
- [x] RBAC (Role-Based Access Control) with ADMIN/USER roles
- [x] Audit Logging (Async tracking of all critical actions)
- [x] Admin Dashboard (Audit logs & User management UI)

## ⚙️ Phase 3: Automation & Enhancements - NEXT
- [ ] **Google Drive Integration:** Automatic ingestion adapter for cloud documents.
- [x] **Reranking:** Integrate Cohere Reranker to improve retrieval precision - COMPLETED
- [ ] **Advanced PDF Processing:** Improved layout analysis for complex tables.

## 💻 Phase 4: UI (Frontend) - IN PROGRESS
- [x] Vite + React + TailwindCSS base setup
- [x] Authentication & Login flow
- [x] Admin Tools (Audit & Users)
- [ ] Full-featured Chat Interface

## 🛠️ Tech Stack
- **Backend:** Kotlin, Spring AI, Spring Security, Spring Data JPA
- **Database:** Supabase (PostgreSQL + pgvector)
- **Frontend:** React, Vite, TailwindCSS
- **LLM:** OpenAI (GPT-4o)
- **Testing:** Kotest, MockK

## 📝 Notes
- Role-based security is enforced both at the API level (Spring Security) and UI level.
- BCrypt hashes are standardized for demo users (initial password is 'password').
- Audit logs are paginated and sorted by newest first.

## ⚠️ Known Issues
- **Semantic Gap:** AI might struggle with specific names or terms in complex legal texts.
- **PDF Layouts:** Complex tables or specific layouts from PDFs sometimes lose their format after chunking.
- **Async Context Passing:** Security Context (and TenantID) must be explicitly passed into async methods (@Async) to avoid losing them on the new thread.
