# Stratiq Frontend

Angular 21 SPA für das Stratiq Inventory & Order Management System.  
Verbindet sich mit dem Spring Boot Backend (`README-backend.md`).

---

## Tech Stack

| Layer | Technologie |
|---|---|
| Framework | Angular 21 (Standalone Components) |
| Styling | Tailwind CSS v4 + custom Fluent Design System |
| Sprache | TypeScript |
| HTTP | Angular HttpClient + funktionaler Interceptor |
| State | Angular Signals |
| Routing | Angular Router (lazy-loaded, mit Auth Guard) |
| Auth | JWT (sessionStorage) |
| i18n | Eigenes JSON-basiertes System (DE / EN / FR) |

---

## Starten

```bash
ng serve
```

App läuft auf **http://localhost:4200**  
Nutzt automatisch `environment.development.ts` (Development-Konfiguration).

---

## Umgebungsvariablen

| Datei | Zweck |
|---|---|
| `environment.ts` | Fallback / Tests — Auth: `localhost:3000` |
| `environment.development.ts` | Lokale Entwicklung |
| `environment.prod.ts` | Produktions-Build |

Zwei URL-Variablen:

```ts
authUrl: 'http://localhost:8080/api'     // → /api/auth/login
apiUrl:  'http://localhost:8080/api/v1'  // → alle /api/v1/* Endpunkte
```

- `authUrl` wird nur vom `AuthService` genutzt
- `apiUrl` wird von allen Feature-Services genutzt (Items, Orders, …)

---

## Architektur

```
src/app/
├── core/
│   ├── guards/
│   │   └── auth.guard.ts          # Leitet auf /auth/login um wenn kein JWT
│   ├── interceptors/
│   │   └── auth.interceptor.ts    # Hängt Bearer-Token an alle Requests
│   └── i18n/
├── features/
│   ├── auth/                      # Login, AuthService, AuthSessionService
│   ├── dashboard/                 # Startseite mit Bereichs-Kacheln
│   ├── items/                     # Artikel-Stammdaten
│   ├── orders/                    # Aufträge
│   └── inventory/                 # (Stub)
└── shared/
    ├── models/common.model.ts     # Uuid, IsoDateTime, Paginated<T>
    ├── enums/                     # OrderStatus, InvoiceStatus, …
    └── utils/
```

---

## Routing

| Pfad | Komponente | Guard |
|---|---|---|
| `/` | → `/dashboard` | — |
| `/auth/login` | `Login` | — |
| `/dashboard` | `Dashboard` | `authGuard` |
| `/items` | `ItemsList` | `authGuard` |
| `/orders` | `OrdersList` | `authGuard` |

Neue geschützte Routen immer mit `canActivate: [authGuard]` in [app.routes.ts](src/app/app.routes.ts) eintragen.

---

## Authentifizierung

**Flow:**
1. `POST /api/auth/login` → `{ accessToken, refreshToken, expiresIn, username }`
2. `AuthSessionService.applyLogin()` speichert Tokens + User in `sessionStorage`
3. `authInterceptor` hängt `Authorization: Bearer <token>` an jeden Request

**Keys im sessionStorage:**
- `stratiq.accessToken`
- `stratiq.refreshToken`
- `stratiq.user`

**Login-Credentials (Entwicklung):** `admin@stratiq.com` / `admin123`

---

## API-Antwortformate

Das Backend liefert je nach Endpunkt unterschiedliche Formate:

### Flat Array (z. B. Items)
```json
[ { "id": "...", "name": "..." }, … ]
```
→ Service gibt `Observable<T[]>` zurück.

### Spring Page (z. B. Orders)
```json
{
  "content": [ { "id": "...", … } ],
  "totalElements": 15,
  "totalPages": 1,
  "number": 0,
  "size": 20,
  "first": true,
  "last": true
}
```
→ Service gibt `Observable<Paginated<T>>` zurück, Komponente liest `response.content`.

Das `Paginated<T>`-Interface in `common.model.ts` bildet das Spring-Page-Format ab.

---

## Internationalisierung (i18n)

Eigenes leichtgewichtiges System ohne externe Bibliothek.

**Dateien:**
- Übersetzungen: `public/i18n/de.json`, `en.json`, `fr.json`
- Service: `src/app/core/i18n/i18n.service.ts`
- Pipe: `src/app/core/i18n/translate.pipe.ts`

**Verwendung im Template:**
```html
{{ 'my.key' | t }}
```

**Verwendung im TypeScript** (z. B. für dynamische Keys):
```ts
private readonly i18n = inject(I18nService);
this.i18n.t('my.key');
```

