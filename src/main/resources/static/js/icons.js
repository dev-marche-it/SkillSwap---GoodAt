/** Icone SVG minimali — palette SkillSwap */
const ICON_PATHS = {
  users:
    '<path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/>',
  search: '<circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>',
  swap: '<path d="M7 16V4m0 0L3 8m4-4l4 4M17 8v12m0 0l4-4m-4 4l-4-4"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  trophy: '<path d="M8 21h8M12 17v4M7 4h10v5a5 5 0 01-10 0V4zM5 4H3v2a3 3 0 003 3M19 4h2v2a3 3 0 01-3 3"/>',
  board: '<rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M9 21V9"/>',
  upload: '<path d="M12 16V4m0 0l4 4m-4-4L8 8"/><path d="M4 20h16"/>',
  inbox: '<path d="M22 12h-6l-2 3H10l-2-3H2"/><path d="M5.45 5.11L2 12v6a2 2 0 002 2h16a2 2 0 002-2v-6l-3.45-6.89A2 2 0 0016.76 4H7.24a2 2 0 00-1.79 1.11z"/>',
  archive:
    '<path d="M21 8v13H3V8M1 3h22v5H1zM10 12h4"/>',
  pin: '<path d="M12 2v6m0 0l-3 3m3-3l3 3"/><circle cx="12" cy="14" r="6"/>',
  star:
    '<polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" fill="currentColor" stroke="none"/>',
  medal1: '<circle cx="12" cy="9" r="6"/><path d="M8.5 14.5L7 22l5-3 5 3-1.5-7.5"/>',
  offer: '<path d="M12 3l9 4.5v9L12 21l-9-4.5v-9L12 3z"/><path d="M12 12l9-4.5M12 12v9M12 12L3 7.5"/>',
  request: '<path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v15H6.5A2.5 2.5 0 004 19.5v-15A2.5 2.5 0 016.5 2z"/>',
  wave: '<path d="M4 12c2-4 4-4 6 0s4 4 6 0 4-4 6 0"/>',
  check: '<path d="M20 6L9 17l-5-5"/>',
  sun:
    '<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41"/>',
  moon: '<path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z"/>',
};

function icon(name, size = 20, className = "") {
  const paths = ICON_PATHS[name];
  if (!paths) return "";
  return `<svg class="ui-icon ${className}" width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${paths}</svg>`;
}

/** Colori esatti del logo PNG originale */
const LOGO_RGB = {
  skill: "#4FB5E6",
  yellow: "#FFD93D",
  orange: "#FF8C33",
};

/** Icona frecce (favicon) — cerchio giallo brand */
function logoSwapMark(size = 40) {
  return `<svg class="brand-mark" width="${size}" height="${size}" viewBox="0 0 40 40" aria-hidden="true">
    <circle cx="20" cy="20" r="20" fill="var(--brand-yellow, #f5c842)"/>
    <path d="M11 15h11M24 15l-3-2.5M24 15l-3 2.5" stroke="#0f1419" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
    <path d="M29 25H18M15 25l3-2.5M15 25l3 2.5" stroke="#0f1419" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
  </svg>`;
}

/** Mark login: puntino della «i» in Skill — frecce in palette */
function loginLogoMark(size = 22) {
  return `<svg class="brand-mark brand-mark--login" width="${size}" height="${size}" viewBox="0 0 40 40" aria-hidden="true">
    <circle cx="20" cy="20" r="20" fill="${LOGO_RGB.yellow}"/>
    <path class="brand-mark-arrow brand-mark-arrow--skill" d="M11 15h11M24 15l-3-2.5M24 15l-3 2.5" stroke="${LOGO_RGB.skill}" stroke-width="2.35" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
    <path class="brand-mark-arrow brand-mark-arrow--orange" d="M29 25H18M15 25l3-2.5M15 25l3 2.5" stroke="${LOGO_RGB.orange}" stroke-width="2.35" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
  </svg>`;
}

/** Wordmark SkillSwap integrato (non immagine raster) */
function logoWordmark(sizeClass = "") {
  return `<div class="brand-wordmark ${sizeClass}" role="img" aria-label="SkillSwap">
    <div class="brand-wordmark-line">
      <span class="brand-skill">Skill</span>
      ${logoSwapMark()}
      <span class="brand-swap">Swap</span>
    </div>
  </div>`;
}

function brandTagline() {
  return `<p class="brand-tagline">The future of <em>Education</em> starts here.</p>`;
}

/** Logo animato login — cerchio+frecce = puntino della «i» in Skill */
function loginAnimatedLogo() {
  return `<div class="login-logo-anim" role="img" aria-label="SkillSwap">
    <p class="login-logo-word">
      <span class="brand-skill">
        <span class="skill-chunk">Sk</span>
        <span class="skill-i">
          <span class="login-logo-mark">${loginLogoMark(22)}</span>
          <span class="skill-i-stem" aria-hidden="true">ı</span>
        </span>
        <span class="skill-chunk">ll</span>
      </span>
      <span class="brand-swap"><span class="brand-swap-s">S</span><span class="brand-swap-rest">wap</span></span>
    </p>
  </div>`;
}
