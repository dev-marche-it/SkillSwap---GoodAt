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
      const msg = data && data.error ? data.error : `Errore HTTP ${res.status}`;
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
    const q = studentId ? `?studentId=${encodeURIComponent(studentId)}` : "";
    return this.json("/api/exchanges" + q);
  },

  proposeExchange(offerId, requestId) {
    return this.json("/api/exchanges", {
      method: "POST",
      body: JSON.stringify({ offerId, requestId }),
    });
  },

  acceptExchange(id) {
    return this.json(`/api/exchanges/${id}/accept`, { method: "PUT" });
  },
  completeExchange(id) {
    return this.json(`/api/exchanges/${id}/complete`, { method: "PUT" });
  },
  cancelExchange(id) {
    return this.json(`/api/exchanges/${id}/cancel`, { method: "PUT" });
  },

  addReview(exchangeId, reviewerStudentId, stars, comment) {
    return this.json("/api/reviews", {
      method: "POST",
      body: JSON.stringify({ exchangeId, reviewerStudentId, stars, comment }),
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
