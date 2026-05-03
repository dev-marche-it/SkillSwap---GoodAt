# SkillSwap — Mappa API REST (preparazione integrazione web)

Questo documento descrive gli endpoint HTTP che un layer web (Spring Boot, Jakarta EE, o proxy verso Flask) può esporre **senza modificare** la logica in `MatchingService`, `ExchangeService` e `ReviewService`: lo stato resta `SkillSwapState` caricato/salvato tramite `Storage` (es. `FileStorage` su `data/`).

Convenzioni: JSON, `Content-Type: application/json`, identificatori stringa come nel dominio (`S1`, `O1`, …). Gli errori di dominio possono mappare su `400` / `404` / `409` con corpo `{ "error": "messaggio" }`.

---

## Studenti e anagrafica

| Metodo | Path | Descrizione |
|--------|------|-------------|
| `GET` | `/api/students` | Elenco studenti (id, nome, classe, email, `ratingAvg`, `ratingCount`). |
| `GET` | `/api/students/{id}` | Dettaglio singolo studente. |
| `GET` | `/api/students/{id}/reviews` | Recensioni ricevute (reviewee = id), come `ReviewService.getReviewsForStudent`. |

## Skill, offerte, richieste

| Metodo | Path | Descrizione |
|--------|------|-------------|
| `GET` | `/api/skills` | Elenco skill. |
| `GET` | `/api/offers` | Elenco offerte (filtri opzionali: `studentId`, `active`). |
| `GET` | `/api/requests` | Elenco richieste (filtro opzionale: `studentId`). |

*Nota:* creazione da console o form web può esporre `POST` che validano input e aggiornano `SkillSwapState` + `storage.save(state)`.

## Matching (read-only sulla stessa logica Java)

| Metodo | Path | Descrizione |
|--------|------|-------------|
| `GET` | `/api/students/{id}/matches/one-way` | `MatchingService.findOneWayMatches(id)` → lista `{ offerId, requestId, score, reason }[]` ordinata per score decrescente. |
| `GET` | `/api/students/{id}/matches/swap` | `MatchingService.findSwapMatches(id)` → stesso formato `MatchResult`. |

Risposta esempio `GET /api/students/S1/matches/one-way`:

```json
[
  {
    "offerId": "O1",
    "requestId": "R1",
    "score": 6,
    "reason": "skill identica (+3), livello sufficiente (+2), stessa classe (+1)"
  }
]
```

## Exchange

| Metodo | Path | Body | Descrizione |
|--------|------|------|-------------|
| `POST` | `/api/exchanges` | `{ "exchangeId": "E1", "offerId": "O1", "requestId": "R1" }` | `ExchangeService.propose` |
| `PUT` | `/api/exchanges/{id}/accept` | — | `accept` |
| `PUT` | `/api/exchanges/{id}/complete` | — | `complete` |
| `PUT` | `/api/exchanges/{id}/cancel` | — | `cancel` |
| `GET` | `/api/exchanges` | — | Tutti gli scambi (o filtro `studentId` partecipante). |
| `GET` | `/api/exchanges/{id}` | — | Dettaglio singolo scambio. |

## Recensioni

| Metodo | Path | Body | Descrizione |
|--------|------|------|-------------|
| `POST` | `/api/reviews` | `{ "reviewId": "V1", "exchangeId": "E1", "reviewerStudentId": "S1", "stars": 5, "comment": "..." }` | `ReviewService.addReview` |

## Classifica

| Metodo | Path | Descrizione |
|--------|------|-------------|
| `GET` | `/api/ranking` | Studenti con almeno una recensione, ordinati per `ratingAvg` decrescente (stessa logica di `ConsoleReportPrinter.printLeaderboard`). |

---

## Allineamento con la cartella `data/`

I CSV prodotti da `FileStorage` sono la fonte condivisa descritta in `docs/Web_integration.md` (prototipo Flask che legge gli stessi file). In produzione un solo processo dovrebbe scrivere i file alla volta, oppure si centralizza tutto nel backend HTTP (Java o Python) con un unico writer.

## Prossimi passi

1. Aggiungere modulo web (es. `spring-boot-starter-web`) e controller che delegano ai service esistenti.
2. Oppure implementare la cartella `web/` in Python seguendo le fasi W1–W7 del documento di integrazione, replicando in lettura i campi dei CSV.
