"""
Server Flask SkillSwap School — fase W1 (setup minimo).
Avvio da questa cartella: python app.py
"""
from flask import Flask

from config import HOST, PORT, SECRET_KEY

app = Flask(__name__)
app.secret_key = SECRET_KEY


@app.route("/")
def index():
    return "SkillSwap School — server attivo"


if __name__ == "__main__":
    app.run(host=HOST, port=PORT, debug=True)