**Sprache wechseln:** `I18nService.setLanguage('en')` — wird in `localStorage` unter `stratiq.lang` gespeichert und beim nächsten Start wiederhergestellt. Standard: `de`.

**Neue Übersetzung hinzufügen:** Key in allen drei JSON-Dateien ergänzen, dann im Template mit `{{ 'key' | t }}` verwenden. Fehlende Keys geben den Key selbst zurück (kein Crash).

**Key-Konvention:** `<feature>.<bereich>.<name>` — z. B. `items.col.name`, `orders.status.confirmed`.

**Bestehende Namespaces:**

| Namespace | Seite |
|---|---|
| `login.*` | Login |
| `dashboard.*` | Dashboard |
| `items.*` | Artikel-Liste |
| `orders.*` | Auftrags-Liste |

---

## Design System

Tailwind CSS v4 mit Custom Theme (`src/styles.css`).

**Theme-Variablen** (auszugsweise):

| Variable | Wert |
|---|---|
| `--color-primary` | `#5d87ff` |
| `--color-primary-soft` | `#e8efff` |
| `--color-text` | `#111827` |
| `--color-text-muted` | `#6b7280` |
| `--radius-card` | `1.25rem` |
| `--radius-control` | `0.9rem` |
| `--shadow-card` | weicher Schatten |
| `--shadow-card-lg` | stärkerer Hover-Schatten |

**Komponenten-Klassen** (in `@layer components`):

| Klasse | Verwendung |
|---|---|
| `.page-shell` | Seiten-Wrapper mit Hintergrund-Gradient |
| `.page-title` | H1-Überschrift |
| `.page-subtitle` | Untertitel |
| `.fluent-card` | Glassmorphism-Karte |
| `.fluent-button-primary` | Primär-Button mit Gradient |
| `.fluent-table` | Tabelle |
| `.fluent-row` | Tabellenzeile mit Hover |
| `.fluent-cell` | Tabellenzelle (muted) |
| `.fluent-cell-primary` | Tabellenzelle (bold) |
| `.fluent-badge` | Badge blau (primary) |
| `.fluent-badge-success` | Badge grün |
| `.fluent-badge-warning` | Badge gelb |
| `.fluent-badge-danger` | Badge rot |
| `.fluent-badge-neutral` | Badge grau |
| `.dashboard-tile` | Kachel mit Hover-Lift-Effekt |

**Konvention:** Kein CSS in Component-Dateien — Tailwind-Utilities im Template, komplexe Hover-/Pseudo-States in `styles.css @layer components`.

---

## Features

### Dashboard (`/dashboard`)
Zentriertes Kachel-Grid mit Links zu den Funktionsbereichen.  
Neue Bereiche in `areas`-Array in [dashboard.ts](src/app/features/dashboard/dashboard.ts) ergänzen.

### Artikel (`/items`)
Tabelle aller Stammdaten-Artikel vom Endpunkt `GET /api/v1/items`.

**Item-Felder:** `id`, `name`, `kind` (`OWN_PRODUCT` | `PURCHASED`), `categoryId`, `categoryName`, `unit`, `price`, `description`, `notes`, `isDeleted`, `createdAt`

### Aufträge (`/orders`)
Tabelle aller Aufträge vom Endpunkt `GET /api/v1/orders` (paginiert).

**Order-Felder:** `id`, `orderNumber`, `status` (`DRAFT` | `CONFIRMED` | `COMPLETED` | `CANCELLED`), `customerId`, `orderDate`, `expectedDate`, `confirmedAt`, `cancelledAt`, `totalAmount`, `notes`, `createdAt`

---

## Neue Feature-Seite hinzufügen

1. Service in `features/<name>/services/` anlegen — `apiUrl` aus `environment` nutzen
2. `pages/<name>-list/<name>-list.ts` mit `signal<T[]>` für Daten + `loading`-Signal
3. Template mit `.page-shell`, `.fluent-card`, `.fluent-table`
4. `<name>.routes.ts` anlegen
5. Route in [app.routes.ts](src/app/app.routes.ts) mit `canActivate: [authGuard]` eintragen
6. Kachel im Dashboard ergänzen
7. Übersetzungskeys (`<name>.title`, `<name>.subtitle`, `<name>.col.*`, …) in alle drei JSON-Dateien eintragen

---

## Angular CLI

```bash
ng serve          # Dev-Server starten
ng build          # Produktions-Build
ng test           # Unit Tests (Vitest)
ng generate component <name>
```
