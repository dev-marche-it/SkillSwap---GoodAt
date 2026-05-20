# SkillSwap School — Documentazione dettagliata del funzionamento

> Questo documento descrive in dettaglio il funzionamento del progetto SkillSwap School, basandosi sulla guida principale `GUIDELINE.md` e sullo stato attuale del repository.

## 1. Obiettivo del progetto

SkillSwap School è un prototipo di piattaforma Java per lo scambio di competenze tra studenti. Lo scopo è consentire a uno studente di:
- offrire una competenza che conosce,
- richiedere una competenza che desidera apprendere,
- trovare un match con un altro studente,
- aprire uno scambio (`Exchange`),
- completare lo scambio e lasciare una recensione.

L'architettura è pensata per separare domìnio, logica applicativa e persistenza, con un ulteriore livello di presentazione per la console.

## 2. Struttura del progetto

La struttura del repository è organizzata in modo coerente con un progetto Maven:

- `pom.xml`: configurazione Maven e dipendenze (tra cui Lombok).
- `src/main/java/it/skillswap/domain`: classi del dominio e modelli dei dati.
- `src/main/java/it/skillswap/service`: servizi che implementano la logica di business.
- `src/main/java/it/skillswap/storage`: storage per la persistenza dei dati.
- `src/main/java/it/skillswap/app`: entry point e controller applicativo.
- `test/`: test unitari delle funzionalità principali.

La documentazione di approfondimento è raccolta in `docs/`, con `GUIDELINE.md` come base di sviluppo.

## 3. Modello del dominio

Il cuore dell'applicazione è il modello di dominio. Le entità principali sono:

- `Student`: rappresenta uno studente del sistema.
- `Skill`: rappresenta una competenza.
- `Offer`: rappresenta un'offerta di competenza pubblicata da uno studente.
- `Request`: rappresenta una richiesta di competenza da parte di uno studente.
- `Exchange`: rappresenta uno scambio tra un'offerta e una richiesta.
- `Review`: rappresenta una recensione post-scambio.
- `SkillSwapState`: aggrega lo stato dell'applicazione in memoria.

### 3.1 Relazioni tra entità

- Uno `Student` può avere più `Offer` e più `Request`.
- Un `Offer` è collegato a un `Student` e a una `Skill`.
- Una `Request` è collegata a un `Student` e a una `Skill`.
- Un `Exchange` associa un `Offer` e una `Request`.
- Una `Review` è collegata a un `Exchange`, un recensore e un recensito.

### 3.2 Stati di `Exchange`

Gli scambi seguono un ciclo di vita definito:

- `PROPOSED`: lo scambio è stato proposto.
- `ACCEPTED`: lo scambio è stato accettato.
- `COMPLETED`: lo scambio è stato completato.
- `CANCELLED`: lo scambio è stato annullato.

Le transizioni valide sono:

- `propose()` → `PROPOSED`
- `accept()` da `PROPOSED` a `ACCEPTED`
- `complete()` da `ACCEPTED` a `COMPLETED`
- `cancel()` da `PROPOSED` a `CANCELLED`

Qualsiasi altra transizione è disabilitata e gestita con un errore.

## 4. Funzionamento della persistenza

Lo stato dell'applicazione può essere caricato e salvato tramite due implementazioni di `Storage`:

- `InMemoryStorage`: mantiene tutti i dati in memoria. Utile per test e per prototipi senza file.
- `FileStorage`: legge e scrive dati in file CSV nella cartella `data/`.

### 4.1 Struttura CSV

I file CSV usano come separatore il punto e virgola (`;`) e contengono le seguenti entità:

- `students.csv`
- `skills.csv`
- `offers.csv`
- `requests.csv`
- `exchanges.csv`
- `reviews.csv`

L'ordine di caricamento è critico, perché le entità successive fanno riferimento a quelle già lette.

### 4.2 Ordine di caricamento in `FileStorage`

1. `students.csv`
2. `skills.csv`
3. `offers.csv` (associazioni con studenti e skill)
4. `requests.csv` (associazioni con studenti e skill)
5. `exchanges.csv` (associazioni con offer e request)
6. `reviews.csv` (associazioni con exchange e studenti)

### 4.3 Scrittura atomica

`FileStorage.save()` deve scrivere ogni file su un file temporaneo (`*.tmp`) prima di rinominarlo nel file finale, in modo da evitare corruzione dei dati in caso di crash durante la scrittura.

