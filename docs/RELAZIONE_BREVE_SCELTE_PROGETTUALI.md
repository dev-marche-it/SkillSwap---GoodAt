# SkillSwap GoodAt - Relazione breve sulle scelte progettuali (versione estesa)

Questa relazione accompagna il codice e chiarisce le motivazioni tecniche dietro le scelte principali.
Non e un manuale d'uso: e un documento di sintesi progettuale che spiega come e perche il sistema e stato costruito in questo modo.

## 1) Obiettivo progettuale e criteri guida

Fin dall'inizio, il progetto e stato impostato con tre obiettivi:
1. modellare correttamente il dominio dello scambio competenze;
2. separare la logica di business dalla UI;
3. mantenere il sistema estendibile (da CLI a Web) senza duplicare regole.

I criteri guida sono stati:
- chiarezza didattica;
- robustezza funzionale;
- testabilita;
- evolvibilita progressiva.

## 2) Separazione dominio, servizi e persistenza

### Dominio (`it.skillswap.domain`)
Il dominio contiene le entita chiave (`Student`, `Skill`, `Offer`, `Request`, `Exchange`, `Review`) e lo stato aggregato (`SkillSwapState`).
Queste classi rappresentano il problema reale, quindi restano indipendenti da HTTP, UI e dettagli di storage.

### Servizi (`it.skillswap.service`)
I servizi applicano le regole:
- `MatchingService`: costruzione abbinamenti;
- `ExchangeService`: gestione stato scambi;
- `ReviewService`: validazione e salvataggio recensioni con update reputazione.

La UI (console o browser) non implementa queste regole: invoca i servizi.

### Persistenza (`it.skillswap.storage`)
La persistenza e astratta tramite `Storage`.
Sono presenti due implementazioni:
- `FileStorage`: persistenza reale su CSV;
- `InMemoryStorage`: alternativa in memoria utile per test/simulazioni.

Questa scelta riduce l'accoppiamento e semplifica il testing.

## 3) Scelte significative per i servizi principali

### 3.1 MatchingService
Scelta chiave: algoritmo a punteggio esplicito e leggibile.

Invece di un ranking opaco, ogni risultato e spiegabile con criteri semplici:
- skill identica (+3),
- livello sufficiente (+2),
- stessa classe (+1).

Motivazione:
- facilita debugging e test;
- migliora trasparenza lato utente;
- consente evoluzioni incrementali (aggiunta di nuovi criteri).

### 3.2 ExchangeService
Scelta chiave: macchina a stati esplicita.

Lo scambio non e un record "libero", ma un processo con transizioni controllate:
- `PROPOSED -> ACCEPTED -> COMPLETED`
- `PROPOSED -> CANCELLED`

Motivazione:
- previene stati incoerenti;
- rende il comportamento prevedibile;
- abilita autorizzazioni contestuali (azioni disponibili in base allo stato).

Nel tempo e stata aggiunta anche protezione anti-duplicato per evitare scambi attivi multipli sulla stessa coppia offer/request.

### 3.3 ReviewService
Scelta chiave: recensione vincolata alla chiusura valida dello scambio.

Regole adottate:
- review solo su scambio `COMPLETED`,
- reviewer deve essere partecipante,
- una recensione per coppia `(exchange, reviewer)`,
- stelle in range 1..5.

Motivazione:
- tutela integrita del ranking;
- evita feedback non contestualizzati;
- garantisce legame tra esperienza reale e reputazione.

## 4) Caricamento e salvataggio CSV

### 4.1 Implementazioni usate
Sono usati entrambi gli storage:
- `FileStorage` in runtime applicativo;
- `InMemoryStorage` in scenari di test o stato effimero.

### 4.2 Strategia di caricamento (`FileStorage.load`)
Il caricamento avviene in ordine dipendente:
1. studenti
2. skill
3. offerte
4. richieste
5. scambi
6. recensioni

Le entita "alte" vengono create solo dopo aver caricato quelle di base.
Questo permette di ricostruire correttamente i collegamenti tra oggetti (es. `Offer` punta allo `Student` e alla `Skill` corretti).

### 4.3 Strategia di salvataggio (`FileStorage.save`)
Il salvataggio serializza lo snapshot corrente (`SkillSwapState`) nei CSV di dominio.
La centralizzazione in un unico storage riduce differenze tra flusso CLI e flusso Web.

## 5) Evoluzione del progetto e impatto sulle scelte

Il progetto e partito come applicazione console, poi esteso a backend web REST con frontend statico.
Questa evoluzione e stata possibile perche:
- la logica era gia nei service Java;
- il layer web si limita a orchestrare chiamate e conversione DTO;
- la persistenza era gia un modulo separato.

Nei cicli di hardening sono state rinforzate:
- validazioni payload,
- coerenza azioni per ruolo,
- gestione errori API,
- aggiornamenti UI post-azione,
- qualita UX (layout, accessibilita visiva, feedback utente).

## 6) Motivazioni globali delle scelte

Le decisioni adottate derivano da un equilibrio tra semplicita e completezza:
- **Semplicita**: CSV e Java vanilla per mantenere il progetto leggibile in ambito didattico.
- **Completezza**: workflow reale con stati, review, ranking, API e frontend.
- **Modularita**: separazione chiara tra model/service/storage/presentation.
- **Manutenibilita**: cambi UI non impattano le regole di business.
- **Scalabilita concettuale**: passaggio futuro a DB e auth avanzata senza rifondare il dominio.

## 7) Conclusione

SkillSwap GoodAt e stato progettato come sistema coerente end-to-end:
- dominio espresso in modo chiaro;
- business rules isolate e testabili;
- persistenza astratta con implementazione concreta su CSV;
- doppia superficie d'uso (CLI e Web) che riusa lo stesso cuore applicativo.

Il risultato e un progetto completo, spiegabile in sede di presentazione tecnica e pronto per ulteriori estensioni senza riscrittura strutturale.

