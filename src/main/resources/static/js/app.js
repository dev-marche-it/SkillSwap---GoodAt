const SESSION_KEY = "skillswap_student";

const PAGE_META = {
  dashboard: { title: "Home", subtitle: "Panoramica del tuo profilo" },
  community: { title: "Bacheca", subtitle: "Offerte e richieste di tutti gli studenti" },
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
let catalogTab = "browse";
let communityTab = "offers";
let selectedStars = 5;
let offersCache = [];
let requestsCache = [];
let communityData = null;

// ─── Session & navigation ───────────────────────────────────

/** Normalizza sessioni salvate con formati vecchi (es. `id` invece di `studentId`). */
function normalizeStudent(student) {
  if (!student) return null;
  const sid = student.studentId || student.id || student.student_id;
  if (!sid) return student;
  return { ...student, studentId: sid };
}

function getStudentId() {
  const sid = currentStudent?.studentId || currentStudent?.id;
  if (!sid) {
    throw new Error("Sessione non valida. Esci e accedi di nuovo.");
  }
  return sid;
}

function loadSession() {
  try {
    const raw = sessionStorage.getItem(SESSION_KEY);
    return raw ? normalizeStudent(JSON.parse(raw)) : null;
  } catch {
    return null;
  }
}

function saveSession(student) {
  sessionStorage.setItem(SESSION_KEY, JSON.stringify(normalizeStudent(student)));
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
  if (name === "community") renderCommunity();
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
    API.offers(""),
    API.requests(),
  ]);
}

