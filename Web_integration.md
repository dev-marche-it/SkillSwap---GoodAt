# SkillSwap School — Guida all'integrazione Web App

> Documento tecnico completo per la trasformazione del prototipo console  
> in una web application accessibile via browser sulla rete locale.  
> Da leggere integralmente prima di scrivere una singola riga di codice web.

---

## Indice

1. [Visione generale](#1-visione-generale)
2. [Stack tecnologico](#2-stack-tecnologico)
3. [Architettura finale](#3-architettura-finale)
4. [Struttura della repo](#4-struttura-della-repo)
5. [Fase W1 — Setup Flask](#5-fase-w1--setup-flask)
6. [Fase W2 — Lettura dei CSV da Flask](#6-fase-w2--lettura-dei-csv-da-flask)
7. [Fase W3 — Route e API](#7-fase-w3--route-e-api)
8. [Fase W4 — Sistema di login](#8-fase-w4--sistema-di-login)
9. [Fase W5 — Pagine HTML e CSS](#9-fase-w5--pagine-html-e-css)
10. [Fase W6 — Sistema di ranking](#10-fase-w6--sistema-di-ranking)
11. [Fase W7 — Accesso sulla rete locale](#11-fase-w7--accesso-sulla-rete-locale)
12. [Fase W8 — Integrazione futura con Spring Boot](#12-fase-w8--integrazione-futura-con-spring-boot)
13. [Flusso dati completo](#13-flusso-dati-completo)
14. [Branch strategy e commit convention](#14-branch-strategy-e-commit-convention)

---

## 1. Visione generale

Il progetto Java ha prodotto:
- **Classi di dominio** (`Student`, `Skill`, `Offer`, `Request`, `Exchange`, `Review`)
- **Service** (`MatchingService`, `ExchangeService`, `ReviewService`)
- **Persistenza CSV** nella cartella `data/`

La web app che costruirai sopra leggerà **gli stessi file CSV** prodotti dal backend Java,  
li servirà tramite **Flask (Python)** e li mostrerà tramite **HTML/CSS/JS** nel browser.

### Cosa vedrà l'utente nel browser

```
┌─────────────────────────────────────────┐
│  SkillSwap School                [logout]│
├─────────────────────────────────────────┤
│  Benvenuto, Anna Rossi  ★ 4.6           │
│                                         │
│  [I tuoi match]  [I tuoi scambi]        │
│  [Classifica]    [Profilo]              │
└─────────────────────────────────────────┘
```

---

## 2. Stack tecnologico

| Layer | Tecnologia | Ruolo |
|---|---|---|
| **Frontend** | HTML5 + CSS3 + JavaScript | Interfaccia utente nel browser |
| **Backend web** | Python 3 + Flask | Server HTTP, routing, sessioni |
| **Logica dati** | Python (lettura CSV) | Replica della logica Java in Python |
| **Persistenza** | File CSV in `data/` | Fonte dati condivisa con Java |
| **Rete** | Flask dev server (LAN) | Accesso da qualsiasi device sulla rete locale |

### Perché Flask e non Spring Boot

Flask è più rapido da avviare per un prototipo web e non richiede di modificare il codice Java.  
I due sistemi condividono solo i file CSV. Spring Boot è l'evoluzione naturale descritta nella Fase W8.

### Prerequisiti da installare

```bash
# Verifica Python
python3 --version   # deve essere >= 3.9

# Installa Flask
pip3 install flask

# Verifica
python3 -c "import flask; print(flask.__version__)"
```

---

## 3. Architettura finale

```
┌──────────────────────────────────────────────────────┐
│                    BROWSER                           │
│         HTML / CSS / JavaScript                      │
│   form login, dashboard, match, ranking              │
└─────────────────────┬────────────────────────────────┘
                      │ HTTP request/response
┌─────────────────────▼────────────────────────────────┐
│                  FLASK SERVER                        │
│                  web/app.py                          │
│                                                      │
│   Route:  /login  /dashboard  /matches  /ranking     │
│   Sessioni utente (cookie)                           │
│   Lettura e scrittura CSV                            │
└─────────────────────┬────────────────────────────────┘
                      │ legge e scrive
┌─────────────────────▼────────────────────────────────┐
│                FILE CSV  (data/)                     │
│   students.csv  offers.csv  requests.csv             │
│   exchanges.csv  reviews.csv  skills.csv             │
└──────────────────────────────────────────────────────┘
                      │ stessi file
┌─────────────────────▼────────────────────────────────┐
│              BACKEND JAVA (console)                  │
│   Main.java → Service → Storage → FileStorage        │
└──────────────────────────────────────────────────────┘
```

> Java e Flask **non girano contemporaneamente**.  
> Java produce e modifica i CSV. Flask li legge e li mostra nel browser.  
> In futuro, Spring Boot sostituirà Flask come backend HTTP.

---

## 4. Struttura della repo

Aggiungi la cartella `web/` alla root esistente:

```
skillswap-school/
├── src/                          ← codice Java (invariato)
├── data/                         ← CSV condivisi
│   ├── students.csv
│   ├── skills.csv
│   ├── offers.csv
│   ├── requests.csv
│   ├── exchanges.csv
│   └── reviews.csv
│
├── web/                          ← NUOVO — tutto il codice web
│   ├── app.py                    ← server Flask principale
│   ├── config.py                 ← configurazione (path CSV, secret key)
│   ├── csv_reader.py             ← lettura e parsing dei CSV
│   ├── matching.py               ← logica di matching in Python
│   ├── requirements.txt          ← dipendenze Python
│   │
│   ├── templates/                ← pagine HTML (Jinja2)
│   │   ├── base.html             ← layout comune a tutte le pagine
│   │   ├── login.html
│   │   ├── dashboard.html
│   │   ├── matches.html
│   │   ├── exchanges.html
│   │   ├── ranking.html
│   │   └── profile.html
│   │
│   └── static/                   ← file statici
│       ├── css/
│       │   └── style.css
│       └── js/
│           └── main.js
│
├── pom.xml
├── GUIDE.md
├── PHASE2_PROFESSIONALIZATION.md
├── WEB_INTEGRATION.md            ← questo documento
└── README.md
```

---

## 5. Fase W1 — Setup Flask

### Obiettivo
Creare il server Flask minimale, verificare che risponda nel browser.

### Step 1 — Crea `web/requirements.txt`

```
flask>=3.0.0
```

### Step 2 — Crea `web/config.py`

```python
import os

# Path assoluto alla cartella data/ relativo alla posizione di app.py
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA_DIR = os.path.join(BASE_DIR, "data")

# Chiave segreta per le sessioni Flask (cambiala in produzione)
SECRET_KEY = "skillswap-secret-key-2026"

# Porta del server
PORT = 5000

# Host — 0.0.0.0 rende il server accessibile sulla rete locale
HOST = "0.0.0.0"
```

### Step 3 — Crea `web/app.py` minimale

```python
from flask import Flask
from config import SECRET_KEY, HOST, PORT

app = Flask(__name__)
app.secret_key = SECRET_KEY

@app.route("/")
def index():
    return "SkillSwap School — server attivo"

if __name__ == "__main__":
    app.run(host=HOST, port=PORT, debug=True)
```

### Step 4 — Avvia il server

```bash
cd web/
python3 app.py
```

Apri il browser su `http://localhost:5000` — deve apparire il messaggio di benvenuto.

---

## 6. Fase W2 — Lettura dei CSV da Flask

### Obiettivo
Creare il modulo Python che legge i CSV e ricostruisce gli oggetti di dominio,  
esattamente come fa `FileStorage.java`.

### Crea `web/csv_reader.py`

```python
import csv
import os
from config import DATA_DIR

def read_csv(filename):
    """Legge un file CSV e restituisce una lista di dizionari."""
    path = os.path.join(DATA_DIR, filename)
    if not os.path.exists(path):
        return []
    with open(path, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f, delimiter=";")
        return list(reader)

def load_students():
    """Restituisce dict {student_id: student_dict}"""
    rows = read_csv("students.csv")
    return {r["student_id"]: r for r in rows}

def load_skills():
    """Restituisce dict {skill_id: skill_dict}"""
    rows = read_csv("skills.csv")
    return {r["skill_id"]: r for r in rows}

def load_offers():
    """Restituisce lista di offer con student e skill risolti."""
    students = load_students()
    skills = load_skills()
    offers = []
    for row in read_csv("offers.csv"):
        row["student"] = students.get(row["student_id"], {})
        row["skill"] = skills.get(row["skill_id"], {})
        offers.append(row)
    return offers

def load_requests():
    """Restituisce lista di request con student e skill risolti."""
    students = load_students()
    skills = load_skills()
    requests = []
    for row in read_csv("requests.csv"):
        row["student"] = students.get(row["student_id"], {})
        row["skill"] = skills.get(row["skill_id"], {})
        requests.append(row)
    return requests

def load_exchanges():
    """Restituisce lista di exchange con offer e request risolti."""
    offers = {o["offer_id"]: o for o in load_offers()}
    requests = {r["request_id"]: r for r in load_requests()}
    exchanges = []
    for row in read_csv("exchanges.csv"):
        row["offer"] = offers.get(row["offer_id"], {})
        row["request"] = requests.get(row["request_id"], {})
        exchanges.append(row)
    return exchanges

def load_reviews():
    """Restituisce lista di review con exchange e student risolti."""
    students = load_students()
    exchanges = {e["exchange_id"]: e for e in load_exchanges()}
    reviews = []
    for row in read_csv("reviews.csv"):
        row["exchange"] = exchanges.get(row["exchange_id"], {})
        row["reviewer"] = students.get(row["reviewer_student_id"], {})
        row["reviewee"] = students.get(row["reviewee_student_id"], {})
        reviews.append(row)
    return reviews
```

### Verifica nel terminale

```bash
cd web/
python3 -c "from csv_reader import load_students; print(load_students())"
```

Deve stampare i dati di `students.csv` come dizionario Python.

---

## 7. Fase W3 — Route e API

### Obiettivo
Definire tutte le route Flask che servono le pagine HTML e i dati.

### Mappa delle route

| Metodo | Path | Descrizione |
|---|---|---|
| `GET` | `/` | Redirect a login o dashboard |
| `GET/POST` | `/login` | Form di login |
| `GET` | `/logout` | Termina la sessione |
| `GET` | `/dashboard` | Home dell'utente loggato |
| `GET` | `/matches` | Match disponibili per l'utente |
| `GET` | `/exchanges` | Scambi dell'utente |
| `GET` | `/ranking` | Classifica studenti per rating |
| `GET` | `/profile/<student_id>` | Profilo pubblico di uno studente |

### Aggiorna `web/app.py` con tutte le route

```python
from flask import Flask, render_template, request, redirect, url_for, session
from config import SECRET_KEY, HOST, PORT
from csv_reader import load_students, load_offers, load_requests, load_exchanges, load_reviews
from matching import find_one_way_matches

app = Flask(__name__)
app.secret_key = SECRET_KEY

# ── Autenticazione ──────────────────────────────────────────

@app.route("/")
def index():
    if "student_id" in session:
        return redirect(url_for("dashboard"))
    return redirect(url_for("login"))

@app.route("/login", methods=["GET", "POST"])
def login():
    error = None
    if request.method == "POST":
        email = request.form.get("email", "").strip()
        students = load_students()
        match = next((s for s in students.values() if s["email"] == email), None)
        if match:
            session["student_id"] = match["student_id"]
            session["student_name"] = match["name"]
            return redirect(url_for("dashboard"))
        error = "Email non trovata. Riprova."
    return render_template("login.html", error=error)

@app.route("/logout")
def logout():
    session.clear()
    return redirect(url_for("login"))

# ── Pagine principali ────────────────────────────────────────

@app.route("/dashboard")
def dashboard():
    if "student_id" not in session:
        return redirect(url_for("login"))
    students = load_students()
    student = students.get(session["student_id"])
    return render_template("dashboard.html", student=student)

@app.route("/matches")
def matches():
    if "student_id" not in session:
        return redirect(url_for("login"))
    results = find_one_way_matches(session["student_id"])
    return render_template("matches.html", matches=results)

@app.route("/exchanges")
def exchanges():
    if "student_id" not in session:
        return redirect(url_for("login"))
    student_id = session["student_id"]
    all_exchanges = load_exchanges()
    my_exchanges = [
        e for e in all_exchanges
        if e.get("offer", {}).get("student_id") == student_id
        or e.get("request", {}).get("student_id") == student_id
    ]
    return render_template("exchanges.html", exchanges=my_exchanges)

@app.route("/ranking")
def ranking():
    if "student_id" not in session:
        return redirect(url_for("login"))
    students = list(load_students().values())
    students.sort(key=lambda s: float(s.get("rating_avg", 0)), reverse=True)
    return render_template("ranking.html", students=students)

@app.route("/profile/<student_id>")
def profile(student_id):
    if "student_id" not in session:
        return redirect(url_for("login"))
    students = load_students()
    student = students.get(student_id)
    reviews = [r for r in load_reviews() if r["reviewee_student_id"] == student_id]
    return render_template("profile.html", student=student, reviews=reviews)

if __name__ == "__main__":
    app.run(host=HOST, port=PORT, debug=True)
```

---

## 8. Fase W4 — Sistema di login

### Obiettivo
Permettere agli studenti di autenticarsi usando la propria email, senza password  
(il prototipo non gestisce password — è un sistema scolastico interno).

### Come funziona

```
Studente inserisce email
        ↓
Flask cerca email in students.csv
        ↓
Trovata → crea sessione con student_id e name
        ↓
Redirect a /dashboard
```

### Crea `web/templates/login.html`

```html
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>SkillSwap School — Login</title>
    <link rel="stylesheet" href="{{ url_for('static', filename='css/style.css') }}">
</head>
<body>
    <div class="login-container">
        <h1>SkillSwap School</h1>
        <p>Accedi con la tua email scolastica</p>

        {% if error %}
            <p class="error">{{ error }}</p>
        {% endif %}

        <form method="POST" action="/login">
            <input type="email" name="email" placeholder="la-tua@email.it" required>
            <button type="submit">Accedi</button>
        </form>
    </div>
</body>
</html>
```

### Protezione delle route

Ogni route che richiede autenticazione deve controllare la sessione:

```python
# Pattern da ripetere in ogni route protetta
if "student_id" not in session:
    return redirect(url_for("login"))
```

---

## 9. Fase W5 — Pagine HTML e CSS

### Obiettivo
Creare un'interfaccia coerente, leggibile e responsive per tutte le pagine.

### Crea `web/templates/base.html` — layout comune

```html
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{% block title %}SkillSwap School{% endblock %}</title>
    <link rel="stylesheet" href="{{ url_for('static', filename='css/style.css') }}">
</head>
<body>
    <nav>
        <span class="nav-brand">SkillSwap School</span>
        <div class="nav-links">
            <a href="/dashboard">Dashboard</a>
            <a href="/matches">Match</a>
            <a href="/exchanges">Scambi</a>
            <a href="/ranking">Classifica</a>
            <a href="/logout">Esci</a>
        </div>
    </nav>

    <main>
        {% block content %}{% endblock %}
    </main>

    <script src="{{ url_for('static', filename='js/main.js') }}"></script>
</body>
</html>
```

### Struttura CSS minimale — `web/static/css/style.css`

```css
/* Reset */
* { margin: 0; padding: 0; box-sizing: border-box; }

body {
    font-family: 'Segoe UI', sans-serif;
    background: #f5f7fa;
    color: #333;
}

/* Navbar */
nav {
    background: #2c3e50;
    color: white;
    padding: 1rem 2rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
nav a { color: white; text-decoration: none; margin-left: 1.5rem; }
nav a:hover { text-decoration: underline; }
.nav-brand { font-weight: bold; font-size: 1.2rem; }

/* Main content */
main { max-width: 960px; margin: 2rem auto; padding: 0 1rem; }

/* Card */
.card {
    background: white;
    border-radius: 8px;
    padding: 1.5rem;
    margin-bottom: 1rem;
    box-shadow: 0 2px 6px rgba(0,0,0,0.08);
}

/* Login */
.login-container {
    max-width: 400px;
    margin: 10vh auto;
    text-align: center;
    padding: 2rem;
    background: white;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.login-container input {
    width: 100%;
    padding: 0.75rem;
    margin: 1rem 0;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
}
.login-container button {
    width: 100%;
    padding: 0.75rem;
    background: #2c3e50;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 1rem;
    cursor: pointer;
}

/* Rating stars */
.stars { color: #f39c12; font-size: 1.2rem; }

/* Error message */
.error { color: #e74c3c; margin: 0.5rem 0; }

/* Table ranking */
table { width: 100%; border-collapse: collapse; }
th, td { padding: 0.75rem 1rem; text-align: left; border-bottom: 1px solid #eee; }
th { background: #f0f0f0; font-weight: 600; }
tr:hover { background: #fafafa; }
```

### Crea `web/templates/dashboard.html`

```html
{% extends "base.html" %}
{% block title %}Dashboard{% endblock %}
{% block content %}
<div class="card">
    <h2>Ciao, {{ student.name }} 👋</h2>
    <p>Classe: {{ student.class }}</p>
    <p>Rating: <span class="stars">★</span> {{ student.rating_avg }} ({{ student.rating_count }} recensioni)</p>
</div>

<div class="card">
    <h3>Cosa vuoi fare?</h3>
    <a href="/matches">Trova un match</a> |
    <a href="/exchanges">I tuoi scambi</a> |
    <a href="/ranking">Vedi la classifica</a>
</div>
{% endblock %}
```

### Crea `web/templates/ranking.html`

```html
{% extends "base.html" %}
{% block title %}Classifica{% endblock %}
{% block content %}
<div class="card">
    <h2>Classifica studenti</h2>
    <table>
        <thead>
            <tr><th>#</th><th>Nome</th><th>Classe</th><th>Rating</th><th>Recensioni</th></tr>
        </thead>
        <tbody>
            {% for i, student in students | enumerate(start=1) %}
            <tr>
                <td>{{ i }}</td>
                <td><a href="/profile/{{ student.student_id }}">{{ student.name }}</a></td>
                <td>{{ student.class }}</td>
                <td><span class="stars">★</span> {{ student.rating_avg }}</td>
                <td>{{ student.rating_count }}</td>
            </tr>
            {% endfor %}
        </tbody>
    </table>
</div>
{% endblock %}
```

---

## 10. Fase W6 — Sistema di ranking

### Obiettivo
Mostrare la classifica degli studenti ordinata per `rating_avg` discendente,  
con possibilità di vedere il profilo e le recensioni ricevute.

### Crea `web/matching.py`

```python
from csv_reader import load_offers, load_requests

LEVELS = ["BEGINNER", "INTERMEDIATE", "ADVANCED"]

def is_level_sufficient(offer_level, min_level):
    return LEVELS.index(offer_level) >= LEVELS.index(min_level)

def calculate_score(offer, request):
    score = 0
    reason_parts = []
    if offer["skill_id"] == request["skill_id"]:
        score += 3
        reason_parts.append("skill identica (+3)")
    if is_level_sufficient(offer.get("level", ""), request.get("min_level", "")):
        score += 2
        reason_parts.append("livello sufficiente (+2)")
    if offer.get("student", {}).get("class") == request.get("student", {}).get("class"):
        score += 1
        reason_parts.append("stessa classe (+1)")
    return score, ", ".join(reason_parts)

def find_one_way_matches(student_id):
    offers = load_offers()
    requests = load_requests()
    results = []
    for req in requests:
        if req["student_id"] != student_id:
            continue
        for offer in offers:
            if offer.get("active", "true").lower() != "true":
                continue
            if offer["student_id"] == student_id:
                continue
            if offer["skill_id"] != req["skill_id"]:
                continue
            score, reason = calculate_score(offer, req)
            results.append({
                "offer": offer,
                "request": req,
                "score": score,
                "reason": reason
            })
    results.sort(key=lambda x: x["score"], reverse=True)
    return results
```

### Aggiungi il filtro `enumerate` a Flask

Jinja2 non ha `enumerate` di default. Aggiungilo in `app.py`:

```python
# Aggiungere dopo la creazione dell'app
app.jinja_env.globals.update(enumerate=enumerate)
```

---

## 11. Fase W7 — Accesso sulla rete locale

### Obiettivo
Rendere il server Flask accessibile da qualsiasi dispositivo (smartphone, tablet, PC)  
connesso alla stessa rete Wi-Fi o LAN.

### Come funziona

Il parametro `host="0.0.0.0"` in `app.run()` fa ascoltare Flask su **tutte le interfacce di rete**,  
non solo su `localhost`. Chiunque sulla stessa rete può raggiungere il server.

### Step 1 — Trova l'IP del tuo PC

```bash
# Linux / Mac
hostname -I

# Windows
ipconfig
```

Prendi l'indirizzo IP locale, es. `192.168.1.42`.

### Step 2 — Avvia il server

```bash
cd web/
python3 app.py
```

Il terminale mostrerà:
```
 * Running on http://0.0.0.0:5000
 * Running on http://192.168.1.42:5000
```

### Step 3 — Accesso da altri dispositivi

Da qualsiasi dispositivo sulla stessa rete, aprire il browser e andare su:

```
http://192.168.1.42:5000
```

### Note di sicurezza

> Il server Flask in modalità `debug=True` è pensato **solo per uso locale e sviluppo**.  
> Non esporlo su internet. Per un deploy pubblico serve un server WSGI come Gunicorn + Nginx.

---

## 12. Fase W8 — Integrazione futura con Spring Boot

### Obiettivo
Descrivere il percorso di evoluzione da Flask a Spring Boot come backend HTTP,  
mantenendo invariato il codice Java di dominio e service.

### Cosa cambia

| Componente | Ora (Flask) | Futuro (Spring Boot) |
|---|---|---|
| Server HTTP | Python / Flask | Java / Spring Boot |
| Lettura CSV | `csv_reader.py` | `FileStorage.java` già esistente |
| Logica matching | `matching.py` (Python) | `MatchingService.java` già esistente |
| Template HTML | Jinja2 | Thymeleaf o React |
| Sessioni | Flask session | Spring Security |

### Endpoint REST da esporre con Spring Boot

```
GET    /api/students                     → lista tutti gli studenti
GET    /api/students/{id}                → profilo studente
GET    /api/students/{id}/matches        → one-way matches
GET    /api/students/{id}/swap-matches   → swap matches
GET    /api/students/{id}/exchanges      → scambi dello studente
POST   /api/exchanges/propose            → body: { offerId, requestId }
PUT    /api/exchanges/{id}/accept        → accetta scambio
PUT    /api/exchanges/{id}/complete      → completa scambio
PUT    /api/exchanges/{id}/cancel        → annulla scambio
POST   /api/reviews                      → body: { exchangeId, reviewerId, stars, comment }
GET    /api/ranking                      → classifica studenti
```

### Come aggiungere Spring Boot al `pom.xml`

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

### Esempio di controller Spring Boot

```java
// app/StudentController.java
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final SkillSwapState state;
    private final MatchingService matchingService;

    public StudentController(SkillSwapState state, MatchingService matchingService) {
        this.state = state;
        this.matchingService = matchingService;
    }

    @GetMapping
    public Collection<Student> getAllStudents() {
        return state.getStudents().values();
    }

    @GetMapping("/{id}/matches")
    public List<MatchResult> getMatches(@PathVariable String id) {
        return matchingService.findOneWayMatches(id);
    }
}
```

> Nota: `MatchingService.java` e tutte le classi di dominio **non cambiano**.  
> Si aggiunge solo il layer controller sopra i service esistenti.

---

## 13. Flusso dati completo

```
UTENTE apre il browser
        ↓
GET http://192.168.1.42:5000/login
        ↓
Flask serve login.html
        ↓
UTENTE inserisce email → POST /login
        ↓
Flask legge students.csv → cerca email
        ↓
Email trovata → session["student_id"] = "S1"
        ↓
Redirect → GET /dashboard
        ↓
Flask legge students.csv → prende student S1
        ↓
Flask renderizza dashboard.html con i dati
        ↓
UTENTE clicca "Trova match" → GET /matches
        ↓
Flask chiama find_one_way_matches("S1")
        ↓
matching.py legge offers.csv + requests.csv
        ↓
Calcola score per ogni coppia offer/request
        ↓
Flask renderizza matches.html con i risultati
        ↓
UTENTE vede la lista dei match ordinati per score
```

---

## 14. Branch strategy e commit convention

### Branch da creare

```bash
git checkout -b feature/web-setup          # Fase W1 — Flask setup
git checkout -b feature/web-csv-reader     # Fase W2 — lettura CSV
git checkout -b feature/web-routes         # Fase W3 — route e API
git checkout -b feature/web-login          # Fase W4 — login
git checkout -b feature/web-ui             # Fase W5 — HTML e CSS
git checkout -b feature/web-ranking        # Fase W6 — ranking
git checkout -b feature/web-lan            # Fase W7 — rete locale
```

### Commit convention

```
feat(web): add Flask server setup and config
feat(web): add CSV reader module
feat(web): add all Flask routes
feat(web): implement email-based login with sessions
feat(web): add base HTML template and CSS
feat(web): add ranking page with student leaderboard
feat(web): enable LAN access on host 0.0.0.0
```

### Avvio rapido (una volta configurato tutto)

```bash
cd web/
pip3 install -r requirements.txt
python3 app.py
# → apri http://localhost:5000
```

---

*Documento generato per SkillSwap School — Guida integrazione Web App*