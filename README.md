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
- **Eseguire Main**: `mvn exec:java -Dexec.mainClass=it.skillswap.app.Main`

Assicurati di avere Maven installato e visibile nel `PATH` (`mvn -v` deve funzionare).

## Lombok

Nel `pom.xml` è già presente la dipendenza Lombok (`org.projectlombok:lombok`, scope `provided`).
Per farla funzionare correttamente nell'IDE:

- Installa il **plugin Lombok** (per IntelliJ IDEA / Eclipse).
- Abilita l'**annotation processing** nelle impostazioni del progetto.

Da qui in avanti puoi usare annotazioni come `@Getter`, `@Setter`, `@Data`, `@Builder` sulle classi di dominio
per eliminare boilerplate (getter/setter, costruttori, ecc.).

## TODO

- [x] Installare Maven e aggiungere dipendenza Lombok nel `pom.xml`
- [x] Migrare struttura cartelle a `src/main/java/it/skillswap/`
- [X] Verificare il funzionamento di Lombok e Maven con `mvn clean compile`
- [ ] Minor fixes e inizio test effettivo
- [ ] Aggiustamento struttura del progetto per garantire solidità
- [ ] Eliminazione codice superfluo per garantire "Clean code" Standard
- [ ] Inizio sviluppo progettuale dell'interfaccia WEB ( CON ALTRE BRANCH E NUOVO CODICE IN CARTELLA WEB)

