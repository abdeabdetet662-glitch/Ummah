/* ═══════════════════════════════════════════════
   Investigation + Voting
   ═══════════════════════════════════════════════ */

// ═══ Show Briefing ═══
function showBriefing() {
  Game.state = STATE.BRIEFING;
  
  const mission = Game.currentMission;
  document.getElementById('briefing-mission').textContent = mission.icon + ' ' + mission.name;
  document.getElementById('briefing-story').textContent = mission.story;
  
  const objList = document.getElementById('briefing-objectives');
  objList.innerHTML = '';
  mission.objectives.forEach(o => {
    const div = document.createElement('div');
    div.className = 'obj-item';
    div.textContent = o;
    objList.appendChild(div);
  });
  
  // Agents
  const agentsList = document.getElementById('briefing-agents');
  agentsList.innerHTML = '';
  
  // Player is index 0 (random index)
  Game.playerIndex = Math.floor(Math.random() * AGENTS.length);
  Game.agents = [...AGENTS];
  
  // Shuffle indices
  for (let i = 0; i < Game.agents.length; i++) {
    const a = Game.agents[i];
    const card = document.createElement('div');
    card.className = 'agent-card';
    card.innerHTML = `
      <div class="agent-emoji">${a.emoji}</div>
      <div class="agent-name">${a.name}${i === Game.playerIndex ? ' (أنت)' : ''}</div>
    `;
    agentsList.appendChild(card);
  }
  
  document.getElementById('briefing').classList.remove('hidden');
}

// ═══ Load Mission ═══
function loadMission() {
  Game.currentMission = MISSIONS[Math.floor(Math.random() * MISSIONS.length)];
  
  // Determine mole (random, not player)
  do {
    Game.moleIndex = Math.floor(Math.random() * AGENTS.length);
  } while (Game.moleIndex === Game.playerIndex);
}

// ═══ Start Investigation Phase ═══
function startInvestigation() {
  Game.state = STATE.INVESTIGATION;
  
  document.getElementById('hud').classList.add('hidden');
  document.getElementById('joystick').classList.add('hidden');
  document.getElementById('actions').classList.add('hidden');
  document.getElementById('investigation').classList.remove('hidden');
  
  // Reset UI
  document.getElementById('inv-chat').innerHTML = '';
  document.getElementById('inv-timer').textContent = '04:00';
  
  // Render agents
  const agentsBox = document.getElementById('inv-agents');
  agentsBox.innerHTML = '';
  
  Game.agents.forEach((a, i) => {
    if (i === Game.playerIndex) return; // skip player
    const card = document.createElement('div');
    card.className = 'inv-agent';
    card.dataset.index = i;
    card.innerHTML = `
      <span class="ag-emoji">${a.emoji}</span>
      <span class="ag-name">${a.name}</span>
      <span class="ag-suspicion" id="susp-${i}"></span>
    `;
    card.addEventListener('click', () => selectAgent(i));
    agentsBox.appendChild(card);
  });
  
  // Render questions
  const questionsBox = document.getElementById('inv-questions');
  questionsBox.innerHTML = '';
  QUESTIONS.forEach(q => {
    const btn = document.createElement('button');
    btn.className = 'inv-q-btn';
    btn.textContent = q.emoji + ' ' + q.text;
    btn.addEventListener('click', () => askQuestion(q));
    questionsBox.appendChild(btn);
  });
  
  // Start timer
  startInvTimer();
}

let invTimerHandle = null;
let invTimeLeft = 240;

function startInvTimer() {
  invTimeLeft = 240;
  if (invTimerHandle) clearInterval(invTimerHandle);
  
  invTimerHandle = setInterval(() => {
    invTimeLeft--;
    const m = Math.floor(invTimeLeft / 60);
    const s = invTimeLeft % 60;
    document.getElementById('inv-timer').textContent = 
      String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
    
    if (invTimeLeft <= 0) {
      clearInterval(invTimerHandle);
      startVoting();
    }
  }, 1000);
}

let selectedAgentIdx = -1;

function selectAgent(idx) {
  selectedAgentIdx = idx;
  document.querySelectorAll('.inv-agent').forEach(el => el.classList.remove('selected'));
  document.querySelector(`.inv-agent[data-index="${idx}"]`).classList.add('selected');
}

