# 🛠️ Development & Build Checklist

To maintain a stable and buildable codebase, follow these rules before concluding any task or committing changes.

## 1. Backend Verification (Mandatory)
- [ ] **Compilation:** Run `.\gradlew.bat classes` to ensure no Kotlin/Java errors.
- [ ] **Tests:** Run `.\gradlew.bat test` (or specific tests) for affected modules.
- [ ] **Linting:** Check for compiler warnings and address them if they indicate potential bugs.

## 2. Frontend Verification (Mandatory)
- [ ] **Build:** Run `npm run build` (if applicable) or check for obvious syntax errors in TSX files.
- [ ] **Local Run:** Ensure `npm run dev` starts without errors.
- [ ] **English Only:** Verify no non-English strings exist in UI components.

## 3. Deployment & State
- [ ] **Database Schema:** If entities changed, verify `schema.sql` or migrations match the code.
- [ ] **Environment:** Ensure all required ENV variables are documented and present.

## 4. Documentation
- [ ] **Mission Control:** Mark completed tasks and update the roadmap.
- [ ] **Walkthrough:** Document significant changes and lessons learned.

---
> [!IMPORTANT]
> **NEVER** report a task as finished without a successful `BUILD SUCCESSFUL` message from Gradle.
