# 📦 TrackSuit

**Gmail-based delivery tracker** — automatically fetches order emails from Gmail and tracks your deliveries in one place.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | Next.js, React, TypeScript |
| **Backend** | Spring Boot 4, Java 17, Gradle |
| **Database** | MySQL 8 (Docker) |
| **Auth** | Google OAuth 2.0 |
| **Email** | Gmail API (read-only) |

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- Docker (for MySQL)
- Google Cloud project with OAuth credentials and Gmail API enabled

### 1. Clone the repo

```bash
git clone https://github.com/YOUR_USERNAME/TrackSuit.git
cd TrackSuit
```

### 2. Set up environment variables

```bash
cp .env.example .env
```

Edit `.env` and fill in your actual values:

```env
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
```

### 3. Start MySQL (Docker)

```bash
docker-compose up -d
```

### 4. Run the backend

```bash
cd backend/backend
./gradlew bootRun
```

The backend starts on `http://localhost:8080`.

### 5. Run the frontend

```bash
# From project root
pnpm install
pnpm dev
```

The frontend starts on `http://localhost:3000`.

## API Endpoints

### Gmail OAuth

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/gmail/connect` | GET | Redirects to Google OAuth consent screen |
| `/api/v1/gmail/callback` | GET | Handles OAuth callback, exchanges code for token |
| `/api/v1/gmail/fetch` | GET | Fetches and parses order emails from Gmail |
| `/api/v1/gmail/status` | GET | Check Gmail connection status |

### Orders

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/orders` | GET | List all tracked orders |
| `/api/v1/orders` | POST | Create a new order |

## OAuth Flow

1. Visit `http://localhost:8080/api/v1/gmail/connect`
2. Login with your Google account on the consent screen
3. After authorization, you're redirected to `/callback` with an access token
4. Visit `http://localhost:8080/api/v1/gmail/fetch` to pull order emails

## Google Cloud Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a project → Enable **Gmail API**
3. Create **OAuth 2.0 Client ID** (Web application)
4. Add authorized redirect URI: `http://localhost:8080/api/v1/gmail/callback`
5. Add your email as a **test user** under OAuth consent screen
6. Copy Client ID and Secret into your `.env` file

## License

MIT
