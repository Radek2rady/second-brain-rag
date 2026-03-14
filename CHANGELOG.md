📝 Technical Changelog – Phase: RAG Stability & Security
🎨 Frontend (UI/UX)

    Encoding Fix: Removed hardcoded emojis from .tsx components (previously causing đź encoding artifacts in cloud environments).

    Icon Refactoring: Implemented lucide-react library. Replaced all emojis with standardized SVG icons (FileText, Globe, Lock, Building2).

    ID Formatting: Fixed document ID truncation and ellipsis rendering by replacing special character … with standard ASCII dots ....

🧠 Backend (RAG & Search Logic)

    Cohere Rerank Fail-safe: Implemented a bypass for the CohereRerankAdapter due to 401 Unauthorized API issues.

    Hybrid Search Recovery: HybridSearchService now automatically falls back to the Top 8 candidates from pgvector/Full-text search if Reranking is skipped or fails.

    Similarity Tuning: Adjusted similarityThreshold in VectorDocumentAdapter to 0.30 to optimize retrieval sensitivity.

    Logging: Corrected adapter logging to accurately reflect current thresholds and result counts for better debugging.

🤖 AI & Prompt Engineering

    Strict Citation Policy: Updated System Prompt in SpringAiChatAdapter to enforce factual grounding.

    Anti-Hallucination Guard: Introduced a FORBIDDEN rule preventing the AI from attributing general knowledge (e.g., recipes) to local files unless explicitly present in the context.

    Source Labeling: Standardized visual source separation using prefixes: 📁 Z vašich dokumentů: for local RAG and ⚠️ Následující informace pocházejí z internetu: for web fallback.

🔐 Security & DevOps

    Environment Hygiene: Added .env to .gitignore. Moved sensitive configuration (e.g., VITE_API_BASE_URL) to Railway Environment Variables.

    Git Cleanup: Performed index cleanup (git rm --cached) to ensure no sensitive files remain in the repository history.

    Encoding Standards: Verified and enforced UTF-8 configuration across the entire stack (Java, Postgres, Jackson serialization).