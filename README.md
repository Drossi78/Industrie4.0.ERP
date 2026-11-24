Ask User
Completed with input: {
  "message": "Deine Nachricht klingt widersprüchlich: Soll ich eine README-Datei erstellen und im Projekt speichern, oder möchtest du den README-Text nur hier im Chat und ausdrücklich keine Datei im Projekt?",
  "requestedSchema": {
    "properties": {
      "readme": {
        "type": "string",
        "title": "Was soll ich tun?",
        "oneOf": [
          {
            "const": "save",
            "title": "README-Datei im Projekt erstellen"
          },
          {
            "const": "chat",
            "title": "Nur Text hier im Chat, nichts im Projekt ändern"
          }
        ]
      }
    }
  }
}

Alles klar – ich ändere nichts an deinem Projekt. Hier ist ein README-Entwurf zum Kopieren:

```markdown
# Stratiq

Stratiq ist eine Webanwendung zur Lager- und Auftragsverwaltung für Industrie 4.0. Sie besteht aus einem Angular-Frontend, einem Spring-Boot-Backend und einer PostgreSQL-Datenbank.

## Wie funktioniert die Anwendung?

Das Frontend sendet HTTP-Anfragen an die REST-API des Backends. Das Backend verarbeitet die Anfragen und liest oder speichert Daten in PostgreSQL. Für geschützte Funktionen meldet man sich an; die API verwendet JWT-Tokens zur Authentifizierung.

Zu den Bereichen gehören Artikel, Aufträge, Lagerbestand, Kunden, Lieferanten, Rechnungen und Produktion.

## Voraussetzungen

- Java 25
- Node.js (empfohlen: eine LTS-Version)
- Docker

## Lokal starten

Öffne drei Terminals im Projektordner.

### 1. PostgreSQL starten

Einmalig einen lokalen Datenbank-Container anlegen:

```bash
docker run --name stratiq-postgres -d \
  -e POSTGRES_DB=postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -v stratiq-db:/var/lib/postgresql/data \
  postgres:17-alpine
```

Bei späteren Starts genügt:

```bash
docker start stratiq-postgres
```

Beim ersten Start muss außerdem das Datenbankschema eingespielt werden:

```bash
docker exec -i stratiq-postgres psql -U postgres -d postgres \
  < backend-master/src/main/resources/db/migration/V1__Complete_schema.sql
```

### 2. Backend starten

```bash
cd backend-master
SPRING_DATASOURCE_URL='jdbc:postgresql://127.0.0.1:5432/postgres?sslmode=disable' \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=postgres \
./mvnw spring-boot:run
```

Das Backend läuft unter [http://localhost:8080](http://localhost:8080). Die API-Dokumentation ist unter [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) verfügbar.

### 3. Frontend starten

```bash
cd frontend-master/stratiq-frontend
npm install
npm start
```

Das Frontend ist unter [http://localhost:4200](http://localhost:4200) erreichbar.

## Anmeldung

Lokale Entwicklungszugangsdaten:

- E-Mail: `admin@stratiq.com`
- Passwort: `admin123`

## Sicherheit

Echte Datenbank-Zugangsdaten und JWT-Schlüssel gehören nicht in ein öffentliches GitHub-Repository. Verwende für produktive Umgebungen Umgebungsvariablen oder einen Secrets Manager.
```
