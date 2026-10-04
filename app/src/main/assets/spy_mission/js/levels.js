/* ═══════════════════════════════════════════════
   بناء المستويات — Walls + Enemies + Powerups
   ═══════════════════════════════════════════════ */

function buildLevel(missionId) {
  Game.walls = [];
  Game.enemies = [];
  Game.powerups = [];
  Game.bullets = [];
  Game.particles = [];
  
  const W = Game.worldWidth;
  const H = Game.worldHeight;
  
  // ═══ Walls — تصميم حسب المهمة ═══
  if (missionId === 'tower') {
    // مكاتب
    addWall(300, 200, 400, 60);
    addWall(800, 200, 400, 60);
    addWall(300, 500, 60, 400);
    addWall(300, 900, 700, 60);
    addWall(1200, 400, 60, 500);
    addWall(1400, 400, 400, 60);
    addWall(1400, 700, 400, 60);
    addWall(1800, 700, 60, 400);
    addWall(500, 1200, 800, 60);
    addWall(1600, 1100, 400, 60);
    addWall(1000, 300, 60, 200);
    addWall(700, 800, 300, 60);
  } else if (missionId === 'airport') {
    // مدرج الطائرات
    addWall(200, 300, 60, 600);
    addWall(200, 300, 600, 60);
    addWall(800, 200, 60, 500);
    addWall(1000, 400, 600, 60);
    addWall(1400, 600, 60, 500);
    addWall(400, 1100, 900, 60);
    addWall(1500, 900, 500, 60);
    addWall(1000, 200, 300, 60);
    addWall(1800, 300, 60, 500);
  } else {
    // بنك
    addWall(400, 200, 600, 60);
    addWall(400, 200, 60, 400);
    addWall(1000, 200, 60, 400);
    addWall(600, 700, 400, 60);
    addWall(200, 900, 400, 60);
    addWall(800, 900, 60, 300);
    addWall(1200, 800, 500, 60);
    addWall(1200, 800, 60, 500);
    addWall(1200, 1250, 500, 60);
    addWall(1800, 400, 60, 600);
    addWall(1600, 300, 300, 60);
  }
  
  // ═══ Player Position ═══
  Game.player = {
    x: 150, y: H - 150,
    radius: 18,
    vx: 0, vy: 0,
    hp: 100, maxHp: 100,
    ammo: 30, maxAmmo: 30,
    reloading: 0,
    fireCooldown: 0,
    alive: true,
    aimAngle: 0,
    weaponKey: 'pistol',
    weapon: WEAPONS.pistol,
    color: '#10B981'
  };
  
  // ═══ Enemies — 15-20 حسب المهمة ═══
  const enemyCount = missionId === 'airport' ? 20 : missionId === 'bank' ? 18 : 15;
  Game.killsTarget = enemyCount;
  Game.kills = 0;
  
  const spawnZones = [
    { x: W - 300, y: 200, r: 200 },
    { x: W - 300, y: H - 300, r: 250 },
    { x: W / 2, y: 150, r: 300 },
    { x: W / 2, y: H - 150, r: 300 },
    { x: 200, y: 300, r: 200 }
  ];
  
  for (let i = 0; i < enemyCount; i++) {
    const zone = spawnZones[i % spawnZones.length];
    let ex, ey, tries = 0;
    do {
      const a = Math.random() * Math.PI * 2;
      const r = Math.random() * zone.r;
      ex = zone.x + Math.cos(a) * r;
      ey = zone.y + Math.sin(a) * r;
      tries++;
    } while (tries < 20 && (ex < 100 || ex > W - 100 || ey < 100 || ey > H - 100));
    
    const types = [
      { hp: 40, speed: 80, damage: 8, visionRange: 350, fireInterval: 1.2, color: '#DC2626', radius: 16 },
      { hp: 60, speed: 60, damage: 12, visionRange: 300, fireInterval: 1.5, color: '#991B1B', radius: 18 },
      { hp: 30, speed: 120, damage: 6, visionRange: 400, fireInterval: 0.8, color: '#F59E0B', radius: 14 }
    ];
    const type = types[Math.floor(Math.random() * types.length)];
    
    Game.enemies.push({
      x: ex, y: ey,
      spawnX: ex, spawnY: ey,
      radius: type.radius,
      hp: type.hp,
      maxHp: type.hp,
      speed: type.speed,
      damage: type.damage,
      visionRange: type.visionRange,
      fireInterval: type.fireInterval,
      fireCooldown: Math.random() * 2,
      color: type.color,
      aimAngle: 0,
      alive: true,
      state: 'patrol',
      patrolTarget: null,
      alertTimer: 0,
      lastSeenX: 0,
      lastSeenY: 0
    });
  }
  
  // ═══ Powerups ═══
  for (let i = 0; i < 6; i++) {
    let px, py, tries = 0;
    do {
      px = 200 + Math.random() * (W - 400);
      py = 200 + Math.random() * (H - 400);
      tries++;
    } while (tries < 20 && Game.walls.some(w => 
      px > w.x - 30 && px < w.x + w.w + 30 && 
      py > w.y - 30 && py < w.y + w.h + 30
    ));
    
    Game.powerups.push({
      x: px, y: py,
      type: Math.random() > 0.5 ? 'health' : 'ammo'
    });
  }
}

function addWall(x, y, w, h) {
  Game.walls.push({ x, y, w, h });
}

// ═══ Start Mission ═══
function startMission() {
  document.getElementById('briefing').classList.add('hidden');
  document.getElementById('hud').classList.remove('hidden');
  document.getElementById('joystick').classList.remove('hidden');
  document.getElementById('actions').classList.remove('hidden');
  
  // Rebuild level
  buildLevel(Game.currentMission.id);
  
  Game.state = STATE.PLAYING;
  Game.startTime = performance.now() / 1000;
  Game.missionTime = 240;
  Game.camera.x = 0;
  Game.camera.y = 0;
  
  updateHealthUI();
  updateAmmoUI();
  updateKillsUI();
  updateTimerUI();
  
  document.getElementById('phase-icon').textContent = '🔫';
  document.getElementById('phase-name').textContent = 'المرحلة 1 — الاختراق';
  document.getElementById('objective-text').textContent = 'اقضِ على الحراس';
  
  playSound('powerup');
}
