Modifiche effettuate dal feature/web-w1-flask-setup (data: 2026-05-23)

- Implementata API REST di base in `app.py` per i seguenti endpoint:
  - `GET /api/skills`, `GET /api/students`, `GET /api/offers`, `GET /api/requests`
  - `GET /api/exchanges`, `POST /api/exchanges`, `PUT /api/exchanges/{id}/accept|complete|cancel`
  - `GET /api/reviews`, `POST /api/reviews`
  - `POST /api/offers`, `POST /api/requests`
  - `GET /api/students/{id}/reviews`, `GET /api/ranking`
  - placeholder per matching: `GET /api/students/{id}/matches/one-way` e `/swap`

- Lettura/scrittura atomica dei CSV in `data/` usando path condiviso (`web/config.py` -> `DATA_DIR`).
- Aggiunto supporto CORS (installare `Flask-Cors` dalla `requirements.txt`).
- Aggiornato `requirements.txt` con `Flask-Cors`.
- Aggiunta logica per ricalcolare i rating studenti dopo l'inserimento di nuove recensioni.

Note e raccomandazioni:
- Questa implementazione replica il comportamento di lettura/scrittura dei CSV (prototipo).
- Per integrazione completa con i `Service` Java, considerare un controller HTTP centralizzato in Java
  o un wrapper che invochi direttamente i metodi Java (WIP).
