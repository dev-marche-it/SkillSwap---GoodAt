const API = {
  async json(url, options = {}) {
    const res = await fetch(url, {
      headers: { "Content-Type": "application/json", ...options.headers },
      ...options,
    });
    const text = await res.text();
    let data = null;
    if (text) {
      try {
        data = JSON.parse(text);
      } catch {
        data = text;
      }
    }
    if (!res.ok) {
      const msg =
        (data && data.error) ||
        (data && data.message) ||
        (typeof data === "string" ? data : null) ||
        `Errore HTTP ${res.status}`;
      throw new Error(msg);
    }
    return data;
  },

  login(email, password) {
    return this.json("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
  },

  register(name, className, email, password) {
    return this.json("/api/auth/register", {
      method: "POST",
      body: JSON.stringify({ name, className, email, password }),
    });
  },

  students() { return this.json("/api/students"); },
  skills() { return this.json("/api/skills"); },
  offers(params = "") { return this.json("/api/offers" + params); },
  requests(params = "") { return this.json("/api/requests" + params); },
  ranking() { return this.json("/api/ranking"); },

  oneWayMatches(studentId) {
    return this.json(`/api/students/${studentId}/matches/one-way`);
  },
  swapMatches(studentId) {
    return this.json(`/api/students/${studentId}/matches/swap`);
  },

  exchanges(studentId) {
    const q = `?studentId=${encodeURIComponent(studentId)}`;
    return this.json("/api/exchanges" + q);
  },

  community(studentId) {
    return this.json(`/api/community?studentId=${encodeURIComponent(studentId)}`);
  },

  proposeExchange(offerId, requestId, requesterStudentId) {
    const sid = String(requesterStudentId || "").trim();
    if (!sid) {
      return Promise.reject(new Error("Sessione scaduta: esci e accedi di nuovo."));
    }
    return this.json("/api/exchanges", {
      method: "POST",
      body: JSON.stringify({ offerId, requestId, requesterStudentId: sid, studentId: sid }),
    });
  },

  acceptExchange(id, studentId) {
    return this.json(`/api/exchanges/${id}/accept`, {
      method: "PUT",
      body: JSON.stringify({ studentId }),
    });
  },
  completeExchange(id, studentId) {
    return this.json(`/api/exchanges/${id}/complete`, {
      method: "PUT",
      body: JSON.stringify({ studentId }),
    });
  },
  cancelExchange(id, studentId) {
    return this.json(`/api/exchanges/${id}/cancel`, {
      method: "PUT",
      body: JSON.stringify({ studentId }),
    });
  },

  addReview(exchangeId, reviewerStudentId, stars, comment) {
    const sid = String(reviewerStudentId || "").trim();
    if (!sid) {
      return Promise.reject(new Error("Sessione scaduta: esci e accedi di nuovo."));
    }
    return this.json("/api/reviews", {
      method: "POST",
      body: JSON.stringify({
        exchangeId,
        reviewerStudentId: sid,
        studentId: sid,
        stars: Number(stars),
        comment: comment || "",
      }),
    });
  },

  studentReviews(id) {
    return this.json(`/api/students/${id}/reviews`);
  },

  createOffer(body) {
    return this.json("/api/offers", { method: "POST", body: JSON.stringify(body) });
  },
  createRequest(body) {
    return this.json("/api/requests", { method: "POST", body: JSON.stringify(body) });
  },
};
