// 윈도우 앱 디자인 시안 공용 스크립트 — 화면 진입/전환 애니메이션, 알림 플라이아웃, 작은 도우미
// 각 화면의 인라인 스크립트 뒤에 불러온다.
window.App = (() => {
  const reduce = matchMedia("(prefers-reduced-motion: reduce)").matches;
  const body = document.body;

  // 클래스를 다시 붙여 애니메이션을 한 번 재생
  function anim(target, cls, root = document) {
    const el = typeof target === "string" ? root.querySelector(target) : target;
    if (!el) return;
    el.classList.remove(cls); void el.offsetWidth; el.classList.add(cls);
    el.addEventListener("animationend", () => el.classList.remove(cls), { once: true });
  }

  // 목록 항목에 순서값(--i)을 매겨 차례로 등장
  function stagger(root = document) {
    root.querySelectorAll(".cards, .grid4, .grid, .stagger").forEach(c =>
      [...c.children].forEach((ch, i) => ch.style.setProperty("--i", Math.min(i, 14))));
  }

  // 화면 진입
  body.classList.add("intro");
  stagger();
  setTimeout(() => body.classList.remove("intro"), 1100);

  // 화면 이동 시 살짝 사라지기
  document.addEventListener("click", e => {
    const a = e.target.closest("a[href]");
    if (!a || a.target || e.ctrlKey || e.metaKey || a.getAttribute("href").startsWith("#")) return;
    e.preventDefault();
    body.classList.add("leaving");
    setTimeout(() => { location.href = a.href; }, reduce ? 0 : 170);
  });
  window.addEventListener("pageshow", () => body.classList.remove("leaving"));

  // ---------------- 알림 플라이아웃 (Figma Mobile_GUI 알림 화면 내용 + 윈도우용 정리) ----------------
  document.body.insertAdjacentHTML("afterbegin", `<svg width="0" height="0" style="position:absolute" aria-hidden="true"><defs>
    <symbol id="ni-sys" viewBox="0 0 24 24"><path d="M10 5a2 2 0 1 1 4 0 7 7 0 0 1 4 6v3a4 4 0 0 0 2 3H4a4 4 0 0 0 2-3v-3a7 7 0 0 1 4-6"/><path d="M9 17v1a3 3 0 0 0 6 0v-1"/></symbol>
    <symbol id="ni-evt" viewBox="0 0 24 24"><rect x="3" y="8" width="18" height="4" rx="1"/><path d="M12 8v13M19 12v7a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-7M7.5 8a2.5 2.5 0 0 1 0-5C10 3 12 8 12 8s2-5 4.5-5a2.5 2.5 0 0 1 0 5"/></symbol>
    <symbol id="ni-forest" viewBox="0 0 24 24"><path d="M5 21c.5-4.5 2.5-8 7-10M9 18c6.2 0 10.2-4.2 11-12-2.1-.5-4.6-.4-7 .5-4 1.5-6 5-5.5 9.5"/></symbol>
    <symbol id="ni-gear" viewBox="0 0 24 24"><path d="M10.3 4.3c.4-1.8 3-1.8 3.4 0a1.7 1.7 0 0 0 2.6 1.1c1.5-.9 3.3.8 2.4 2.4a1.7 1.7 0 0 0 1 2.5c1.9.5 1.9 3.1 0 3.5a1.7 1.7 0 0 0-1 2.6c.9 1.5-.9 3.3-2.4 2.4a1.7 1.7 0 0 0-2.6 1c-.4 1.9-3 1.9-3.4 0a1.7 1.7 0 0 0-2.6-1c-1.5.9-3.3-.9-2.4-2.4a1.7 1.7 0 0 0-1-2.6c-1.9-.4-1.9-3 0-3.5a1.7 1.7 0 0 0 1-2.5c-.9-1.6.9-3.3 2.4-2.4 1 .6 2.3.1 2.6-1.1z"/><circle cx="12" cy="12" r="3"/></symbol>
    <symbol id="ni-x" viewBox="0 0 24 24"><path d="M18 6 6 18M6 6l12 12"/></symbol>
    <symbol id="ni-chev" viewBox="0 0 24 24"><path d="m9 6 6 6-6 6"/></symbol>
  </defs></svg>`);
  const NOTI = [
    { id: 1, cls: "sys", when: "1시간 전", msg: "오늘 마감인 할 일이 3개 남아있어요. 차근차근 끝내볼까요? 💪", unread: true, href: "home-v4.html" },
    { id: 2, cls: "evt", when: "1시간 전", msg: "🎄✨ Merry Christmas ✨🎄<br>7일 연속 클리어하고 크리스마스 한정 정령을 만나봐요!", unread: true, href: "settings-v4.html#notice" },
    { id: 3, cls: "forest", when: "1시간 전", msg: "새로운 정령을 발견했어요!", unread: true, href: "dex-v4.html" },
    { id: 4, cls: "sys", when: "1일 전", msg: "중요한 일정이 아직 완료되지 않았어요...🥹", href: "home-v4.html" },
    { id: 5, cls: "forest", when: "2일 전", msg: "루미가 모험을 마치고 돌아왔어요!<br>어떤 보상을 가져왔는지 확인하러 가보세요.", href: "forest-v4.html" },
    { id: 6, cls: "evt", when: "2주 전", msg: "[EVENT] 루틴 5연속 성공 시 특성 경험치가 2배 !!", href: "settings-v4.html#notice" },
    { id: 7, cls: "forest", when: "1달 전", msg: "새로운 정령을 발견했어요!", href: "dex-v4.html" },
  ];
  const CAT = { sys: "시스템", evt: "이벤트", forest: "정령의숲" };
  const win = document.querySelector(".window");
  const bell = document.querySelector('.iconbtn[aria-label="알림"]');
  if (win && bell) {
    let filter = "all", bannerOff = false;
    const scrim = document.createElement("div"); scrim.className = "fly-scrim";
    const fly = document.createElement("aside"); fly.className = "flyout"; fly.setAttribute("aria-label", "알림");
    const ico = id => `<svg class="i"><use href="#${id}"/></svg>`;
    const item = (n, i) => `<div class="noti ${n.unread ? "unread" : "old"} ${n.cls}" style="--i:${i}" data-id="${n.id}" role="button" tabindex="0">
        <span class="noti-ic">${ico("ni-" + n.cls)}</span>
        <span class="noti-body"><span class="top"><span class="cat">${CAT[n.cls]}</span><span class="when">${n.when}</span></span>
        <span class="msg">${n.msg}</span></span>
        <button class="noti-x" data-del="${n.id}" aria-label="알림 지우기" title="지우기">${ico("ni-x")}</button></div>`;
    const renderFly = () => {
      const list = NOTI.filter(n => filter === "all" || n.cls === filter);
      const fresh = list.filter(n => n.unread), old = list.filter(n => !n.unread);
      const unreadAll = NOTI.filter(n => n.unread).length;
      const tab = (k, label) => `<button class="fly-tab" data-f="${k}" aria-pressed="${filter === k}">${label}${k !== "all" && NOTI.some(n => n.cls === k && n.unread) ? `<i></i>` : ""}</button>`;
      fly.innerHTML = `
        <div class="fly-h"><span class="grow">알림${unreadAll ? `<span class="fly-count">${unreadAll}</span>` : ""}</span>
          ${unreadAll ? `<button class="readall" id="fly-readall">모두 읽음</button>` : ""}
          <a class="iconbtn" href="settings-v4.html#notify" aria-label="알림 설정" title="알림 설정">${ico("ni-gear")}</a>
          <button class="iconbtn" id="fly-close" aria-label="닫기">${ico("ni-x")}</button></div>
        <div class="fly-tabs">${tab("all", "전체")}${tab("sys", "시스템")}${tab("evt", "이벤트")}${tab("forest", "정령의숲")}</div>
        <div class="fly-body">
          ${bannerOff ? "" : `<div class="fly-banner"><a href="settings-v4.html#notify">${ico("ni-sys")}<span class="grow">알림을 설정하고 중요한 플랜을 놓치지 마세요!</span>${ico("ni-chev")}</a><button id="fly-banner-x" aria-label="안내 닫기">${ico("ni-x")}</button></div>`}
          ${fresh.length ? `<div class="fly-sec">새로운 알림</div>${fresh.map(item).join("")}` : ""}
          ${old.length ? `<div class="fly-sec">지난 알림</div>${old.map((n, i) => item(n, i + fresh.length)).join("")}` : ""}
          ${list.length ? "" : `<div class="fly-empty"><img class="float" src="assets/spirit_neutral.png" alt="">${filter === "all" ? "알림이 없어요" : CAT[filter] + " 알림이 없어요"}</div>`}
        </div>`;
      const badge = bell.querySelector(".badge");
      if (badge) { badge.style.display = unreadAll ? "" : "none"; }
    };
    renderFly();
    win.append(scrim, fly);
    const open = () => { renderFly(); fly.classList.add("open"); scrim.classList.add("open"); bell.setAttribute("aria-expanded", "true"); };
    const close = () => { fly.classList.remove("open"); scrim.classList.remove("open"); bell.setAttribute("aria-expanded", "false"); };
    bell.addEventListener("click", () => fly.classList.contains("open") ? close() : open());
    scrim.addEventListener("click", close);
    const go = href => { body.classList.add("leaving"); setTimeout(() => location.href = href, reduce ? 0 : 170); };
    fly.addEventListener("click", e => {
      if (e.target.closest("#fly-close")) return close();
      if (e.target.closest("#fly-readall")) { NOTI.forEach(n => n.unread = false); renderFly(); return; }
      if (e.target.closest("#fly-banner-x")) { bannerOff = true; renderFly(); return; }
      const t = e.target.closest(".fly-tab"); if (t) { filter = t.dataset.f; renderFly(); return; }
      const del = e.target.closest("[data-del]");
      if (del) {
        const el = del.closest(".noti"), idx = NOTI.findIndex(n => n.id === +del.dataset.del);
        el.classList.add("removing");
        setTimeout(() => { NOTI.splice(idx, 1); renderFly(); }, reduce ? 0 : 220);
        return;
      }
      const n = e.target.closest(".noti");
      if (n) { const d = NOTI.find(x => x.id === +n.dataset.id); d.unread = false; if (d.href) go(d.href); else renderFly(); }
    });
    fly.addEventListener("keydown", e => { if (e.key === "Enter" && e.target.classList.contains("noti")) e.target.click(); });
    document.addEventListener("keydown", e => { if (e.key === "Escape" && fly.classList.contains("open")) close(); });
  }

  // ---------------- 테마 (라이트 / 다크 / 시스템, 대비 높이기) ----------------
  const root = document.documentElement;
  const mq = matchMedia("(prefers-color-scheme: dark)");
  const theme = {
    get: () => localStorage.getItem("tws-theme") || "light",
    contrast: () => localStorage.getItem("tws-contrast") !== "normal", // 기본: 대비 높임
    apply() {
      const t = theme.get(), dark = t === "dark" || (t === "system" && mq.matches);
      root.classList.add("theming");
      root.dataset.theme = dark ? "dark" : "light";
      if (theme.contrast()) root.dataset.contrast = "more"; else delete root.dataset.contrast;
      setTimeout(() => root.classList.remove("theming"), 350);
      const tb = document.getElementById("tb-theme");
      if (tb) { tb.innerHTML = `<svg class="i"><use href="#${dark ? "tbi-sun" : "tbi-moon"}"/></svg>`; tb.title = dark ? "라이트 모드로" : "다크 모드로"; }
    },
    set(t) { localStorage.setItem("tws-theme", t); theme.apply(); },
    setContrast(on) { localStorage.setItem("tws-contrast", on ? "more" : "normal"); theme.apply(); },
  };
  mq.addEventListener("change", () => theme.get() === "system" && theme.apply());

  // 타이틀바 아이콘 정의 (각 화면의 아이콘 세트와 별개)
  document.body.insertAdjacentHTML("afterbegin", `<svg width="0" height="0" style="position:absolute" aria-hidden="true"><defs>
    <symbol id="tbi-menu" viewBox="0 0 24 24"><path d="M4 7h16M4 12h10M4 17h16"/></symbol>
    <symbol id="tbi-side" viewBox="0 0 24 24"><rect x="3" y="4.5" width="18" height="15" rx="3"/><path d="M9 4.5v15"/></symbol>
    <symbol id="tbi-moon" viewBox="0 0 24 24"><path d="M20 14.5A8.5 8.5 0 0 1 9.5 4a8.5 8.5 0 1 0 10.5 10.5z"/></symbol>
    <symbol id="tbi-sun" viewBox="0 0 24 24"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></symbol>
  </defs></svg>`);

  const titlebar = document.querySelector(".titlebar");
  const nav = document.querySelector(".nav");
  const shell = document.querySelector(".shell");
  if (titlebar) {
    const app = titlebar.querySelector(".app"), ctl = titlebar.querySelector(".winctl");
    const left = document.createElement("div"); left.className = "tb-left";
    if (nav) left.innerHTML = `<button class="tb-btn" id="tb-menu" aria-label="메뉴 열기" title="메뉴"><svg class="i"><use href="#tbi-menu"/></svg></button>
      <button class="tb-btn" id="tb-side" aria-label="사이드바 고정" title="사이드바 고정/숨기기 (Ctrl+B)"><svg class="i"><use href="#tbi-side"/></svg></button>`;
    titlebar.insertBefore(left, app); left.appendChild(app);
    const right = document.createElement("div"); right.className = "tb-right";
    right.innerHTML = `<button class="tb-btn" id="tb-theme" aria-label="테마 바꾸기"></button>`;
    titlebar.insertBefore(right, ctl); right.appendChild(ctl);
    document.getElementById("tb-theme").onclick = () => theme.set(root.dataset.theme === "dark" ? "light" : "dark");
  }
  theme.apply();

  // ---------------- 왼쪽 패널: 고정 / 숨김(호버·메뉴 버튼으로 잠깐 보기) ----------------
  let narrow = false, narrowPin = false, peekSticky = false, peekTimer;
  const pinned = () => narrow ? narrowPin : localStorage.getItem("tws-nav") !== "off";
  function applyNav() {
    if (!win || !nav) return;
    const on = pinned();
    win.classList.toggle("nav-off", !on);
    if (on) win.classList.remove("nav-peek");
    const b = document.getElementById("tb-side");
    if (b) { b.setAttribute("aria-pressed", on); b.title = on ? "사이드바 숨기기 (Ctrl+B)" : "사이드바 고정 (Ctrl+B)"; }
  }
  function peek(on, sticky) {
    if (!win || !nav || pinned()) return;
    clearTimeout(peekTimer);
    peekSticky = on && !!sticky;
    win.classList.toggle("nav-peek", on);
  }
  function togglePin() {
    if (narrow) narrowPin = !narrowPin; else localStorage.setItem("tws-nav", pinned() ? "off" : "on");
    applyNav();
  }
  if (win && nav && shell) {
    const edge = document.createElement("div"); edge.className = "nav-edge"; shell.prepend(edge);
    edge.addEventListener("mouseenter", () => peek(true));
    nav.addEventListener("mouseenter", () => clearTimeout(peekTimer));
    nav.addEventListener("mouseleave", () => { if (!peekSticky) peekTimer = setTimeout(() => peek(false), 220); });
    document.getElementById("tb-menu").onclick = e => { e.stopPropagation(); if (pinned()) { togglePin(); } else peek(!win.classList.contains("nav-peek"), true); };
    document.getElementById("tb-side").onclick = togglePin;
    document.addEventListener("click", e => { if (peekSticky && !e.target.closest(".nav")) peek(false); });
    document.addEventListener("keydown", e => {
      if (e.ctrlKey && e.key.toLowerCase() === "b") { e.preventDefault(); togglePin(); }
      if (e.key === "Escape" && win.classList.contains("nav-peek")) peek(false);
    });
  }

  // ---------------- 창 크기: 미리보기 막대 + 너비별 클래스 ----------------
  const SIZES = [[1440, 900], [1280, 800], [1100, 720], [960, 640]];
  if (win) {
    const saved = (localStorage.getItem("tws-size") || "").split("x").map(Number);
    if (saved[0]) { win.style.setProperty("--win-w", saved[0] + "px"); win.style.setProperty("--win-h", saved[1] + "px"); }
    const bar = document.createElement("div"); bar.className = "sizebar"; bar.setAttribute("aria-label", "창 크기 미리보기");
    bar.innerHTML = `<b>창 크기</b>${SIZES.map(([w, h]) => `<button data-w="${w}" data-h="${h}">${w}×${h}</button>`).join("")}<span class="now" id="size-now"></span>`;
    document.body.prepend(bar);
    bar.addEventListener("click", e => {
      const b = e.target.closest("button[data-w]"); if (!b) return;
      win.style.width = win.style.height = "";
      win.style.setProperty("--win-w", b.dataset.w + "px"); win.style.setProperty("--win-h", b.dataset.h + "px");
      localStorage.setItem("tws-size", `${b.dataset.w}x${b.dataset.h}`);
    });
    const sizeClasses = () => {
      const w = Math.round(win.offsetWidth), h = Math.round(win.offsetHeight);
      const wasNarrow = narrow;
      narrow = w < 1100;
      win.classList.toggle("w-narrow", narrow);
      win.classList.toggle("w-mid", !narrow && w < 1280);
      if (narrow !== wasNarrow) { narrowPin = false; applyNav(); }
      document.getElementById("size-now").textContent = `현재 ${w}×${h}`;
      bar.querySelectorAll("button[data-w]").forEach(b => b.setAttribute("aria-pressed", +b.dataset.w === w && +b.dataset.h === h));
    };
    // 첫 화면: 저장된 창 크기에 맞는 배치를 바로 적용하고, 그 순간에는 사이드바가 접히는 전환을 보이지 않는다
    win.classList.add("preload");
    sizeClasses(); applyNav();
    document.documentElement.classList.remove("booting");
    requestAnimationFrame(() => requestAnimationFrame(() => win.classList.remove("preload")));
    new ResizeObserver(sizeClasses).observe(win);
  }

  // ---------------- 작은 디테일 ----------------
  // 아이콘 버튼에 툴팁(이름) 붙이기
  document.querySelectorAll(".iconbtn[aria-label], .roundbtn[aria-label], .winctl button[aria-label]").forEach(b => { if (!b.title) b.title = b.getAttribute("aria-label"); });
  // 창 조작 버튼: 최소화·최대화·닫기 (시안용 동작)
  if (win) {
    const [minB, maxB, closeB] = win.querySelectorAll(".winctl button");
    const toggleMax = () => { win.classList.toggle("maxed"); if (maxB) maxB.title = win.classList.contains("maxed") ? "이전 크기로" : "최대화"; };
    maxB && maxB.addEventListener("click", toggleMax);
    titlebar && titlebar.addEventListener("dblclick", e => { if (!e.target.closest("button")) toggleMax(); });
    const hide = msg => { win.classList.add("minimizing"); setTimeout(() => { win.classList.remove("minimizing"); toastMsg(msg); }, reduce ? 0 : 300); };
    minB && minB.addEventListener("click", () => hide("작업 표시줄로 내렸어요 (시안)"));
    closeB && closeB.addEventListener("click", () => hide("트레이에서 계속 알려드릴게요 (시안)"));
  }
  function toastMsg(text) {
    const t = document.getElementById("toast"), tt = document.getElementById("toast-t");
    if (!t || !tt) return;
    tt.textContent = text; const u = document.getElementById("toast-undo"); if (u) u.hidden = true;
    t.classList.remove("open"); void t.offsetWidth; t.classList.add("open");
    clearTimeout(toastMsg.timer); toastMsg.timer = setTimeout(() => t.classList.remove("open"), 2600);
  }
  // ? 키: 단축키 안내
  document.addEventListener("keydown", e => {
    if (e.key !== "?" || /INPUT|TEXTAREA|SELECT/.test(document.activeElement.tagName) || document.querySelector(".modal-scrim")) return;
    const host = win || document.body, sc = document.createElement("div"); sc.className = "modal-scrim open";
    const row = (k, d) => `<dt>${k.split("+").map(x => `<kbd>${x}</kbd>`).join("")}</dt><dd>${d}</dd>`;
    sc.innerHTML = `<div class="modal keys" role="dialog" aria-modal="true"><b>단축키</b><dl>
      ${row("Ctrl+N", "일정 추가")}${row("Ctrl+F", "검색")}${row("Ctrl+B", "사이드바 고정/숨기기")}
      ${row("Space", "선택한 일정 완료")}${row("Enter", "선택한 일정 자세히")}${row("Delete", "선택한 일정 삭제")}
      ${row("W+M+Y", "캘린더 주간 · 월간 · 연간")}${row("PageUp+PageDown", "캘린더 이전 · 다음")}${row("Esc", "패널·메뉴 닫기")}</dl>
      <div class="row one"><button data-r="1" class="primary">확인</button></div></div>`;
    sc.querySelectorAll("dt").forEach(dt => { if (dt.textContent === "WMY") dt.innerHTML = "<kbd>W</kbd><kbd>M</kbd><kbd>Y</kbd>"; if (dt.textContent === "PageUpPageDown") dt.innerHTML = "<kbd>PgUp</kbd><kbd>PgDn</kbd>"; });
    host.appendChild(sc);
    const close = () => sc.remove();
    sc.addEventListener("click", ev => { if (ev.target === sc || ev.target.closest("[data-r]")) close(); });
    sc.addEventListener("keydown", ev => { if (ev.key === "Escape") close(); });
    sc.querySelector("button").focus();
  });

  // ---------------- 모달 ----------------
  function modal({ title, text = "", ok = "확인", cancel = null, danger = false, tone = null }) {
    return new Promise(res => {
      const host = document.querySelector(".window") || document.body;
      const sc = document.createElement("div"); sc.className = "modal-scrim open";
      sc.innerHTML = `<div class="modal" role="alertdialog" aria-modal="true"><b>${title}</b>${text ? `<p>${text}</p>` : ""}
        <div class="row ${cancel ? "" : "one"}">${cancel ? `<button data-r="0">${cancel}</button>` : ""}<button data-r="1" class="${tone || (danger ? "danger" : "primary")}">${ok}</button></div></div>`;
      host.appendChild(sc);
      const done = v => { sc.remove(); res(v); };
      sc.addEventListener("click", e => { const b = e.target.closest("[data-r]"); if (b) done(b.dataset.r === "1"); else if (e.target === sc && cancel) done(false); });
      sc.querySelector('[data-r="1"]').focus();
      sc.addEventListener("keydown", e => { if (e.key === "Escape") done(false); });
    });
  }

  return { anim, stagger, reduce, theme, modal };
})();