async function loadCommunity() {
  communityData = await API.community(getStudentId());
  offersCache = communityData.activeOffers.concat(communityData.myOffers);
  requestsCache = communityData.openRequests.concat(communityData.myRequests);
  return communityData;
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
      API.oneWayMatches(getStudentId()),
      API.swapMatches(getStudentId()),
      API.exchanges(getStudentId()),
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

    await loadCommunity();
    const preview = document.getElementById("dashboard-community");
    const topOffers = communityData.activeOffers.slice(0, 3);
    preview.innerHTML = topOffers.length
      ? topOffers.map((o) => renderOfferCard(o, communityData.myRequests, false)).join("")
      : emptyState("👥", "Bacheca vuota", "Sii il primo a pubblicare un'offerta o una richiesta!");
    bindCommunityActions(preview);
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
        ? await API.swapMatches(getStudentId())
        : await API.oneWayMatches(getStudentId());

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
            <button type="button" class="btn btn-primary btn-sm" data-propose-offer="${m.offerId}" data-propose-request="${m.requestId}">
              Proponi scambio
            </button>
          </article>`;
      })
      .join("");

    box.querySelectorAll("[data-propose-offer]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        btn.disabled = true;
        try {
          await API.proposeExchange(btn.dataset.proposeOffer, btn.dataset.proposeRequest, getStudentId());
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
    const list = await API.exchanges(getStudentId());

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

  if (e.canAccept) {
    actions.push(`<button class="btn btn-primary btn-sm" data-accept="${e.exchangeId}">Accetta proposta</button>`);
  }
  if (e.canCancel) {
    actions.push(`<button class="btn btn-danger btn-sm" data-cancel="${e.exchangeId}">Annulla</button>`);
  }
  if (e.canComplete) {
    actions.push(`<button class="btn btn-primary btn-sm" data-complete="${e.exchangeId}">Segna completato</button>`);
  }
  if (e.canReview) {
    actions.push(`<button class="btn btn-secondary btn-sm" data-review="${e.exchangeId}">Lascia recensione</button>`);
  } else if (e.status === "COMPLETED") {
    actions.push(`<span class="card-desc">Hai già lasciato una recensione per questo scambio.</span>`);
  }

  const roleHint =
    e.status === "PROPOSED" && e.canAccept
      ? `<p class="card-desc">Sei il tutor (${escapeHtml(e.offerStudentName)}): puoi accettare questa proposta.</p>`
      : e.status === "PROPOSED"
        ? `<p class="card-desc">In attesa che <strong>${escapeHtml(e.offerStudentName)}</strong> accetti la tua proposta.</p>`
        : "";

  return `
    <article class="exchange-card">
      <div style="display:flex;justify-content:space-between;align-items:center;gap:0.5rem;flex-wrap:wrap;margin-bottom:0.5rem;">
        <strong>${escapeHtml(e.exchangeId)}</strong>
        <span class="status-pill ${st.class}">${st.label}</span>
      </div>
      ${roleHint}
      <div class="exchange-lines">
        <div>📤 <strong>${escapeHtml(e.offerStudentName)}</strong> offre: ${escapeHtml(e.offerSkillName)}</div>
        <div>📥 <strong>${escapeHtml(e.requestStudentName)}</strong> cerca: ${escapeHtml(e.requestSkillName)}</div>
      </div>
      <div class="card-actions">${actions.join("") || '<span class="card-desc">Nessuna azione disponibile per te in questo stato.</span>'}</div>
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
    const sid = getStudentId();
    if (action === "accept") await API.acceptExchange(id, sid);
    if (action === "cancel") await API.cancelExchange(id, sid);
    if (action === "complete") await API.completeExchange(id, sid);
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

// ─── Community ─────────────────────────────────────────────

function renderOfferCard(offer, myRequests, compact = false) {
  const matching = myRequests.filter((r) => r.skillId === offer.skillId);
  const proposeBlock =
    matching.length === 1
      ? `<button type="button" class="btn btn-primary btn-sm" data-community-propose data-offer-id="${offer.offerId}" data-request-id="${matching[0].requestId}">Proponi scambio</button>`
      : matching.length > 1
        ? `<label class="field" style="margin-top:0.75rem"><span>La tua richiesta su ${escapeHtml(offer.skillName)}</span>
           <select class="community-request-pick" data-offer-id="${offer.offerId}">
             ${matching.map((r) => `<option value="${r.requestId}">${escapeHtml(r.requestId)} — min ${levelLabel(r.minLevel)}</option>`).join("")}
           </select></label>
           <button type="button" class="btn btn-primary btn-sm" data-community-propose-select data-offer-id="${offer.offerId}">Proponi scambio</button>`
        : `<div class="card-actions" style="margin-top:0.75rem">
             <span class="card-desc">Per proporre uno scambio, pubblica prima una richiesta su «${escapeHtml(offer.skillName)}».</span>
             <button type="button" class="btn btn-secondary btn-sm" data-goto-request data-skill-id="${offer.skillId}">Pubblica richiesta</button>
           </div>`;

  return `
    <article class="match-card">
      <div class="match-card-header">
        <div>
          <div class="match-title">${escapeHtml(offer.skillName)}</div>
          <div class="match-meta">Offerta di <strong>${escapeHtml(offer.studentName)}</strong> · ${levelLabel(offer.level)}${offer.note ? ` · ${escapeHtml(offer.note)}` : ""}</div>
        </div>
        ${offer.active ? '<span class="tag positive">Attiva</span>' : '<span class="tag">Non attiva</span>'}
      </div>
      ${compact ? "" : proposeBlock}
    </article>`;
}

function renderRequestCard(req) {
  return `
    <article class="match-card">
      <div class="match-title">${escapeHtml(req.skillName)}</div>
      <div class="match-meta">Richiesta di <strong>${escapeHtml(req.studentName)}</strong> · min ${levelLabel(req.minLevel)}${req.note ? ` · ${escapeHtml(req.note)}` : ""}</div>
    </article>`;
}

const COMMUNITY_TAB_HINTS = {
  offers:
    "📌 <strong>In tempo reale:</strong> solo offerte <em>attive</em> pubblicate da altri studenti. Quelle concluse o disattivate non compaiono qui.",
  requests:
    "📌 <strong>In tempo reale:</strong> richieste <em>aperte</em> degli altri studenti. Non è lo storico: sono annunci ancora validi.",
  mine:
    "🗂️ <strong>Il tuo archivio:</strong> tutte le offerte e richieste che hai pubblicato, <em>attive e passate</em> (anche concluse dopo uno scambio).",
};

function setCommunityTabHint(tab) {
  const el = document.getElementById("community-tab-hint");
  if (el) el.innerHTML = COMMUNITY_TAB_HINTS[tab] || "";
}

function renderMyOfferCard(offer) {
  const isPast = !offer.active;
  return `
    <article class="match-card ${isPast ? "is-history" : ""}">
      <div class="match-card-header">
        <div>
          <div class="match-title">${escapeHtml(offer.skillName)}</div>
          <div class="match-meta">${levelLabel(offer.level)}${offer.note ? ` · ${escapeHtml(offer.note)}` : ""}</div>
        </div>
        ${offer.active
          ? '<span class="tag positive">Pubblicata · attiva</span>'
          : '<span class="tag muted">Pubblicata · passata / conclusa</span>'}
      </div>
    </article>`;
}

function renderMyRequestCard(req) {
  return `
    <article class="match-card is-history">
      <div class="match-card-header">
        <div>
          <div class="match-title">${escapeHtml(req.skillName)}</div>
          <div class="match-meta">min ${levelLabel(req.minLevel)}${req.note ? ` · ${escapeHtml(req.note)}` : ""}</div>
        </div>
        <span class="tag muted">Richiesta pubblicata</span>
      </div>
    </article>`;
}

async function renderCommunity() {
  const box = document.getElementById("community-list");
  box.innerHTML = loadingCards(3);
  setCommunityTabHint(communityTab);
  try {
    const data = await loadCommunity();
    let items = [];
    if (communityTab === "offers") items = data.activeOffers;
    else if (communityTab === "requests") items = data.openRequests;
    else {
      const activeOffers = data.myOffers.filter((o) => o.active);
      const pastOffers = data.myOffers.filter((o) => !o.active);

      box.innerHTML =
        `<h3 class="section-label with-badge"><span>Le tue offerte attive</span><span class="section-badge live">Oggi</span></h3>` +
        (activeOffers.length
          ? activeOffers.map(renderMyOfferCard).join("")
          : emptyState("📤", "Nessuna offerta attiva", "Pubblica ciò che sai insegnare da «Pubblica skill».")) +
        `<h3 class="section-label with-badge" style="margin-top:1.5rem"><span>Le tue offerte passate</span><span class="section-badge archive">Storico</span></h3>` +
        (pastOffers.length
          ? pastOffers.map(renderMyOfferCard).join("")
          : `<p class="card-desc" style="margin:0 0 1rem">Nessuna offerta conclusa o disattivata.</p>`) +
        `<h3 class="section-label with-badge" style="margin-top:1.5rem"><span>Le tue richieste pubblicate</span><span class="section-badge archive">Storico</span></h3>` +
        (data.myRequests.length
          ? data.myRequests.map(renderMyRequestCard).join("")
          : emptyState("📥", "Nessuna richiesta", "Pubblica su cosa hai bisogno di aiuto."));
      return;
    }

    if (!items.length) {
      box.innerHTML = emptyState(
        "👥",
        "Nessun contenuto",
        communityTab === "offers"
          ? "Nessuna offerta attiva al momento dagli altri studenti."
          : "Nessuna richiesta aperta al momento dagli altri studenti."
      );
      return;
    }

    box.innerHTML =
      communityTab === "offers"
        ? items.map((o) => renderOfferCard(o, data.myRequests)).join("")
        : items.map(renderRequestCard).join("");
    bindCommunityActions(box);
  } catch (e) {
    box.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
  }
}

function bindCommunityActions(box) {
  box.querySelectorAll("[data-community-propose]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      try {
        await API.proposeExchange(btn.dataset.offerId, btn.dataset.requestId, getStudentId());
        toast("Proposta inviata! Il tutor può accettare da «I miei scambi».", "success");
        showPage("exchanges");
      } catch (e) {
        toast(e.message, "error");
      }
    });
  });
  box.querySelectorAll("[data-community-propose-select]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const select = box.querySelector(`select[data-offer-id="${btn.dataset.offerId}"]`);
      if (!select?.value) return toast("Seleziona una richiesta", "error");
      try {
        await API.proposeExchange(btn.dataset.offerId, select.value, getStudentId());
        toast("Proposta inviata!", "success");
        showPage("exchanges");
      } catch (e) {
        toast(e.message, "error");
      }
    });
  });
  box.querySelectorAll("[data-goto-request]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      showPage("catalog");
      await renderCatalog();
      showCatalogPanel("request");
      const skillSel = document.getElementById("request-skill");
      if (btn.dataset.skillId && skillSel) {
        skillSel.value = btn.dataset.skillId;
      }
      toast("Compila e pubblica la richiesta, poi torna in Bacheca.", "default");
    });
  });
}

