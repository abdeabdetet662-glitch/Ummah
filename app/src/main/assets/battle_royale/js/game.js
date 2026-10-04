/* ═══════════════════════════════════════════════
   Main Game Logic
   ═══════════════════════════════════════════════ */

let matchmakingHandle = null;

// ═══ INIT ═══
document.addEventListener('DOMContentLoaded', async () => {
  initEngine();
  setupJoystick();
  setupActionButtons();
  
  const ok = await initNetwork();
  if (!ok) return;
  
  // Load player info
  loadPlayerInfo();
  
  // Show lobby
  setTimeout(() => {
    document.getElementById('loading').classList.add('hidden');
    document.getElementById('lobby').classList.remove('hidden');
  }, 1500);
});

function loadPlayerInfo() {
  let playerName = 'عبد الله';
  let playerId = 'UMM-XXXX';
  let balance = 0;
  
  try {
    if (window.Android) {
      if (window.Android.getPlayerName) playerName = window.Android.getPlayerName();
      if (window.Android.getPlayerId) playerId = window.Android.getPlayerId();
      if (window.Android.getBalance) balance = window.Android.getBalance();
    }
  } catch (e) {}
  
  Network.userName = playerName;
  document.getElementById('my-name').textContent = playerName;
  document.getElementById('my-id').textContent = playerId;
  document.getElementById('my-balance').textContent = balance + ' Đ';
}

// ═══ MATCHMAKING ═══
async function findMatch() {
  document.getElementById('lobby').classList.add('hidden');
  document.getElementById('matchmaking').classList.remove('hidden');
  document.getElementById('mm-status').textContent = 'جاري البحث...';
  
  try {
    // Join queue
    await Network.joinQueue();
    Network.setupDisconnect();
    
    // Listen to queue
    let playerCount = 0;
    const updateQueue = (players) => {
      playerCount = players.length;
      document.getElementById('mm-count').textContent = players.length;
      document.getElementById('mm-players').innerHTML = players.map(p =>
        `<div class="mm-player-card ${p.userId === Network.userId ? 'me' : ''}">👤</div>`
      ).join('');
      
      if (players.length >= 3) { // Start with 3+ for testing
        setTimeout(() => startMatch(), 800);
      }
    };
    
    Network.listenQueue(updateQueue);
    
    // For testing: auto-start after 5s
    setTimeout(() => {
      if (playerCount < 3) {
        // Fill with bots
        startMatchWithBots();
      }
    }, 5000);
    
  } catch (e) {
    toast('❌ فشل البحث: ' + e.message);
    cancelMatchmaking();
  }
}

async function startMatch() {
  try {
    await Network.leaveQueue();
    await Network.findOrCreateMatch();
    
    // Show game
    document.getElementById('matchmaking').classList.add('hidden');
    document.getElementById('hud').classList.remove('hidden');
    document.getElementById('joystick').classList.remove('hidden');
    document.getElementById('actions').classList.remove('hidden');
    
    Game.state = 'playing';
    
    // Add me
    Game.me = new Player(Network.userId, Network.userName, true);
    Game.me.x = 300 + Math.random() * 300;
    Game.me.y = 300 + Math.random() * 300;
    Game.players[Network.userId] = Game.me;
    
    // Listen to other players
    Network.listenPlayers(players => {
      for (const p of players) {
        if (p.userId === Network.userId) continue;
        if (!Game.players[p.userId]) {
          Game.players[p.userId] = new Player(p.userId, p.userName, false);
        }
        const pl = Game.players[p.userId];
        pl.targetX = p.x;
        pl.targetY = p.y;
        pl.hp = p.hp;
        pl.alive = p.alive;
        pl.aimAngle = p.aimAngle || 0;
        pl.kills = p.kills || 0;
        if (!p.alive && pl.alive) {
          // Player died
          pl.alive = false;
          addKillFeed(`${p.userName} سقط`);
        }
      }
      
      // Check if I'm the last one
      const aliveCount = Object.values(Game.players).filter(p => p.alive).length;
      document.getElementById('hud-alive').textContent = aliveCount;
      
      if (aliveCount === 1 && Game.me.alive && Object.keys(Game.players).length >= 2) {
        onVictory();
      }
    });
    
    Network.listenBullets(b => {
      if (b.owner === Network.userId) return;
      // Add remote bullet
      Game.bullets.push({
        x: b.x,
        y: b.y,
        vx: b.vx,
        vy: b.vy,
        damage: b.damage,
        owner: b.owner,
        ownerName: b.ownerName,
        life: 1.5,
        color: '#FF4444',
        size: 3
      });
    });
    
    updateHealthUI();
    updateAmmoUI();
    
  } catch (e) {
    toast('❌ خطأ: ' + e.message);
  }
}

