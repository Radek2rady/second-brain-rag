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
- [x] Implementace Chat History (perzistentní paměť konverze přes PostgreSQL) a CORS pro frontend
- [ ] Frontend pro chatování (React/Vite) - **NA ŘADĚ**

## 🛠️ Tech Stack
- **Backend:** Kotlin, Spring AI
- **Vektorová DB:** Supabase (PostgreSQL + pgvector)
- **LLM:** OpenAI (GPT-4o)
- **Testing:** Kotest, MockK

## 📝 Poznámky
- UUID u dokumentů je striktně validováno obousměrně v adaptérech.
- Ošetřeny Open-in-view warningy ze Spring JPA.