## 5. Logica applicativa e servizi

I servizi sono le classi che implementano la logica di business e sono progettati per essere testabili e indipendenti dalla UI.

### 5.1 `MatchingService`

`MatchingService` è responsabile della ricerca di possibili corrispondenze tra studenti.

I metodi principali sono:

- `findOneWayMatches(String studentId)`: trova tutte le offerte altrui che soddisfano le richieste dello studente.
- `findSwapMatches(String studentId)`: trova match reciproci in cui due studenti si scambiano competenze.

#### Punteggio dei match

Il sistema assegna un punteggio ai match secondo queste regole:

- Skill identica: +3 punti.
- Livello sufficiente: +2 punti.
- Stessa classe scolastica: +1 punto.

I match sono ordinati per score decrescente.

#### Regole principali

- Uno studente non può fare match con sé stesso.
- Solo offerte con `active = true` partecipano al matching.

### 5.2 `ExchangeService`

`ExchangeService` gestisce il ciclo di vita degli scambi.

Metodi chiave:

- `propose(String offerId, String requestId)`: crea un `Exchange` in stato `PROPOSED`.
- `accept(String exchangeId)`: porta lo scambio da `PROPOSED` a `ACCEPTED`.
- `complete(String exchangeId)`: completa lo scambio e imposta `closedAt`.
- `cancel(String exchangeId)`: annulla lo scambio quando è ancora proposto.

#### Regole operative

- `propose()` fallisce se l'offerta è inattiva.
- `closeAt` è impostato solo quando lo scambio diventa `COMPLETED` o `CANCELLED`.
- Uno scambio già chiuso non può essere riaperto.
- Le transizioni non valide interrompono il flusso con un'eccezione.

### 5.3 `ReviewService`

`ReviewService` gestisce le recensioni che uno studente rilascia dopo un `Exchange` completato.

Metodo principale:

- `addReview(String exchangeId, String reviewerId, int stars, String comment)`

#### Requisiti delle recensioni

- È possibile recensire solo se lo scambio è `COMPLETED`.
- Ogni studente può lasciare una sola recensione per scambio.
- Le stelle devono essere un valore intero tra 1 e 5.
- Dopo la recensione, il `ratingAvg` e il `ratingCount` dello studente recensito sono aggiornati.

#### Aggiornamento del rating

La formula utilizzata è:

```text
newRatingAvg = ((ratingAvg * ratingCount) + newStars) / (ratingCount + 1)
ratingCount++
```

Questo preserva la media ponderata delle recensioni precedenti.

## 6. Presentazione e console

L'app è pensata per una interfaccia a linea di comando. La UI minimale offre comandi per:

- creare studenti,
- aggiungere offerte,
- aggiungere richieste,
- elencare offerte e richieste,
- avviare il matching,
- aprire, accettare e chiudere scambi,
- pubblicare recensioni.

### 6.1 `AppController` e `Main`

`Main.java` funge da punto di ingresso. Deve limitarsi a:

- iniziare lo storage,
- caricare lo stato,
- istanziare `AppController`.

`AppController` gestisce il loop della CLI, legge i comandi dell'utente e chiama i servizi appropriati.

### 6.2 `ConsoleReportPrinter`

La classe `ConsoleReportPrinter` formatta l'output in modo leggibile, usando `StringBuilder` per generare:

- profili studenti,
- liste di match,
- dettagli degli scambi,
- leaderboard dei migliori rating.

L'obiettivo è mantenere la presentazione separata dalla logica di business.

## 7. Flusso operativo completo

Ecco il tipico ciclo di utilizzo dell'app:

1. Lo studente viene creato e salvato in `students.csv`.
2. Lo studente pubblica un'offerta e/o una richiesta.
3. `MatchingService` analizza le richieste e le offerte attive.
4. Se esiste un buon match, si propone un `Exchange`.
5. Lo scambio viene accettato dall'altro studente.
6. Lo scambio viene completato e viene impostata la data di chiusura.
7. Uno studente lascia una recensione.
8. Il rating dello studente recensito viene aggiornato.

## 8. Dettaglio del flusso dati

### 8.1 Creazione di un'offerta

Quando uno studente crea un `Offer`:
- l'offerta è collegata all'ID dello studente e all'ID della skill;
- se `active = false`, non potrà essere usata per il matching;
- viene salvata in memoria e, a richiesta, persistita su CSV.

