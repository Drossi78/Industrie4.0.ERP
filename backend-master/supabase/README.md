# Datenbank-Migrationen (Supabase)

Alle SQL-Migrationen für die Stratiq-Datenbank liegen in
`supabase/migrations/` und werden mit der **Supabase CLI** verwaltet.
Die Migrationen sind die single source of truth — niemals direkt in
Studio das Schema ändern.

> **Remote-Deploy macht nur der Admin.** Du arbeitest immer nur lokal:
> Migration schreiben, lokal testen, committen, pushen. Den `db push`
> auf die geteilte Remote-DB übernimmt Moritz.

---

## Voraussetzungen

- **Docker Desktop** muss laufen
- **Supabase CLI**
  ```bash
  brew install supabase/tap/supabase     # macOS
  scoop install supabase                 # Windows
  ```

---

## Einmalige Einrichtung

```bash
cd backend_stratiq

supabase start
```

Beim ersten Mal lädt Docker die Supabase-Images runter (2–5 Minuten).
Danach läuft die lokale Instanz mit allen bisherigen Migrationen +
`seed.sql` automatisch eingespielt.

Verbindungen (per `supabase status` jederzeit abrufbar):

| Service  | URL |
|---|---|
| Studio (Web-UI) | <http://localhost:54323> |
| Postgres        | `postgresql://postgres:postgres@localhost:54322/postgres` |
| API Gateway     | <http://localhost:54321> |

---

## Tägliches Arbeiten

```bash
supabase start                    # falls nicht eh schon läuft
git pull                          # neue Migrationen vom Team holen
supabase migration up             # nur die neuen Migrationen anwenden
```

`supabase migration up` wendet **nur die noch nicht angewendeten**
Migrationen an — deine bestehenden Testdaten bleiben erhalten.

Datenbank ansehen: **Studio** auf <http://localhost:54323>
(oder mit einem Postgres-Client wie DBeaver/TablePlus/IntelliJ auf
`localhost:54322`, User/Pass `postgres`/`postgres`).

`supabase stop` zum Beenden — Daten bleiben erhalten.

### Wann doch `db reset`?

```bash
supabase db reset
```

→ wipt die lokale DB komplett und spielt **alle** Migrationen +
`seed.sql` frisch ein. Nur wenn:

- deine Testdaten kaputt/durcheinander sind und du sauber neu starten willst
- du sichergehen willst, dass deine Migration auch auf einer leeren DB
  sauber durchläuft (Pflicht **bevor** der Admin sie auf Remote pusht)

---

## Neue Migration anlegen

```bash
supabase migration new <kurzer_name>
```

Beispiel:
```bash
supabase migration new add_priority_to_orders
```

Erzeugt eine leere Datei in `supabase/migrations/<timestamp>_<name>.sql`.
SQL reinschreiben, dann lokal anwenden:

```bash
supabase migration up
```

Vor dem Commit zur Sicherheit einmal auf leerer DB testen:
```bash
supabase db reset
```

Wenn alles passt → committen und pushen:
```bash
git add supabase/migrations/<datei>
git commit -m "Add priority column to orders"
git push
```

Der Admin macht später den `supabase db push` auf die Remote-DB.

---

## Regeln

- **Eine logische Änderung pro Migration** — lieber zwei kleine als
  eine riesige.
- **Naming:** `<verb>_<objekt>` — z.B. `add_email_index_to_users`,
  `create_audit_logs_table`, `drop_legacy_assets_table`.
- **Niemals eine bereits gepushte Migration editieren** — Korrekturen
  immer als neue Migration.
- **Niemals direkt in Studio Schema-Änderungen machen** — sonst
  driften lokale DB und Repo auseinander.

---

## Cheatsheet

| Befehl | Wirkung |
|---|---|
| `supabase start` / `stop`     | Lokale Instanz starten / stoppen |
| `supabase status`             | Lokale URLs, Keys, DB-Connection anzeigen |
| `supabase migration up`       | Neue Migrationen anwenden (Daten bleiben) |
| `supabase db reset`           | Lokale DB platt + alle Migrationen + seed neu einspielen |
| `supabase migration new <name>` | Neue Migration anlegen |
| `supabase migration list`     | Liste aller Migrationen (lokal & remote) |

---

## Hilfe

- **Schema-Referenz / ER-Diagramm:** `docs/architecture/`
- **Supabase-Doku:** <https://supabase.com/docs/guides/local-development/cli/getting-started>
