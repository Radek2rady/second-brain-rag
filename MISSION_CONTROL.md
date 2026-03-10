# 🧠 Second Brain RAG - Mission Control

## 🎯 Cíl projektu
Vytvořit inteligentního asistenta, který umí odpovídat na dotazy na základě mých vlastních dokumentů uložených v Supabase.

## � Fáze 1: Jádro & Vyhledávání (Search) - HOTOVO
- [x] Projekt inicializován (Kotlin, Spring Boot 3.4)
- [x] Hexagonální architektura nastavena
- [x] Propojení se Supabase + pgvector hotovo
- [x] Implementováno ukládání a sémantické vyhledávání (Retrieval)
- [x] Implementace LLM Chat endpointu přes `ChatClient` a porty
- [x] Backendové jádro RAG je plně funkční a otestované manuálně
- [x] Stabilizace kódu pomocí Unit testů v Kotest/MockK
- [x] Implementace Chat History (perzistentní paměť konverze přes PostgreSQL)

## 🔒 Fáze 2: Bezpečnost (Security)
- [x] JWT Autentizace a generování tokenů
- [x] Multi-tenant data isolation (Tenant Isolation)
- [ ] RBAC (Role-Based Access Control) - **PRÁVĚ PROBÍHÁ**
- [ ] Audit Logging - **PRÁVĚ PROBÍHÁ**

## ⚙️ Fáze 3: Automatizace a Vylepšení (Automation) - ROADMAP
- [ ] Integrace s Google Drive
- [ ] Implementace Rerankingu (Cohere/BGE)

## 💻 Fáze 4: Uživatelské rozhraní (Frontend) - ROADMAP
- [x] CORS pro frontend
- [ ] Frontend pro chatování (React/Vite)

## 🛠️ Tech Stack
- **Backend:** Kotlin, Spring AI, Spring Security
- **Vektorová DB:** Supabase (PostgreSQL + pgvector)
- **LLM:** OpenAI (GPT-4o)
- **Testing:** Kotest, MockK

## 📝 Poznámky
- UUID u dokumentů je striktně validováno obousměrně v adaptérech.
- Ošetřeny Open-in-view warningy ze Spring JPA.

## ⚠️ Known Issues
- **Semantic Gap:** AI může mít problém s identifikací správných jmen nebo specifických výrazů u složitějších textů.
- **PDF Layouts:** Komplexní tabulky nebo specifické rozložení z PDF dokumentů někdy nemusí zachovat svůj původní formát po chunkování.
- **Async Context passing:** Security Context (a TenantID) je nutné explicitně předávat do asynchronních metod (@Async), jinak dojde k jeho ztrátě na novém vlákně.
