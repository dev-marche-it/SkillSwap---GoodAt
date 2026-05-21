# SkillSwap---GoodAt
Una piattaforma per lo scambio di competenze tra studenti della stessa scuola. Con un sistema di ranking e back-end completamente in Java.

## Struttura del progetto (Maven)

Il progetto è ora organizzato come classico progetto Maven:

- `pom.xml`: configurazione Maven (Java 17, dipendenze, plugin).
- `src/main/java/it/skillswap/app/Main.java`: entrypoint dell'applicazione da riga di comando.
- `src/main/java/it/skillswap/domain/...`: modello di dominio (`Student`, `Skill`, `Offer`, `Request`, `Exchange`, `Review`, `SkillSwapState`, ecc.).
- `src/main/java/it/skillswap/service/...`: logica di matching (`MatchingService`, `MatchResult`, ecc.).
- `src/main/java/it/skillswap/storage/...`: persistenza in memoria (`Storage`, `InMemoryStorage`).

## Maven

Per usare il progetto con Maven:

- **Compilare**: `mvn clean compile`
- **CLI console**: `mvn exec:java` (main `it.skillswap.app.Main`)
- **Web (Spring Boot)**: `mvn spring-boot:run` → browser su [http://localhost:8080](http://localhost:8080)

Assicurati di avere Maven installato e visibile nel `PATH` (`mvn -v` deve funzionare).

### Interfaccia web (backend Java)

L’app web riusa `MatchingService`, `ExchangeService`, `ReviewService` e `FileStorage` senza duplicare la logica.

- **Registrazione** e **login** con email + password (min. 6 caratteri)
- Account demo: `alessandro@scuola.it` / `riccardo@scuola.it` — password `SkillSwap123`
- API REST sotto `/api/...` (vedi `web/README_WEB.md`)
- Frontend statico in `src/main/resources/static/`

**Non avviare contemporaneamente** la CLI e il server web: entrambi scrivono gli stessi CSV.

## Lombok

Nel `pom.xml` è già presente la dipendenza Lombok (`org.projectlombok:lombok`, scope `provided`).
Per farla funzionare correttamente nell'IDE:

- Installa il **plugin Lombok** (per IntelliJ IDEA / Eclipse).
- Abilita l'**annotation processing** nelle impostazioni del progetto.

Da qui in avanti puoi usare annotazioni come `@Getter`, `@Setter`, `@Data`, `@Builder` sulle classi di dominio
per eliminare boilerplate (getter/setter, costruttori, ecc.).


 | __UPDATED:__ La directory __*docs*__ contiene tutta la documentazione del progetto. |
 | --- |
| **Studio completo** | [`docs/GUIDA_STUDIO_COMPLETA.md`](docs/GUIDA_STUDIO_COMPLETA.md) — manuale didattico per imparare tutto il progetto |
| **Reference tecnico** | [`docs/DOCUMENTAZIONE_COMPLETA.md`](docs/DOCUMENTAZIONE_COMPLETA.md) — architettura e implementazione |
## TODO

- [x] Installare Maven e aggiungere dipendenza Lombok nel `pom.xml`
- [x] Migrare struttura cartelle a `src/main/java/it/skillswap/`
- [X] Verificare il funzionamento di Lombok e Maven con `mvn clean compile`
- [X] Minor fixes e inizio test effettivo
- [X] Aggiustamento struttura del progetto per garantire solidità
- [x] Eliminazione codice superfluo per garantire "Clean code" Standard
- [x] Interfaccia WEB con backend Java (Spring Boot REST + pagina statica)
- [ ] Ottimizzare la pagina e il funzionamento Backend
- [ ] Migliorare il design dell'intera applicazione(Logo, Palette ecc...) Branding
- [ ] Verificare la solidità del progetto
- [ ] Sviluppare la presentazione per l'esposizione





