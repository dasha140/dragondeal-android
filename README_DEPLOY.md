# DragonDeal — Build & Deploy Guide

## Android

### Debug build (local development)

Собирается с URL из `local.properties` (или fallback на локальный IP):

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

Чтобы изменить локальный backend URL, отредактируйте `local.properties` (файл НЕ в git):

```
API_BASE_URL=http://192.168.0.111:8081/api/
```

### Release build (production / Railway)

**Требует** параметр `API_BASE_URL_RELEASE`. Без него сборка упадёт с понятной ошибкой:

```bash
./gradlew assembleRelease -PAPI_BASE_URL_RELEASE=https://your-app.up.railway.app/api/
```

APK: `app/build/outputs/apk/release/app-release.apk`

⚠️ **Нельзя собрать release с локальным IP** — Gradle требует production URL.

## Backend

### Local development (H2)

```bash
cd /path/to/Marketgm
mvn spring-boot:run
```

- Порт: `8081`
- DB: H2 файл `./data/marketgm-db`
- JWT: dev-ключ (не для production)
- Mail dev mode: `APP_MAIL_DEV_MODE=true mvn spring-boot:run`

### Railway / Production (PostgreSQL)

Railway env variables:

| Переменная | Значение |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `JWT_SECRET` | `openssl rand -hex 32` |
| `MAIL_USERNAME` | Gmail аккаунт |
| `MAIL_PASSWORD` | Gmail App Password |
| `MAIL_FROM` | `noreply@marketgm.com` |
| `APP_MAIL_DEV_MODE` | `false` |

Railway автоматически подключает PostgreSQL и injects `DATABASE_URL`.

Локальная эмуляция prod:

```bash
SPRING_PROFILES_ACTIVE=prod \
DATABASE_URL="jdbc:postgresql://localhost:5432/marketgm" \
SPRING_DATASOURCE_USERNAME="postgres" \
SPRING_DATASOURCE_PASSWORD="secret" \
JWT_SECRET="ваш-секретный-ключ-32-байта" \
MAIL_USERNAME="your@gmail.com" \
MAIL_PASSWORD="app-password" \
mvn spring-boot:run
```
