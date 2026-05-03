import os

# Directory radice del repository (genitore della cartella web/)
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA_DIR = os.path.join(BASE_DIR, "data")

# Chiave segreta per le sessioni Flask (sostituire in produzione)
SECRET_KEY = "skillswap-secret-key-2026"

PORT = 5000

# 0.0.0.0 consente accesso dalla rete locale (vedi docs/Web_integration.md fase W7)
HOST = "0.0.0.0"
