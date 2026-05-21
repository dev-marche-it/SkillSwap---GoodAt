const SESSION_KEY = "skillswap_student";

const PAGE_META = {
  dashboard: { title: "Home", subtitle: "Panoramica del tuo profilo" },
  matches: { title: "Trova match", subtitle: "Studenti compatibili con le tue richieste" },
  exchanges: { title: "I miei scambi", subtitle: "Proposte, scambi in corso e completati" },
  catalog: { title: "Pubblica skill", subtitle: "Offri competenze o chiedi aiuto" },
  ranking: { title: "Classifica", subtitle: "I migliori tutor della scuola" },
};

const STATUS_LABEL = {
  PROPOSED: { label: "In attesa", class: "proposed" },
  ACCEPTED: { label: "In corso", class: "accepted" },
  COMPLETED: { label: "Completato", class: "completed" },
  CANCELLED: { label: "Annullato", class: "cancelled" },
};

const LEVEL_LABEL = {
  BEGINNER: "Principiante",
  INTERMEDIATE: "Intermedio",
  ADVANCED: "Avanzato",
};

let currentStudent = null;
let matchMode = "one-way";
let catalogTab = "offer";
let selectedStars = 5;
let offersCache = [];
let requestsCache = [];

// ─── Session & navigation ───────────────────────────────────

