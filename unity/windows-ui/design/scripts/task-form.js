// 일정 추가·수정 폼 — Figma Mobile_GUI "일정 추가"(MAIN_RE_01 프레임 10종) 기준
// 제목 · 중요 · 날짜(누르면 달력이 펼쳐짐) · 시간(켜면 시·분 휠) · 반복(없음/매일/매주/매월, 매주=요일, 매월=날짜) · 알림(없음/10분 전/30분 전/1시간 전)
window.TaskForm = (() => {
  const DAYS = "일월화수목금토";
  const REPEAT = ["없음", "매일", "매주", "매월"];
  const ALARM = ["없음", "10분 전", "30분 전", "1시간 전"];
  const ui = { open: null, calY: null, calM: null, fresh: null }; // 펼친 항목은 다시 그려도 유지
  const pad = n => String(n).padStart(2, "0");
  const fmtLong = d => { const x = new Date(d + "T00:00"); return `${x.getFullYear()}. ${pad(x.getMonth() + 1)}. ${pad(x.getDate())} (${DAYS[x.getDay()]})`; };
  const esc = s => String(s).replace(/[&<>"]/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]));
  const ic = n => `<svg class="i"><use href="#${n}"/></svg>`;

  // 필요한 아이콘을 한 번만 넣음
  if (!document.getElementById("tf-chev")) document.body.insertAdjacentHTML("afterbegin", `<svg width="0" height="0" style="position:absolute" aria-hidden="true"><defs>
    <symbol id="tf-chev" viewBox="0 0 24 24"><path d="m6 9 6 6 6-6"/></symbol>
    <symbol id="tf-up" viewBox="0 0 24 24"><path d="m6 15 6-6 6 6"/></symbol>
    <symbol id="tf-updown" viewBox="0 0 24 24"><path d="m8 10 4-4 4 4M8 14l4 4 4-4"/></symbol>
  </defs></svg>`);

  function norm(t) {
    if (!t.repeatDays) t.repeatDays = [];
    if (!ALARM.includes(t.alarm)) t.alarm = "없음";
    if (!REPEAT.includes(t.repeat)) t.repeat = "없음";
    return t;
  }

  function calHTML(selDate, multi) {
    const base = new Date((selDate || "2026-10-13") + "T00:00");
    if (ui.calY == null) { ui.calY = base.getFullYear(); ui.calM = base.getMonth(); }
    const y = ui.calY, m = ui.calM, first = new Date(y, m, 1), days = new Date(y, m + 1, 0).getDate();
    let cells = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"].map(d => `<span class="tf-dow">${d}</span>`).join("") + "<i></i>".repeat(first.getDay());
    for (let d = 1; d <= days; d++) {
      const key = `${y}-${pad(m + 1)}-${pad(d)}`;
      const on = multi ? multi.includes(d) : key === selDate;
      cells += `<button type="button" class="tf-d ${on ? "on" : ""} ${key === "2026-10-13" ? "today" : ""}" data-pick="${multi ? d : key}">${d}</button>`;
    }
    return `<div class="tf-cal"><div class="tf-cal-h"><b>${y !== 2026 ? y + "년 " : ""}${m + 1}월</b>
      <span><button type="button" class="tf-nav" data-cal="-1" aria-label="이전 달">${ic("tf-chev")}</button><button type="button" class="tf-nav next" data-cal="1" aria-label="다음 달">${ic("tf-chev")}</button></span></div>
      <div class="tf-cal-g">${cells}</div></div>`;
  }

  function wheel(kind, val, max, step) {
    const prev = (val - step + max) % max, next = (val + step) % max;
    return `<div class="tf-wheel" data-wheel="${kind}" tabindex="0" aria-label="${kind === "h" ? "시" : "분"} ${val}">
      <button type="button" data-step="-1" aria-label="이전">${pad(prev)}</button><b>${pad(val)}</b><button type="button" data-step="1" aria-label="다음">${pad(next)}</button></div>`;
  }

  function menu(key, list, cur) {
    return `<div class="tf-menu${ui.fresh === key ? " tf-new" : ""}" role="listbox">${list.map(o => `<button type="button" role="option" aria-selected="${o === cur}" data-opt="${key}" data-val="${o}">${o}</button>`).join("")}</div>`;
  }

  function html(t) {
    norm(t);
    const nw = k => ui.fresh === k ? " tf-new" : "";
    const [hh, mm] = (t.time || "18:00").split(":").map(Number);
    const repeatSub = t.repeat === "매주"
      ? `<div class="tf-sub tf-days${nw("weekly")}">${[...DAYS].map((d, i) => `<button type="button" class="tf-chip ${t.repeatDays.includes(i) ? "on" : ""}" data-day="${i}">${d}</button>`).join("")}</div>`
      : t.repeat === "매월" ? `<div class="tf-sub${nw("monthly")}">${calHTML(t.date, t.repeatDays.length ? t.repeatDays : [new Date(t.date + "T00:00").getDate()])}<div class="tf-hint">매월 반복할 날짜를 골라요</div></div>` : "";
    return `<input class="p-title" id="p-title" value="${esc(t.title)}" placeholder="일정 입력" aria-label="일정 이름">
      <div class="fcard"><div class="f">${ic("i-star")}중요<span class="v"><button type="button" class="box" id="p-imp" role="checkbox" aria-checked="${t.imp}" aria-label="중요">${ic("i-check")}</button></span></div></div>
      <div class="fcard tf">
        <div class="f tf-row ${ui.open === "date" ? "open" : ""}" data-row="date">${ic("i-cal")}날짜<span class="v"><button type="button" class="tf-val" data-toggle="date">${fmtLong(t.date)}</button></span></div>
        ${ui.open === "date" ? `<div class="tf-sub${nw("date")}">${calHTML(t.date)}</div>` : ""}
        <div class="f tf-row">${ic("i-clock")}시간<span class="v">${t.time ? `<button type="button" class="tf-val time" data-toggle="time">${t.time.replace(":", " : ")}</button>` : ""}<button type="button" class="switch" id="p-timesw" role="switch" aria-checked="${!!t.time}" aria-label="시간 지정"></button></span></div>
        ${t.time && ui.open === "time" ? `<div class="tf-sub tf-time${nw("time")}">${wheel("h", hh, 24, 1)}<span class="tf-colon">:</span>${wheel("m", mm, 60, 5)}</div>` : ""}
        <div class="f tf-row">${ic("i-repeat")}반복<span class="v tf-dd"><button type="button" class="tf-val dd" data-toggle="repeat">${t.repeat}${ic("tf-updown")}</button>${ui.open === "repeat" ? menu("repeat", REPEAT, t.repeat) : ""}</span></div>
        ${repeatSub}
        <div class="f tf-row">${ic("i-bell")}알림<span class="v tf-dd"><button type="button" class="tf-val dd" data-toggle="alarm">${t.alarm}${ic("tf-updown")}</button>${ui.open === "alarm" ? menu("alarm", ALARM, t.alarm) : ""}</span></div>
      </div>`;
  }

  // onChange(key, rerenderOnly): 값이 바뀌면 key, 펼침만 바뀌면 null
  function bind(root, t, onChange, isAdd) {
    norm(t);
    setTimeout(() => { ui.fresh = null; });
    const $ = s => root.querySelector(s);
    const set = (k, v) => { t[k] = v; onChange(k); };
    const redraw = () => onChange(null);
    const title = $("#p-title");
    title.oninput = () => { if (isAdd) { t.title = title.value; const ok = root.querySelector("#p-ok"); if (ok) ok.disabled = !t.title.trim(); } };
    title.onchange = () => { if (!isAdd) { const v = title.value.trim(); if (v) set("title", v); } };
    title.onkeydown = e => { if (e.key === "Enter") { if (isAdd) root.querySelector("#p-ok")?.click(); else title.blur(); } };
    $("#p-imp").onclick = () => set("imp", !t.imp);
    $("#p-timesw").onclick = () => { if (t.time) { set("time", ""); } else { ui.open = "time"; ui.fresh = "time"; set("time", "18:00"); } };
    root.querySelectorAll("[data-toggle]").forEach(b => b.onclick = e => {
      e.stopPropagation(); const k = b.dataset.toggle; ui.open = ui.open === k ? null : k; ui.fresh = ui.open;
      if (k === "date") { const x = new Date(t.date + "T00:00"); ui.calY = x.getFullYear(); ui.calM = x.getMonth(); }
      redraw();
    });
    root.querySelectorAll("[data-opt]").forEach(b => b.onclick = e => {
      e.stopPropagation(); const k = b.dataset.opt; ui.open = null;
      if (k === "repeat") { ui.fresh = b.dataset.val === "매주" ? "weekly" : b.dataset.val === "매월" ? "monthly" : null; t.repeatDays = b.dataset.val === "매주" ? [new Date(t.date + "T00:00").getDay()] : []; if (b.dataset.val === "매월") { const x = new Date(t.date + "T00:00"); ui.calY = x.getFullYear(); ui.calM = x.getMonth(); } }
      set(k, b.dataset.val);
    });
    root.querySelectorAll("[data-cal]").forEach(b => b.onclick = () => { ui.calM += +b.dataset.cal; if (ui.calM < 0) { ui.calM = 11; ui.calY--; } if (ui.calM > 11) { ui.calM = 0; ui.calY++; } redraw(); });
    root.querySelectorAll("[data-pick]").forEach(b => b.onclick = () => {
      const v = b.dataset.pick;
      if (/^\d+$/.test(v)) { const d = +v; t.repeatDays = t.repeatDays.includes(d) ? t.repeatDays.filter(x => x !== d) : [...t.repeatDays, d].sort((a, b) => a - b); onChange("repeat"); }
      else { ui.open = null; set("date", v); }
    });
    root.querySelectorAll("[data-day]").forEach(b => b.onclick = () => {
      const d = +b.dataset.day; t.repeatDays = t.repeatDays.includes(d) ? t.repeatDays.filter(x => x !== d) : [...t.repeatDays, d].sort();
      onChange("repeat");
    });
    root.querySelectorAll("[data-wheel]").forEach(w => {
      const kind = w.dataset.wheel;
      const step = dir => {
        let [h, m] = t.time.split(":").map(Number);
        if (kind === "h") h = (h + dir + 24) % 24; else m = (m + dir * 5 + 60) % 60;
        set("time", `${pad(h)}:${pad(m)}`);
        setTimeout(() => root.querySelector(`[data-wheel="${kind}"]`)?.focus());
      };
      w.querySelectorAll("[data-step]").forEach(b => b.onclick = () => step(+b.dataset.step));
      w.onwheel = e => { e.preventDefault(); step(e.deltaY > 0 ? 1 : -1); };
      w.onkeydown = e => { if (e.key === "ArrowDown") { e.preventDefault(); step(1); } if (e.key === "ArrowUp") { e.preventDefault(); step(-1); } };
    });
  }

  function reset() { ui.open = null; ui.calY = null; ui.calM = null; }
  // 바깥을 누르면 펼친 메뉴 닫기
  document.addEventListener("click", e => { if ((ui.open === "repeat" || ui.open === "alarm") && !e.target.closest(".tf-dd")) { ui.open = null; document.dispatchEvent(new CustomEvent("taskform:redraw")); } });

  const summary = t => {
    norm(t);
    if (t.repeat === "매주" && t.repeatDays.length) return `매주 ${t.repeatDays.map(i => DAYS[i]).join("·")}`;
    if (t.repeat === "매월" && t.repeatDays.length) return `매월 ${t.repeatDays.join("·")}일`;
    return t.repeat;
  };
  return { html, bind, reset, summary, ui };
})();
