# DragonDeal 🐉

Android marketplace application with blockchain-based escrow for secure peer-to-peer transactions.

## Overview

DragonDeal is a mobile marketplace where users can browse listings, communicate with sellers, and complete secure transactions protected by a blockchain escrow smart contract.

### Features

- **Browse & Search** — View listings with images, prices, and seller details
- **Secure Transactions** — Blockchain escrow via `DragonDealEscrow.sol` smart contract
- **Real-time Chat** — In-app messaging between buyers and sellers
- **Order Management** — Track orders through the full lifecycle
- **Payment Integration** — Multiple payment methods with escrow protection
- **Seller Profiles** — View ratings, reviews, and seller history
- **Authentication** — Email-based auth with verification

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin + Java |
| **Architecture** | MVVM with Repository pattern |
| **DI** | Koin |
| **Networking** | Retrofit 2 + OkHttp 4 |
| **Image Loading** | Glide |
| **Async** | ViewModel + LiveData |
| **Testing** | JUnit 5, Mockito, Mockito-Kotlin |
| **Build** | Gradle (Kotlin DSL) |
| **Backend** | Spring Boot (MarketGM) |
| **Blockchain** | Solidity (Escrow contract) |

## Project Structure

```
app/src/main/java/com/elvsf/dragondeal/
├── DragonDealApp.kt          # Application entry point + Koin init
├── adapter/                  # RecyclerView adapters
├── api/                      # Retrofit clients, interceptors, repositories
│   ├── chat/                 # Chat API layer
│   └── payment/              # Payment API layer
├── di/                       # Koin dependency injection modules
├── dto/                      # Data Transfer Objects (API responses)
├── model/                    # Domain models
├── payment/                  # Payment-related components
├── screens/                  # Activities (UI screens)
├── storage/                  # Local data storage (tokens, prefs)
├── ui/                       # Custom UI components
├── util/                     # Utilities, callbacks, error handlers
└── viewmodel/                # ViewModels for each screen
```

## Getting Started

### Prerequisites

- **Android Studio** (latest stable)
- **JDK 11+**
- **Android SDK** — API 26 (min) → API 36 (target)
- **Backend** running (MarketGM Spring Boot) — see [Backend Setup](#backend)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/dasha140/dragondeal-android.git
   cd dragondeal-android
   ```

2. **Configure backend URL**
   
   Create/edit `local.properties` (not committed to git):
   ```properties
   API_BASE_URL=http://your-backend-host:8081/api/
   ```
   
   Default fallback: `http://dragondeal.online:8081/api/`

3. **Open in Android Studio**
   - File → Open → select project directory
   - Let Gradle sync complete

4. **Run**
   - Click ▶ Run or `Shift+F10`
   - Or from terminal: `./gradlew installDebug`

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires production URL)
./gradlew assembleRelease -PAPI_BASE_URL_RELEASE=https://your-app.up.railway.app/api/
```

APK output:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

### Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Backend

The Android app connects to the **MarketGM** Spring Boot backend:

- **Local dev**: H2 database, JWT dev mode
- **Production**: PostgreSQL on Railway

See `README_DEPLOY.md` for detailed backend setup instructions.

## Blockchain

The escrow smart contract (`DragonDealEscrow.sol`) handles secure P2P transactions:

- **Contract**: `IdeaProjects/Marketgm/blockchain/contracts/DragonDealEscrow.sol`
- **Tests**: TypeScript + Hardhat
- **Bindings**: Ethers.js generated types

## Architecture

### MVVM Pattern

```
Activity/Fragment → ViewModel → Repository → API/Storage
                        ↑
                    LiveData (reactive updates)
```

### Dependency Injection (Koin)

```kotlin
// AppModule.kt defines all injections
val appModule = module {
    single { RetrofitClient.create() }
    factory { AuthInterceptor() }
    // ...
}
```

### Error Handling

Centralized via `ApiCallback<T>` — all API calls return consistent error structures parsed by `BackendErrorParser`.

## Branching Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code |
| `develop` | Integration branch for features |
| `feature/*` | New features |
| `fix/*` | Bug fixes |
| `release/*` | Release preparation |

## CI/CD

GitHub Actions workflow runs on every PR:
- Gradle build (`assembleDebug`)
- Unit tests (`testDebugUnitTest`)
- Lint checks

## License

Private project — all rights reserved.

## Contact

- **GitHub**: [@dasha140](https://github.com/dasha140)
