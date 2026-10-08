# Contributing to DragonDeal

Thank you for contributing to DragonDeal! Here are some guidelines to help.

## Development Workflow

1. **Create a feature branch** from `develop` (or `main` if `develop` doesn't exist)
   ```bash
   git checkout -b feature/my-feature develop
   ```

2. **Make your changes** following the existing architecture patterns:
   - New screens → Activity + ViewModel + Repository
   - New API endpoints → Add to existing Retrofit interface or create new one
   - New dependencies → Register in `AppModule.kt` (Koin)

3. **Write tests** for new functionality
   - Unit tests go in `app/src/test/java/com/elvsf/dragondeal/`
   - Mirror the source package structure

4. **Commit with conventional messages**
   ```
   feat: add seller rating display
   fix: resolve chat message duplicate issue
   test: add ViewModel tests for OrdersScreen
   refactor: extract error handling to ApiCallback
   ```

5. **Push and create a PR**
   ```bash
   git push origin feature/my-feature
   ```

## Code Style

- **Kotlin files**: `.kt` extension, PascalCase for classes, camelCase for functions
- **Java files**: Standard Java conventions (legacy code)
- **Resources**: `snake_case` for XML resource names
- **Packages**: lowercase, dot-separated

## Architecture Rules

- **No Context in repositories** — use DI to provide Context-dependent components
- **ViewModels own UI state** — Activities/Fragments observe, don't manage state directly
- **Repositories are the single source of truth** — no direct API calls from ViewModels
- **Use `ApiCallback<T>`** for all async API operations — centralized error handling

## Before Submitting a PR

- [ ] `./gradlew test` passes
- [ ] `./gradlew assembleDebug` builds successfully
- [ ] No new lint warnings
- [ ] Tests cover new logic