function loadSession() {
  try {
    const raw = sessionStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function saveSession(student) {
  sessionStorage.setItem(SESSION_KEY, JSON.stringify(student));
}

function clearSession() {
  sessionStorage.removeItem(SESSION_KEY);
}

function showLogin() {
  document.getElementById("view-login").classList.remove("hidden");
  document.getElementById("view-app").classList.add("hidden");
}

function showApp() {
  document.getElementById("view-login").classList.add("hidden");
  document.getElementById("view-app").classList.remove("hidden");
  updateSidebarUser();
}

function setActiveNav(page) {
  document.querySelectorAll(".nav-item").forEach((el) => {
    el.classList.toggle("active", el.dataset.page === page);
  });
  const meta = PAGE_META[page] || PAGE_META.dashboard;
  document.getElementById("page-title").textContent = meta.title;
  document.getElementById("page-subtitle").textContent = meta.subtitle;
}

function showPage(name) {
  document.querySelectorAll(".page").forEach((p) => p.classList.add("hidden"));
  document.getElementById("page-" + name)?.classList.remove("hidden");
  setActiveNav(name);
  closeSidebar();

  if (name === "dashboard") renderDashboard();
  if (name === "matches") renderMatches();
  if (name === "exchanges") renderExchanges();
  if (name === "catalog") renderCatalog();
  if (name === "ranking") renderRanking();
}

function updateSidebarUser() {
  if (!currentStudent) return;
  const initials = currentStudent.name
    .split(" ")
    .map((n) => n[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
  document.getElementById("user-avatar").textContent = initials;
  document.getElementById("user-name").textContent = currentStudent.name;
  document.getElementById("user-class").textContent = currentStudent.className;
}

function closeSidebar() {
  document.getElementById("sidebar").classList.remove("open");
  document.getElementById("sidebar-backdrop").classList.remove("open");
}

// ─── UI helpers ───────────────────────────────────────────

function toast(message, type = "default") {
  const container = document.getElementById("toast-container");
  const el = document.createElement("div");
  el.className = `toast ${type}`;
  el.textContent = message;
  container.appendChild(el);
  setTimeout(() => {
    el.style.opacity = "0";
    el.style.transition = "opacity 0.3s";
    setTimeout(() => el.remove(), 300);
  }, 3200);
}

function escapeHtml(str) {
  const d = document.createElement("div");
  d.textContent = str ?? "";
  return d.innerHTML;
}

function emptyState(icon, title, desc) {
  return `
    <div class="empty-state">
      <div class="empty-icon">${icon}</div>
      <p>${escapeHtml(title)}</p>
      <span>${escapeHtml(desc)}</span>
    </div>`;
}

function loadingCards(n = 2) {
  return Array(n)
    .fill('<div class="match-card skeleton" style="min-height:100px;background:linear-gradient(90deg,#f1f5f9 25%,#e2e8f0 50%,#f1f5f9 75%);background-size:200% 100%;animation:shimmer 1.2s infinite;border-radius:14px;"></div>')
    .join("");
}

function parseReasonTags(reason) {
  if (!reason) return "";
  const parts = reason.replace(/^SWAP:\s*/i, "").split(",").map((s) => s.trim()).filter(Boolean);
  return parts
    .map((p) => `<span class="tag positive">${escapeHtml(p)}</span>`)
    .join("");
}

function levelLabel(level) {
  return LEVEL_LABEL[level] || level;
}

async function refreshCaches() {
  [offersCache, requestsCache] = await Promise.all([
    API.offers("?active=true"),
    API.requests(),
  ]);
}

function lookupOffer(id) {
  return offersCache.find((o) => o.offerId === id);
}

function lookupRequest(id) {
  return requestsCache.find((r) => r.requestId === id);
}

// ─── Dashboard ─────────────────────────────────────────────

async function renderDashboard() {
  document.getElementById("welcome-title").textContent = currentStudent.name;
  document.getElementById("welcome-meta").textContent =
    `Classe ${currentStudent.className} · ${currentStudent.ratingCount} recensioni ricevute`;
  document.querySelector("#hero-rating .rating-big").textContent =
    currentStudent.ratingAvg.toFixed(1);

  const statsEl = document.getElementById("dashboard-stats");
  statsEl.innerHTML = loadingCards(3);

  try {
    await refreshCaches();
    const [oneWay, swaps, exchanges] = await Promise.all([
      API.oneWayMatches(currentStudent.studentId),
      API.swapMatches(currentStudent.studentId),
      API.exchanges(currentStudent.studentId),
    ]);
    const pending = exchanges.filter((e) => e.status === "PROPOSED" || e.status === "ACCEPTED").length;

    statsEl.innerHTML = `
      <div class="stat-card">
        <div class="stat-value">${oneWay.length}</div>
        <div class="stat-label">Match one-way</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">${swaps.length}</div>
        <div class="stat-label">Scambi reciproci</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">${pending}</div>
        <div class="stat-label">Scambi attivi</div>
      </div>`;

    const badge = document.getElementById("badge-exchanges");
    if (pending > 0) {
      badge.textContent = pending;
      badge.classList.remove("hidden");
    } else {
      badge.classList.add("hidden");
    }
  } catch (e) {
    statsEl.innerHTML = `<p class="alert alert-error">${escapeHtml(e.message)}</p>`;
  }
}

// ─── Matches ───────────────────────────────────────────────

async function renderMatches() {
  const box = document.getElementById("matches-list");
  const hint = document.getElementById("match-hint");
  hint.textContent =
    matchMode === "swap"
      ? "Trova un compagno con cui scambiare competenze in entrambe le direzioni."
      : "Studenti che possono aiutarti sulle competenze che hai richiesto.";

  box.innerHTML = loadingCards(2);

  try {
    await refreshCaches();
    const list =
      matchMode === "swap"
        ? await API.swapMatches(currentStudent.studentId)
        : await API.oneWayMatches(currentStudent.studentId);

    if (!list.length) {
      box.innerHTML = emptyState(
        "🔍",
        "Nessun match al momento",
        "Pubblica una richiesta in «Pubblica skill» o attendi nuove offerte dai compagni."
      );
      return;
    }

    box.innerHTML = list
      .map((m) => {
        const offer = lookupOffer(m.offerId);
        const request = lookupRequest(m.requestId);
        const skillName = offer?.skillName || request?.skillName || "Competenza";
        const tutorName = offer?.studentName || "—";
        const yourSkill = request?.skillName || "—";

        return `
          <article class="match-card">
            <div class="match-card-header">
              <div>
                <div class="match-title">${escapeHtml(skillName)}</div>
                <div class="match-meta">
                  ${matchMode === "swap" ? "Scambio reciproco · " : ""}
                  Con <strong>${escapeHtml(tutorName)}</strong>
                  ${request ? ` · per la tua richiesta su <em>${escapeHtml(yourSkill)}</em>` : ""}
                </div>
              </div>
              <span class="score-badge">${m.score} pt</span>
            </div>
            <div class="reason-tags">${parseReasonTags(m.reason)}</div>
            <button type="button" class="btn btn-primary btn-sm" data-propose="${m.offerId}" data-request="${m.requestId}">
              Proponi scambio
            </button>
          </article>`;
      })
      .join("");

    box.querySelectorAll("[data-propose]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        btn.disabled = true;
        try {
          await API.proposeExchange(btn.dataset.propose, btn.dataset.request);
          toast("Scambio proposto con successo!", "success");
          showPage("exchanges");
        } catch (e) {
          toast(e.message, "error");
          btn.disabled = false;
        }
      });
    });
  } catch (e) {
    box.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
  }
}

// ─── Exchanges ─────────────────────────────────────────────

async function renderExchanges() {
  const box = document.getElementById("exchanges-list");
  box.innerHTML = loadingCards(2);

  try {
    const list = await API.exchanges(currentStudent.studentId);

    if (!list.length) {
      box.innerHTML = emptyState(
        "🔄",
        "Nessuno scambio",
        "Vai in «Trova match» e proponi il primo scambio con un compagno."
      );
      return;
    }

    box.innerHTML = list.map((e) => renderExchangeCard(e)).join("");
    bindExchangeActions(box);
    updateExchangeBadge(list);
  } catch (err) {
    box.innerHTML = `<div class="alert alert-error">${escapeHtml(err.message)}</div>`;
  }
}

function renderExchangeCard(e) {
  const st = STATUS_LABEL[e.status] || { label: e.status, class: "proposed" };
  const actions = [];

  if (e.status === "PROPOSED") {
    actions.push(`<button class="btn btn-primary btn-sm" data-accept="${e.exchangeId}">Accetta</button>`);
    actions.push(`<button class="btn btn-danger btn-sm" data-cancel="${e.exchangeId}">Annulla</button>`);
  }
  if (e.status === "ACCEPTED") {
    actions.push(`<button class="btn btn-primary btn-sm" data-complete="${e.exchangeId}">Segna completato</button>`);
  }
  if (e.status === "COMPLETED") {
    actions.push(`<button class="btn btn-secondary btn-sm" data-review="${e.exchangeId}">Lascia recensione</button>`);
  }

  return `
    <article class="exchange-card">
      <div style="display:flex;justify-content:space-between;align-items:center;gap:0.5rem;flex-wrap:wrap;margin-bottom:0.5rem;">
        <strong>${escapeHtml(e.exchangeId)}</strong>
        <span class="status-pill ${st.class}">${st.label}</span>
      </div>
      <div class="exchange-lines">
        <div>📤 ${escapeHtml(e.offerSummary)}</div>
        <div>📥 ${escapeHtml(e.requestSummary)}</div>
      </div>
      <div class="card-actions">${actions.join("")}</div>
    </article>`;
}

function bindExchangeActions(box) {
  box.querySelectorAll("[data-accept]").forEach((b) =>
    b.addEventListener("click", () => exchangeAction(b.dataset.accept, "accept")));
  box.querySelectorAll("[data-cancel]").forEach((b) =>
    b.addEventListener("click", () => exchangeAction(b.dataset.cancel, "cancel")));
  box.querySelectorAll("[data-complete]").forEach((b) =>
    b.addEventListener("click", () => exchangeAction(b.dataset.complete, "complete")));
  box.querySelectorAll("[data-review]").forEach((b) =>
    b.addEventListener("click", () => openReviewModal(b.dataset.review)));
}

function updateExchangeBadge(list) {
  const pending = list.filter((e) => e.status === "PROPOSED" || e.status === "ACCEPTED").length;
  const badge = document.getElementById("badge-exchanges");
  if (pending > 0) {
    badge.textContent = pending;
    badge.classList.remove("hidden");
  } else {
    badge.classList.add("hidden");
  }
}

async function exchangeAction(id, action) {
  try {
    if (action === "accept") await API.acceptExchange(id);
    if (action === "cancel") await API.cancelExchange(id);
    if (action === "complete") await API.completeExchange(id);
    const labels = { accept: "Scambio accettato", cancel: "Scambio annullato", complete: "Scambio completato" };
    toast(labels[action], "success");
    renderExchanges();
    if (action === "complete") renderDashboard();
  } catch (e) {
    toast(e.message, "error");
  }
}

// ─── Review modal ──────────────────────────────────────────

function openReviewModal(exchangeId) {
  document.getElementById("review-exchange-id").value = exchangeId;
  document.getElementById("review-comment").value = "";
  selectedStars = 5;
  document.getElementById("review-stars").value = "5";
  document.querySelectorAll("#star-picker button").forEach((b, i) => {
    b.classList.toggle("active", i < 5);
  });
  document.getElementById("modal-review").showModal();
}

function setupStarPicker() {
  document.querySelectorAll("#star-picker button").forEach((btn) => {
    btn.addEventListener("click", () => {
      selectedStars = parseInt(btn.dataset.star, 10);
      document.getElementById("review-stars").value = String(selectedStars);
      document.querySelectorAll("#star-picker button").forEach((b, i) => {
        b.classList.toggle("active", i < selectedStars);
      });
    });
  });
}

// ─── Catalog ───────────────────────────────────────────────

function showCatalogPanel(tab) {
  catalogTab = tab;
  document.querySelectorAll("[data-catalog]").forEach((b) => {
    b.classList.toggle("active", b.dataset.catalog === tab);
  });
  document.getElementById("catalog-panel-offer").classList.toggle("hidden", tab !== "offer");
  document.getElementById("catalog-panel-request").classList.toggle("hidden", tab !== "request");
  document.getElementById("catalog-panel-browse").classList.toggle("hidden", tab !== "browse");
  if (tab === "browse") renderBrowseTables();
}

async function renderCatalog() {
  try {
    const skills = await API.skills();
    const fill = (sel) => {
      sel.innerHTML =
        '<option value="" disabled selected>Seleziona competenza…</option>' +
        skills.map((s) => `<option value="${s.skillId}">${escapeHtml(s.name)}</option>`).join("");
    };
    fill(document.getElementById("offer-skill"));
    fill(document.getElementById("request-skill"));
    showCatalogPanel(catalogTab);
  } catch (e) {
    toast(e.message, "error");
  }
}

async function renderBrowseTables() {
  try {
    await refreshCaches();
    const offersEl = document.getElementById("catalog-offers");
    const requestsEl = document.getElementById("catalog-requests");

    offersEl.innerHTML = offersCache.length
      ? `<table class="data-table"><thead><tr><th>Studente</th><th>Skill</th><th>Livello</th></tr></thead><tbody>` +
        offersCache
          .map(
            (o) =>
              `<tr><td>${escapeHtml(o.studentName)}</td><td>${escapeHtml(o.skillName)}</td><td><span class="level-chip">${levelLabel(o.level)}</span></td></tr>`
          )
          .join("") +
        "</tbody></table>"
      : emptyState("📋", "Nessuna offerta", "Sii il primo a pubblicare un’offerta!");

    requestsEl.innerHTML = requestsCache.length
      ? `<table class="data-table"><thead><tr><th>Studente</th><th>Skill</th><th>Livello min.</th></tr></thead><tbody>` +
        requestsCache
          .map(
            (r) =>
              `<tr><td>${escapeHtml(r.studentName)}</td><td>${escapeHtml(r.skillName)}</td><td><span class="level-chip">${levelLabel(r.minLevel)}</span></td></tr>`
          )
          .join("") +
        "</tbody></table>"
      : emptyState("📋", "Nessuna richiesta", "Pubblica la tua prima richiesta di aiuto.");
  } catch (e) {
    toast(e.message, "error");
  }
}

// ─── Ranking ───────────────────────────────────────────────

async function renderRanking() {
  const tbody = document.getElementById("ranking-body");
  const empty = document.getElementById("ranking-empty");
  const podium = document.getElementById("ranking-podium");

  try {
    const list = await API.ranking();
    if (!list.length) {
      tbody.innerHTML = "";
      podium.classList.add("hidden");
      empty.classList.remove("hidden");
      return;
    }
    empty.classList.add("hidden");

    const top3 = list.slice(0, 3);
    const order = top3.length >= 3 ? [1, 0, 2] : top3.map((_, i) => i);
    const medals = ["🥇", "🥈", "🥉"];
    podium.innerHTML = order
      .map((idx, displayIdx) => {
        const s = top3[idx];
        if (!s) return "";
        const placeClass = displayIdx === 0 && top3.length >= 2 ? "first" : "";
        return `
          <div class="podium-item ${placeClass}">
            <div class="place">${medals[displayIdx] || displayIdx + 1}</div>
            <strong>${escapeHtml(s.name)}</strong>
            <div class="rating">★ ${s.ratingAvg.toFixed(1)}</div>
            <small style="color:var(--text-muted)">${s.ratingCount} voti</small>
          </div>`;
      })
      .join("");
    podium.classList.remove("hidden");

    tbody.innerHTML = list
      .map(
        (s, i) => `
      <tr>
        <td>${i + 1}</td>
        <td><strong>${escapeHtml(s.name)}</strong></td>
        <td>${escapeHtml(s.className)}</td>
        <td><span style="color:var(--primary-dark);font-weight:700">★ ${s.ratingAvg.toFixed(1)}</span></td>
        <td>${s.ratingCount}</td>
      </tr>`
      )
      .join("");
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="5" class="alert alert-error">${escapeHtml(e.message)}</td></tr>`;
  }
}

// ─── Event listeners ───────────────────────────────────────

function showAuthTab(tab) {
  document.querySelectorAll("[data-auth-tab]").forEach((b) => {
    b.classList.toggle("active", b.dataset.authTab === tab);
  });
  document.getElementById("auth-panel-login").classList.toggle("hidden", tab !== "login");
  document.getElementById("auth-panel-register").classList.toggle("hidden", tab !== "register");
  document.getElementById("login-error").classList.add("hidden");
  document.getElementById("register-error").classList.add("hidden");
}

document.querySelectorAll("[data-auth-tab]").forEach((btn) => {
  btn.addEventListener("click", () => showAuthTab(btn.dataset.authTab));
});

document.getElementById("form-login").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const err = document.getElementById("login-error");
  err.classList.add("hidden");
  try {
    const email = document.getElementById("login-email").value.trim();
    const password = document.getElementById("login-password").value;
    currentStudent = await API.login(email, password);
    saveSession(currentStudent);
    showApp();
    showPage("dashboard");
    toast(`Bentornato, ${currentStudent.name}!`, "success");
  } catch (e) {
    err.textContent = e.message;
    err.classList.remove("hidden");
  }
});

document.getElementById("form-register").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const err = document.getElementById("register-error");
  err.classList.add("hidden");
  const password = document.getElementById("reg-password").value;
  const confirm = document.getElementById("reg-password-confirm").value;
  if (password !== confirm) {
    err.textContent = "Le password non coincidono.";
    err.classList.remove("hidden");
    return;
  }
  try {
    currentStudent = await API.register(
      document.getElementById("reg-name").value.trim(),
      document.getElementById("reg-class").value.trim(),
      document.getElementById("reg-email").value.trim(),
      password
    );
    saveSession(currentStudent);
    showApp();
    showPage("dashboard");
    toast(`Account creato. Benvenuto, ${currentStudent.name}!`, "success");
  } catch (e) {
    err.textContent = e.message;
    err.classList.remove("hidden");
  }
});

document.querySelectorAll("[data-demo-email]").forEach((btn) => {
  btn.addEventListener("click", () => {
    showAuthTab("login");
    document.getElementById("login-email").value = btn.dataset.demoEmail;
    document.getElementById("login-password").value = "SkillSwap123";
  });
});

document.getElementById("btn-logout").addEventListener("click", () => {
  clearSession();
  currentStudent = null;
  showLogin();
  toast("Sei uscito dall’account");
});

document.querySelectorAll(".nav-item[data-page]").forEach((a) => {
  a.addEventListener("click", (ev) => {
    ev.preventDefault();
    showPage(a.dataset.page);
  });
});

document.querySelectorAll("[data-goto]").forEach((btn) => {
  btn.addEventListener("click", () => showPage(btn.dataset.goto));
});

document.querySelectorAll("[data-match]").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll("[data-match]").forEach((b) => b.classList.remove("active"));
    btn.classList.add("active");
    matchMode = btn.dataset.match;
    renderMatches();
  });
});

document.querySelectorAll("[data-catalog]").forEach((btn) => {
  btn.addEventListener("click", () => showCatalogPanel(btn.dataset.catalog));
});

document.getElementById("form-offer").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const skillId = document.getElementById("offer-skill").value;
  if (!skillId) return toast("Seleziona una competenza", "error");
  try {
    await API.createOffer({
      studentId: currentStudent.studentId,
      skillId,
      level: document.getElementById("offer-level").value,
      note: document.getElementById("offer-note").value,
    });
    toast("Offerta pubblicata!", "success");
    document.getElementById("form-offer").reset();
    renderCatalog();
    renderDashboard();
  } catch (e) {
    toast(e.message, "error");
  }
});

document.getElementById("form-request").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const skillId = document.getElementById("request-skill").value;
  if (!skillId) return toast("Seleziona una competenza", "error");
  try {
    await API.createRequest({
      studentId: currentStudent.studentId,
      skillId,
      minLevel: document.getElementById("request-min-level").value,
      note: document.getElementById("request-note").value,
    });
    toast("Richiesta pubblicata!", "success");
    document.getElementById("form-request").reset();
    renderCatalog();
    renderDashboard();
  } catch (e) {
    toast(e.message, "error");
  }
});

document.getElementById("form-review").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const exchangeId = document.getElementById("review-exchange-id").value;
  const stars = parseInt(document.getElementById("review-stars").value, 10);
  const comment = document.getElementById("review-comment").value.trim();
  try {
    await API.addReview(exchangeId, currentStudent.studentId, stars, comment);
    currentStudent = await API.json("/api/students/" + currentStudent.studentId);
    saveSession(currentStudent);
    document.getElementById("modal-review").close();
    toast("Grazie! Recensione inviata.", "success");
    renderExchanges();
    renderDashboard();
  } catch (e) {
    toast(e.message, "error");
  }
});

document.getElementById("review-cancel").addEventListener("click", () => {
  document.getElementById("modal-review").close();
});

document.getElementById("menu-toggle").addEventListener("click", () => {
  document.getElementById("sidebar").classList.add("open");
  document.getElementById("sidebar-backdrop").classList.add("open");
});

document.getElementById("sidebar-close").addEventListener("click", closeSidebar);
document.getElementById("sidebar-backdrop").addEventListener("click", closeSidebar);

setupStarPicker();

// ─── Init ──────────────────────────────────────────────────

currentStudent = loadSession();
if (currentStudent) {
  showApp();
  showPage("dashboard");
} else {
  showLogin();
}