/** Aggiorna bacheca, catalogo e home dopo una pubblicazione. */
async function refreshAfterPublish() {
  await loadCommunity();
  renderDashboard();
  if (!document.getElementById("page-community").classList.contains("hidden")) {
    renderCommunity();
  }
  if (catalogTab === "browse") {
    renderBrowseTables();
  }
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
    const data = await loadCommunity();
    const offersEl = document.getElementById("catalog-offers");
    const requestsEl = document.getElementById("catalog-requests");

    offersEl.innerHTML = data.activeOffers.length
      ? data.activeOffers.map((o) => renderOfferCard(o, data.myRequests)).join("")
      : emptyState("📋", "Nessuna offerta attiva", "Sii il primo a pubblicare un'offerta!");

    requestsEl.innerHTML = data.openRequests.length
      ? data.openRequests.map(renderRequestCard).join("")
      : emptyState("📋", "Nessuna richiesta", "Pubblica la tua prima richiesta di aiuto.");

    bindCommunityActions(offersEl);
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
    currentStudent = normalizeStudent(await API.login(email, password));
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
    currentStudent = normalizeStudent(
      await API.register(
        document.getElementById("reg-name").value.trim(),
        document.getElementById("reg-class").value.trim(),
        document.getElementById("reg-email").value.trim(),
        password
      )
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

document.querySelectorAll("[data-community-tab]").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll("[data-community-tab]").forEach((b) => b.classList.remove("active"));
    btn.classList.add("active");
    communityTab = btn.dataset.communityTab;
    renderCommunity();
  });
});

