#  Major Fixes

## 🔴 Problemi Critici

### 1. AppController non aggiornato per enum

**Problema:**  
`AppController` legge `String` dalla CLI invece di usare enum.

**File:**  

src/main/java/it/skillswap/app/AppController.java

**Errori di compilazione:**
- Linea 126 → `Skill(id, name, category)`  
  → `String` non convertibile a `SkillCategory`
- Linea 150 → `Offer(id, student, skill, level, note)`  
  → `String` non convertibile a `SkillLevel`
- Linea 182 → `Request(id, student, skill, minLevel, note)`  
  → `String` non convertibile a `SkillLevel`

**Causa:**  
Gli enum sono stati creati in Fase 2.2 ma il controller non è stato aggiornato.

**Impatto:**  
🔴 **CRITICO — Il progetto non compila**

---

### 2. FileStorage non supporta enum nel CSV

**Problema:**  
`FileStorage` legge `String` dal CSV ma il dominio richiede enum.

**File:** 

src/main/java/it/skillswap/storage/FileStorage.java


**Errori di compilazione:**
- Linea 41 → `Skill(p[0], p[1], p[2])`  
  → `p[2]` (`String`) non convertibile a `SkillCategory`
- Linea 52 → `Offer(p[0], st, sk, p[3], p[4])`  
  → `p[3]` non convertibile a `SkillLevel`
- Linea 64 → `Request(p[0], st, sk, p[3], p[4])`  
  → `p[3]` non convertibile a `SkillLevel`

**Causa:**  
Il CSV contiene stringhe, ma il dominio ora usa enum.

**Impatto:**  
🔴 **CRITICO — Storage non funziona**

---

### 3. Fase 2.2 non completata (integrazione enum)

**Stato attuale:**
- ✅ `SkillLevel.java` creato
- ✅ `SkillCategory.java` creato
- ❌ Storage non aggiornato
- ❌ AppController non aggiornato
- ❌ CSV non compatibile con enum

**Impatto:**  
🔴 **BLOCCO — Va completata prima di proseguire**

---

## 🟡 Problemi di Qualità del Codice

### 4. Campi non `final` nei Service

**Problema:**  
Campi mutabili non necessari.

**File:**

- src/main/java/it/skillswap/service/ExchangeService.java (linea 12)

- src/main/java/it/skillswap/service/ReviewService.java (linea 14)


**Errore lint:**
```java
private SkillSwapState state; // può essere final
```


## 5. Warning nei test

**Problemi:**

__MatchingServiceTest__

Linea 82:

assertTrue(results.size() > 0);

→ usare:
```
assertFalse(results.isEmpty());
```

__*ExchangeServiceTest*__
```assertThrows()``` non utilizzato (3 volte)
Variabili inutilizzate (```exchange```, ```offerActiveBefore```)
__*ReviewServiceTest*__

```assertThrows()``` non utilizzato (3 volte)

__*ValidatorTest*__
```assertThrows()``` non utilizzato (4 volte)

__*Impatto:*__
🟡 BASSO — Funziona ma non conforme


## 6. Classe Student senza campi final

*Problema:* 

Campi immutabili non dichiarati final.

File:

src/main/java/it/skillswap/domain/Student.java

*Campi coinvolti:*

```private String studentId;
private String name;
private String className;
private String email;
```

Impatto:
🟠 BASSO — Miglioramento strutturale

Piano di Correzione

| Priorità| Attività |File |
|---|---|---|
| 🔴CRITICA | 	Integrare enum nello storage |	FileStorage.java	
|🔴CRITICA	|Integrare enum nel controller|	AppController.java	|
|🟡 MEDIA|	Rendere final i campi nei service |	service/*.java|	
|🟡 MEDIA|	Rendere final i campi in Student|	Student.java|	
|🟡 MEDIA|	Pulire warning nei test|	test/*.java	



__*Documento di correzione per lo sviluppo del progetto. Documentazione momentaneo*__