### 8.2 Creazione di una richiesta

Una `Request` contiene:
- l'ID dello studente che cerca la skill,
- l'ID della skill desiderata,
- il livello minimo richiesto,
- eventuali note contestuali.

### 8.3 Generazione dei match

`MatchingService` confronta:
- i `Request` di uno studente con le `Offer` attive altrui,
- i possibili match reciproci con lo stesso ordine di competenze scambiate.

In `findSwapMatches`, lo scambio è valido quando:
- lo studente A offre la skill cercata da B,
- lo studente B offre la skill cercata da A.

### 8.4 Apertura di uno scambio

Nel momento in cui `propose()` viene chiamato:
- viene creato un nuovo `Exchange` in stato `PROPOSED`;
- il sistema memorizza `createdAt` e lascia `closedAt` vuoto;
- l'offerta coinvolta deve essere `active`.

### 8.5 Completamento o annullamento dello scambio

- Se lo scambio è accettato, `accept()` porta lo stato a `ACCEPTED`.
- Se viene concluso con successo, `complete()` imposta `COMPLETED` e `closedAt`.
- Se viene annullato prima dell'accettazione, `cancel()` imposta `CANCELLED` e `closedAt`.

## 9. Validazioni e regole business

Il progetto applica diverse regole chiave che garantiscono coerenza e integrità:

- Uno studente non può trovarsi in match con sé stesso.
- Offerte inattive non partecipano al matching.
- Solo scambi in stato `PROPOSED` possono diventare `ACCEPTED` o `CANCELLED`.
- Solo scambi `ACCEPTED` possono diventare `COMPLETED`.
- Le recensioni sono permesse solo su scambi `COMPLETED`.
- Ogni recensione deve avere stelle comprese tra 1 e 5.
- Ogni studente può lasciare una sola recensione per scambio.

## 10. Testing e qualità

Il progetto è pensato per essere coperto da test unitari.

### 10.1 Aree testate

- `MatchingService`: logica dei match e ordinamento dei risultati.
- `ExchangeService`: transizioni di stato valide e non valide.
- `ReviewService`: aggiunta recensioni e aggiornamento del rating.
- `FileStorage`: caricamento e salvataggio corretto dei CSV.
- `Validator`: validazione di stelle, stato di scambio e dati richiesti.

### 10.2 Strategie di test

- Usare `InMemoryStorage` per isolare la logica dalla persistenza.
- Testare sia i percorsi positivi che i percorsi di errore.
- Mantenere ogni test piccolo, leggibile e con un solo `assert` principale.
- Coprire i bordi: offerta inattiva, recensione duplicata, stato non valido.

## 11. Evoluzione futura

Il progetto è già predisposto per evoluzioni ordinarie:

- transizione verso `DbStorage` per persistere su database SQL;
- introduzione di una web app separata che legge gli stessi CSV;
- implementazione di un front-end grafico a partire dai servizi già esistenti;
- integrazione di un sistema di login e permessi;
- refactoring verso un'architettura a microservizi quando il dominio crescerà.

## 12. Come leggere questo progetto

Per esporre al meglio il progetto, concentra l'attenzione su:

1. Architettura a strati: dominio, servizi, storage, presentazione.
2. Ciclo di vita completo di un `Exchange`.
3. Ruolo dei match e del punteggio nel trovare la migliore corrispondenza.
4. Gestione sicura dei CSV con caricamento ordinato e salvataggio atomico.
5. Separazione tra logica e output: `ConsoleReportPrinter` non esegue business logic.
6. Test come documentazione viva della funzionalità.

## 13. Sommario operativo

Questa è la sequenza concreta di esecuzione in un uso reale:

- L'utente accende l'app e `Main` carica lo stato con `Storage`.
- Viene avviata la CLI gestita da `AppController`.
- L'utente crea studenti, offerte, richieste.
- `MatchingService` calcola i risultati e spiega il punteggio.
- L'utente propone un `Exchange` per un match valido.
- L'altro utente accetta e completa lo scambio.
- Alla fine, viene creata una `Review` e il rating dello studente recensito viene aggiornato.
- Lo stato viene salvato su disco.

---


*Il gruppo sta sviluppando una integrazione WEBAPP reperibile anche tramite Repository Github*