async function startMatchWithBots() {
  // For testing - add bots
  await startMatch();
  
  // Add 4 bots
  for (let i = 0; i < 4; i++) {
    const botId = 'bot_' + i;
    const bot = new Player(botId, 'Bot ' + (i + 1), false);
    bot.x = 400 + Math.random() * 800;
    bot.y = 400 + Math.random() * 600;
    bot.targetX = bot.x;
    bot.targetY = bot.y;
    Game.players[botId] = bot;
    
    // Simple bot AI
    setInterval(() => {
      if (!bot.alive || !Game.me || !Game.me.alive) return;
      bot.targetX = Game.me.x + (Math.random() - 0.5) * 200;
      bot.targetY = Game.me.y + (Math.random() - 0.5) * 200;
    }, 2000);
  }
}

function cancelMatchmaking() {
  Network.leaveQueue();
  document.getElementById('matchmaking').classList.add('hidden');
  document.getElementById('lobby').classList.remove('hidden');
}

// ═══ SHOOTING ═══
function setupActionButtons() {
  const shootBtn = document.getElementById('btn-shoot');
  const reloadBtn = document.getElementById('btn-reload');
  const weaponBtn = document.getElementById('btn-weapon');
  
  shootBtn.addEventListener('touchstart', e => {
    e.preventDefault();
    Game.input.shooting = true;
  }, { passive: false });
  shootBtn.addEventListener('touchend', e => {
    e.preventDefault();
    Game.input.shooting = false;
  }, { passive: false });
  shootBtn.addEventListener('mousedown', () => Game.input.shooting = true);
  shootBtn.addEventListener('mouseup', () => Game.input.shooting = false);
  
  reloadBtn.addEventListener('touchstart', e => {
    e.preventDefault();
    if (Game.me) Game.me.reload();
  }, { passive: false });
  
  weaponBtn.addEventListener('touchstart', e => {
    e.preventDefault();
    if (Game.me) {
      const w = Game.me.switchWeapon();
      document.getElementById('weapon-switch-icon').textContent = w.icon;
      document.getElementById('weapon-icon').textContent = w.icon;
    }
  }, { passive: false });
}

// ═══ JOYSTICK ═══
function setupJoystick() {
  const joy = document.getElementById('joystick');
  const base = joy.querySelector('.joystick-base');
  const knob = joy.querySelector('.joystick-knob');
  let touchId = null;
  let baseX = 0, baseY = 0;
  
  function start(e, t) {
    const rect = base.getBoundingClientRect();
    baseX = rect.left + rect.width / 2;
    baseY = rect.top + rect.height / 2;
    touchId = t.identifier;
    Game.input.joystick.active = true;
    move(t.clientX, t.clientY);
  }
  
  function move(x, y) {
    const dx = x - baseX;
    const dy = y - baseY;
    const dist = Math.sqrt(dx * dx + dy * dy);
    const max = 50;
    const clamp = Math.min(dist, max);
    const nx = dist > 0 ? dx / dist : 0;
    const ny = dist > 0 ? dy / dist : 0;
    
    knob.style.transform = `translate(calc(-50% + ${nx * clamp}px), calc(-50% + ${ny * clamp}px))`;
    
    Game.input.joystick.dx = nx * (clamp / max);
    Game.input.joystick.dy = ny * (clamp / max);
  }
  
  function end() {
    touchId = null;
    Game.input.joystick.active = false;
    Game.input.joystick.dx = 0;
    Game.input.joystick.dy = 0;
    knob.style.transform = 'translate(-50%, -50%)';
  }
  
  base.addEventListener('touchstart', e => { e.preventDefault(); start(e, e.changedTouches[0]); }, { passive: false });
  base.addEventListener('touchmove', e => {
    e.preventDefault();
    for (const t of e.changedTouches) if (t.identifier === touchId) move(t.clientX, t.clientY);
  }, { passive: false });
  base.addEventListener('touchend', e => {
    for (const t of e.changedTouches) if (t.identifier === touchId) end();
  });
  base.addEventListener('touchcancel', end);
}