function askQuestion(q) {
  if (selectedAgentIdx < 0) {
    toast('⚠️ اختر عميلاً أولاً');
    return;
  }
  
  const agent = Game.agents[selectedAgentIdx];
  const isMole = selectedAgentIdx === Game.moleIndex;
  const answers = AGENT_ANSWERS[agent.name];
  
  // Add question to chat
  addChatMsg(q.emoji + ' ' + q.text, 'question', 'أنت');
  
  // Agent thinks...
  setTimeout(() => {
    let answer;
    if (isMole) {
      // 70% chance answer lie, 30% truth (to be more subtle)
      answer = Math.random() < 0.7 ? answers.lies[q.id] : answers.truths[q.id];
    } else {
      // Truthful agent: 90% truth, 10% "I don't know"
      answer = Math.random() < 0.9 ? answers.truths[q.id] : 'لا أتذكر بالضبط';
    }
    
    addChatMsg(agent.emoji + ' ' + answer, 'answer', agent.name);
  }, 800);
}

function addChatMsg(text, type, name) {
  const chat = document.getElementById('inv-chat');
  const div = document.createElement('div');
  div.className = 'chat-msg ' + type;
  div.innerHTML = `<div class="msg-name">${name}</div>${text}`;
  chat.appendChild(div);
  chat.scrollTop = chat.scrollHeight;
}

// ═══ Voting ═══
function startVoting() {
  Game.state = STATE.VOTING;
  
  if (invTimerHandle) clearInterval(invTimerHandle);
  
  document.getElementById('investigation').classList.add('hidden');
  document.getElementById('voting').classList.remove('hidden');
  
  const box = document.getElementById('vote-agents');
  box.innerHTML = '';
  
  Game.agents.forEach((a, i) => {
    if (i === Game.playerIndex) return;
    const card = document.createElement('div');
    card.className = 'vote-agent';
    card.dataset.index = i;
    card.innerHTML = `
      <span class="vt-emoji">${a.emoji}</span>
      <span class="vt-name">${a.name}</span>
    `;
    card.addEventListener('click', () => castVote(i));
    box.appendChild(card);
  });
}

let hasVoted = false;

function castVote(idx) {
  if (hasVoted) {
    toast('⚠️ صوّتت من قبل');
    return;
  }
  hasVoted = true;
  
  document.querySelectorAll('.vote-agent').forEach(el => el.classList.remove('selected'));
  document.querySelector(`.vote-agent[data-index="${idx}"]`).classList.add('selected');
  
  const agent = Game.agents[idx];
  document.getElementById('vote-status').textContent = 
    `✅ صوّتت لـ ${agent.name}`;
  
  // Check result
  setTimeout(() => {
    const correct = idx === Game.moleIndex;
    showResult(correct);
  }, 1500);
}

// ═══ Result ═══
function showResult(caught) {
  Game.state = STATE.RESULT;
  
  document.getElementById('voting').classList.add('hidden');
  document.getElementById('result').classList.remove('hidden');
  
  const icon = document.getElementById('result-icon');
  const title = document.getElementById('result-title');
  const sub = document.getElementById('result-sub');
  const reveal = document.getElementById('result-reveal');
  const stats = document.getElementById('result-stats');
  
  const mole = Game.agents[Game.moleIndex];
  
  if (caught) {
    icon.textContent = '🏆';
    title.textContent = 'فوز!';
    title.className = 'result-title win';
    sub.textContent = 'اكتشفت الخائن!';
  } else {
    icon.textContent = '💀';
    title.textContent = 'خسارة';
    title.className = 'result-title lose';
    sub.textContent = 'الخائن هرب!';
  }
  
  reveal.innerHTML = `
    🎭 الخائن كان: <b>${mole.emoji} ${mole.name}</b><br>
    <span style="color:var(--text-dim);font-size:13px">
      التخصص: ${mole.specialty}
    </span>
  `;
  
  stats.innerHTML = `
    <div class="stat-box">
      <div class="stat-label">💀 الأعداء</div>
      <div class="stat-value">${Game.kills}/${Game.killsTarget}</div>
    </div>
    <div class="stat-box">
      <div class="stat-label">⏱️ الوقت</div>
      <div class="stat-value">${formatTime(240 - Game.missionTime)}</div>
    </div>
    <div class="stat-box">
      <div class="stat-label">${caught ? '✅ اكتشاف' : '❌ فشل'}</div>
      <div class="stat-value">${caught ? '+5000 Đ' : '+500 Đ'}</div>
    </div>
    <div class="stat-box">
      <div class="stat-label">🏅 النتيجة</div>
      <div class="stat-value">${caught ? 'ممتاز' : 'حاول مجدداً'}</div>
    </div>
  `;
  
  // Send result to Android
  try {
    if (window.Android && window.Android.onMissionEnd) {
      window.Android.onMissionEnd(caught, Game.kills, mole.name);
    }
  } catch (e) {}
}

function formatTime(sec) {
  const m = Math.floor(sec / 60);
  const s = Math.floor(sec % 60);
  return String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
}

function endGame(win, reason) {
  showResult(win);
}

function exitGame() {
  try {
    if (window.Android && window.Android.onExit) {
      window.Android.onExit();
    }
  } catch (e) {}
}