document.getElementById("form-offer").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const skillId = document.getElementById("offer-skill").value;
  if (!skillId) return toast("Seleziona una competenza", "error");
  try {
    await API.createOffer({
      studentId: getStudentId(),
      skillId,
      level: document.getElementById("offer-level").value,
      note: document.getElementById("offer-note").value,
    });
    toast("Offerta pubblicata! Visibile in Bacheca.", "success");
    document.getElementById("form-offer").reset();
    showCatalogPanel("browse");
    await refreshAfterPublish();
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
      studentId: getStudentId(),
      skillId,
      minLevel: document.getElementById("request-min-level").value,
      note: document.getElementById("request-note").value,
    });
    toast("Richiesta pubblicata! Ora puoi proporre scambi in Bacheca.", "success");
    document.getElementById("form-request").reset();
    showCatalogPanel("browse");
    await refreshAfterPublish();
  } catch (e) {
    toast(e.message, "error");
  }
});

document.getElementById("form-review").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const exchangeId = document.getElementById("review-exchange-id").value;
  const stars = parseInt(document.getElementById("review-stars").value, 10);
  const comment = document.getElementById("review-comment").value.trim();
  if (!exchangeId) return toast("Scambio non valido", "error");
  if (stars < 1 || stars > 5) return toast("Seleziona un voto da 1 a 5 stelle", "error");
  const submitBtn = ev.target.querySelector('button[type="submit"]');
  if (submitBtn) submitBtn.disabled = true;
  try {
    await API.addReview(exchangeId, getStudentId(), stars, comment);
    currentStudent = normalizeStudent(await API.json("/api/students/" + getStudentId()));
    saveSession(currentStudent);
    document.getElementById("modal-review").close();
    toast("Grazie! Recensione inviata.", "success");
    renderExchanges();
    renderDashboard();
  } catch (e) {
    toast(e.message, "error");
  } finally {
    if (submitBtn) submitBtn.disabled = false;
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
  if (!currentStudent.studentId) {
    clearSession();
    currentStudent = null;
    showLogin();
    toast("Sessione aggiornata: accedi di nuovo.", "default");
  } else {
    showApp();
    showPage("dashboard");
  }
} else {
  showLogin();
}