// ═══ UI HELPERS ═══
function updateHealthUI() {
  if (!Game.me) return;
  const pct = Math.max(0, Game.me.hp / Game.me.maxHp);
  document.getElementById('health-fill').style.width = (pct * 100) + '%';
  document.getElementById('health-text').textContent = Math.max(0, Math.round(Game.me.hp));
}

function updateAmmoUI() {
  if (!Game.me) return;
  document.getElementById('ammo-current').textContent = Game.me.ammo;
  document.getElementById('ammo-max').textContent = Game.me.maxAmmo;
}

function addKillFeed(text) {
  const feed = document.getElementById('kill-feed');
  const div = document.createElement('div');
  div.className = 'kill-item';
  div.textContent = text;
  feed.appendChild(div);
  setTimeout(() => div.remove(), 4000);
}

function toast(text) {
  const el = document.getElementById('toast');
  el.textContent = text;
  el.classList.remove('hidden');
  clearTimeout(el._t);
  el._t = setTimeout(() => el.classList.add('hidden'), 2000);
}

// ═══ DEATH / VICTORY ═══
function onLocalDeath() {
  Game.state = 'dead';
  Game.input.shooting = false;
  
  document.getElementById('hud').classList.add('hidden');
  document.getElementById('joystick').classList.add('hidden');
  document.getElementById('actions').classList.add('hidden');
  document.getElementById('death').classList.remove('hidden');
  
  const aliveCount = Object.values(Game.players).filter(p => p.alive).length + 1;
  document.getElementById('death-sub').textContent = `المركز #${aliveCount} من ${Object.keys(Game.players).length}`;
  
  document.getElementById('death-stats').innerHTML = `
    <div class="result-stat">
      <div class="rs-icon">💀</div>
      <div class="rs-value">${Game.me.kills}</div>
      <div class="rs-label">قتلات</div>
    </div>
    <div class="result-stat">
      <div class="rs-icon">⏱️</div>
      <div class="rs-value">${Math.floor(Game.totalTime)}</div>
      <div class="rs-label">ثانية</div>
    </div>
    <div class="result-stat">
      <div class="rs-icon">💰</div>
      <div class="rs-value">+50</div>
      <div class="rs-label">Đ</div>
    </div>
  `;
  
  // Notify Android
  try {
    if (window.Android && window.Android.onGameEnd) {
      window.Android.onGameEnd(false, Game.me.kills, aliveCount);
    }
  } catch (e) {}
  
  Network.leaveMatch();
}

function onVictory() {
  Game.state = 'victory';
  Game.input.shooting = false;
  
  document.getElementById('hud').classList.add('hidden');
  document.getElementById('joystick').classList.add('hidden');
  document.getElementById('actions').classList.add('hidden');
  document.getElementById('victory').classList.remove('hidden');
  
  document.getElementById('victory-stats').innerHTML = `
    <div class="result-stat">
      <div class="rs-icon">💀</div>
      <div class="rs-value">${Game.me.kills}</div>
      <div class="rs-label">قتلات</div>
    </div>
    <div class="result-stat">
      <div class="rs-icon">⏱️</div>
      <div class="rs-value">${Math.floor(Game.totalTime)}</div>
      <div class="rs-label">ثانية</div>
    </div>
    <div class="result-stat">
      <div class="rs-icon">🏆</div>
      <div class="rs-value">+5000</div>
      <div class="rs-label">Đ</div>
    </div>
  `;
  
  try {
    if (window.Android && window.Android.onGameEnd) {
      window.Android.onGameEnd(true, Game.me.kills, 1);
    }
  } catch (e) {}
  
  Network.leaveMatch();
}

function backToLobby() {
  location.reload();
}

function exitGame() {
  try {
    if (window.Android && window.Android.onExit) window.Android.onExit();
  } catch (e) {}
}

function showLeaderboard() { toast('🏆 قريباً'); }
function showRules() { toast('📋 قريباً'); }

// ═══ EXPORT ═══
window.findMatch = findMatch;
window.cancelMatchmaking = cancelMatchmaking;
window.backToLobby = backToLobby;
window.exitGame = exitGame;
window.showLeaderboard = showLeaderboard;
window.showRules = showRules;
