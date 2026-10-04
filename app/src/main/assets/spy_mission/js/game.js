/* ═══════════════════════════════════════════════
   عملية سرية — Spy Shooter Engine
   ═══════════════════════════════════════════════ */

// ═══ State ═══
const STATE = {
  MENU: 'menu',
  BRIEFING: 'briefing',
  PLAYING: 'playing',
  PUZZLE: 'puzzle',
  INVESTIGATION: 'investigation',
  VOTING: 'voting',
  RESULT: 'result'
};

const Game = {
  state: STATE.MENU,
  canvas: null,
  ctx: null,
  width: 0,
  height: 0,
  dpr: 1,
  
  // Timing
  lastTime: 0,
  delta: 0,
  totalTime: 0,
  
  // Camera
  camera: { x: 0, y: 0 },
  
  // Input
  input: {
    joystick: { active: false, baseX: 0, baseY: 0, knobX: 0, knobY: 0, dx: 0, dy: 0, id: null },
    shooting: false,
    keys: {}
  },
  
  // Entities
  player: null,
  enemies: [],
  bullets: [],
  powerups: [],
  particles: [],
  effects: [],
  walls: [],
  
  // Level
  level: null,
  worldWidth: 2400,
  worldHeight: 1600,
  
  // Counters
  kills: 0,
  killsTarget: 0,
  startTime: 0,
  missionTime: 240, // 4 minutes
  
  // Mole (hidden role)
  moleIndex: -1,
  agents: [],
  playerIndex: 0,
  suspicion: {},
  
  // Mission
  currentMission: null
};

// ═══════════════════════════════════════════════
//  INIT
// ═══════════════════════════════════════════════
function initGame() {
  Game.canvas = document.getElementById('game');
  Game.ctx = Game.canvas.getContext('2d');
  
  resizeCanvas();
  window.addEventListener('resize', resizeCanvas);
  
  setupInput();
  loadMission();
  
  // Hide loading, show briefing
  setTimeout(() => {
    document.getElementById('loading').classList.add('hidden');
    showBriefing();
  }, 1800);
  
  // Start loop
  Game.lastTime = performance.now();
  requestAnimationFrame(gameLoop);
}

function resizeCanvas() {
  const dpr = window.devicePixelRatio || 1;
  Game.dpr = dpr;
  Game.width = window.innerWidth;
  Game.height = window.innerHeight;
  Game.canvas.width = Game.width * dpr;
  Game.canvas.height = Game.height * dpr;
  Game.canvas.style.width = Game.width + 'px';
  Game.canvas.style.height = Game.height + 'px';
  Game.ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
}

// ═══════════════════════════════════════════════
//  GAME LOOP
// ═══════════════════════════════════════════════
function gameLoop(now) {
  const dt = Math.min((now - Game.lastTime) / 1000, 0.05);
  Game.lastTime = now;
  Game.delta = dt;
  Game.totalTime += dt;
  
  if (Game.state === STATE.PLAYING) {
    update(dt);
    render();
  } else {
    // Clear canvas when not playing
    Game.ctx.clearRect(0, 0, Game.width, Game.height);
  }
  
  requestAnimationFrame(gameLoop);
}

function update(dt) {
  const p = Game.player;
  if (!p || !p.alive) return;
  
  // Timer
  Game.missionTime -= dt;
  if (Game.missionTime <= 0) {
    endGame(false, 'انتهى الوقت');
    return;
  }
  updateTimerUI();
  
  // Player movement
  const speed = 200 * dt;
  let dx = Game.input.joystick.dx;
  let dy = Game.input.joystick.dy;
  
  // Normalize
  const mag = Math.sqrt(dx * dx + dy * dy);
  if (mag > 0.15) {
    dx /= mag;
    dy /= mag;
  } else {
    dx = 0;
    dy = 0;
  }
  
  p.vx = dx * speed;
  p.vy = dy * speed;
  
  // Move with collision
  moveEntity(p, p.vx * dt, p.vy * dt);
  
  // Aiming: player aims in movement direction or last direction
  if (mag > 0.15) {
    p.aimAngle = Math.atan2(dy, dx);
  }
  
  // Shooting
  if (Game.input.shooting && p.ammo > 0 && p.fireCooldown <= 0) {
    shootPlayer();
  }
  
  // Cooldowns
  p.fireCooldown = Math.max(0, p.fireCooldown - dt);
  if (p.reloading > 0) {
    p.reloading -= dt;
    if (p.reloading <= 0) {
      p.ammo = p.maxAmmo;
      updateAmmoUI();
    }
  }
  
  // Update enemies
  for (const e of Game.enemies) updateEnemy(e, dt);
  
  // Update bullets
  updateBullets(dt);
  
  // Update particles
  updateParticles(dt);
  
  // Camera follows player
  updateCamera();
  
  // Check powerup pickup
  checkPickups();
  
  // Check win
  if (Game.kills >= Game.killsTarget) {
    // Phase 1 complete → Investigation
    setTimeout(() => startInvestigation(), 1500);
    Game.kills = Game.killsTarget;
  }
}

function updateCamera() {
  const p = Game.player;
  const targetX = p.x - Game.width / 2;
  const targetY = p.y - Game.height / 2;
  Game.camera.x += (targetX - Game.camera.x) * 0.1;
  Game.camera.y += (targetY - Game.camera.y) * 0.1;
  
  // Clamp
  Game.camera.x = Math.max(0, Math.min(Game.worldWidth - Game.width, Game.camera.x));
  Game.camera.y = Math.max(0, Math.min(Game.worldHeight - Game.height, Game.camera.y));
  
  if (Game.worldWidth < Game.width) Game.camera.x = (Game.worldWidth - Game.width) / 2;
  if (Game.worldHeight < Game.height) Game.camera.y = (Game.worldHeight - Game.height) / 2;
}

// ═══════════════════════════════════════════════
//  PHYSICS
// ═══════════════════════════════════════════════
function moveEntity(e, dx, dy) {
  // Move X
  const oldX = e.x;
  e.x += dx;
  if (collidesWithWalls(e)) e.x = oldX;
  
  // Move Y
  const oldY = e.y;
  e.y += dy;
  if (collidesWithWalls(e)) e.y = oldY;
  
  // World bounds
  e.x = Math.max(e.radius, Math.min(Game.worldWidth - e.radius, e.x));
  e.y = Math.max(e.radius, Math.min(Game.worldHeight - e.radius, e.y));
}

function collidesWithWalls(e) {
  for (const w of Game.walls) {
    if (circleRectCollision(e.x, e.y, e.radius, w.x, w.y, w.w, w.h)) return true;
  }
  return false;
}

function circleRectCollision(cx, cy, r, rx, ry, rw, rh) {
  const nx = Math.max(rx, Math.min(cx, rx + rw));
  const ny = Math.max(ry, Math.min(cy, ry + rh));
  const dx = cx - nx;
  const dy = cy - ny;
  return dx * dx + dy * dy < r * r;
}

function hasLineOfSight(x1, y1, x2, y2) {
  const steps = 20;
  for (let i = 1; i < steps; i++) {
    const t = i / steps;
    const x = x1 + (x2 - x1) * t;
    const y = y1 + (y2 - y1) * t;
    for (const w of Game.walls) {
      if (x > w.x && x < w.x + w.w && y > w.y && y < w.y + w.h) return false;
    }
  }
  return true;
}

// ═══════════════════════════════════════════════
//  SHOOTING
// ═══════════════════════════════════════════════
function shootPlayer() {
  const p = Game.player;
  const w = p.weapon;
  
  const angle = p.aimAngle + (Math.random() - 0.5) * w.spread;
  const speed = w.bulletSpeed;
  
  // Muzzle position
  const mx = p.x + Math.cos(angle) * (p.radius + 8);
  const my = p.y + Math.sin(angle) * (p.radius + 8);
  
  for (let i = 0; i < w.pellets; i++) {
    const a = angle + (Math.random() - 0.5) * w.spread * (w.pellets > 1 ? 2 : 0);
    Game.bullets.push({
      x: mx, y: my,
      vx: Math.cos(a) * speed,
      vy: Math.sin(a) * speed,
      damage: w.damage,
      owner: 'player',
      life: 1.5,
      color: w.bulletColor,
      size: w.bulletSize || 3
    });
  }
  
  p.ammo--;
  p.fireCooldown = 1 / w.fireRate;
  updateAmmoUI();
  
  // Muzzle flash
  spawnMuzzleFlash(mx, my, angle);
  playSound('shoot');
}

function updateBullets(dt) {
  for (let i = Game.bullets.length - 1; i >= 0; i--) {
    const b = Game.bullets[i];
    b.x += b.vx * dt;
    b.y += b.vy * dt;
    b.life -= dt;
    
    // Wall collision
    let hit = false;
    for (const w of Game.walls) {
      if (b.x > w.x && b.x < w.x + w.w && b.y > w.y && b.y < w.y + w.h) {
        hit = true;
        spawnImpact(b.x, b.y);
        break;
      }
    }
    
    // Entity collision
    if (!hit) {
      if (b.owner === 'player') {
        for (const e of Game.enemies) {
          if (!e.alive) continue;
          const dx = e.x - b.x;
          const dy = e.y - b.y;
          if (dx * dx + dy * dy < (e.radius + 4) * (e.radius + 4)) {
            e.hp -= b.damage;
            spawnBlood(b.x, b.y);
            spawnDamageNumber(e.x, e.y, b.damage);
            hit = true;
            if (e.hp <= 0) {
              e.alive = false;
              Game.kills++;
              updateKillsUI();
              spawnExplosion(e.x, e.y);
              playSound('kill');
            }
            break;
          }
        }
      } else {
        // Enemy bullet vs player
        const p = Game.player;
        if (p.alive) {
          const dx = p.x - b.x;
          const dy = p.y - b.y;
          if (dx * dx + dy * dy < (p.radius + 4) * (p.radius + 4)) {
            p.hp -= b.damage;
            spawnBlood(b.x, b.y);
            spawnDamageNumber(p.x, p.y, b.damage);
            hit = true;
            updateHealthUI();
            playSound('hurt');
            if (p.hp <= 0) {
              p.hp = 0;
              p.alive = false;
              endGame(false, 'قُتلت في المعركة');
            }
          }
        }
      }
    }
    
    // Out of bounds or expired
    if (hit || b.life <= 0 || b.x < 0 || b.x > Game.worldWidth || b.y < 0 || b.y > Game.worldHeight) {
      Game.bullets.splice(i, 1);
    }
  }
}

// ═══════════════════════════════════════════════
//  ENEMIES AI
// ═══════════════════════════════════════════════
function updateEnemy(e, dt) {
  if (!e.alive) return;
  
  const p = Game.player;
  if (!p.alive) return;
  
  const dx = p.x - e.x;
  const dy = p.y - e.y;
  const dist = Math.sqrt(dx * dx + dy * dy);
  const canSee = dist < e.visionRange && hasLineOfSight(e.x, e.y, p.x, p.y);
  
  if (canSee) {
    e.state = 'chase';
    e.lastSeenX = p.x;
    e.lastSeenY = p.y;
    e.alertTimer = 3;
  } else if (e.alertTimer > 0) {
    e.alertTimer -= dt;
  } else {
    e.state = 'patrol';
  }
  
  if (e.state === 'chase' && canSee) {
    // Move toward player
    const nx = dx / dist;
    const ny = dy / dist;
    
    // Keep some distance
    const idealDist = e.range || 200;
    if (dist > idealDist) {
      moveEntity(e, nx * e.speed * dt, ny * e.speed * dt);
    } else if (dist < idealDist * 0.6) {
      moveEntity(e, -nx * e.speed * 0.5 * dt, -ny * e.speed * 0.5 * dt);
    }
    
    // Face player
    e.aimAngle = Math.atan2(dy, dx);
    
    // Shoot
    e.fireCooldown -= dt;
    if (e.fireCooldown <= 0 && dist < e.visionRange) {
      shootEnemy(e);
      e.fireCooldown = e.fireInterval;
    }
  } else if (e.state === 'patrol') {
    // Patrol
    if (!e.patrolTarget || Math.abs(e.x - e.patrolTarget.x) < 10 && Math.abs(e.y - e.patrolTarget.y) < 10) {
      e.patrolTarget = {
        x: e.spawnX + (Math.random() - 0.5) * 300,
        y: e.spawnY + (Math.random() - 0.5) * 300
      };
    }
    const pdx = e.patrolTarget.x - e.x;
    const pdy = e.patrolTarget.y - e.y;
    const pd = Math.sqrt(pdx * pdx + pdy * pdy);
    if (pd > 5) {
      moveEntity(e, (pdx / pd) * e.speed * 0.4 * dt, (pdy / pd) * e.speed * 0.4 * dt);
      e.aimAngle = Math.atan2(pdy, pdx);
    }
  }
}

function shootEnemy(e) {
  const angle = e.aimAngle + (Math.random() - 0.5) * 0.15;
  const mx = e.x + Math.cos(angle) * (e.radius + 6);
  const my = e.y + Math.sin(angle) * (e.radius + 6);
  
  Game.bullets.push({
    x: mx, y: my,
    vx: Math.cos(angle) * 450,
    vy: Math.sin(angle) * 450,
    damage: e.damage,
    owner: 'enemy',
    life: 2,
    color: '#FF4444',
    size: 3
  });
  
  spawnMuzzleFlash(mx, my, angle, '#FF6666');
  playSound('enemy_shoot');
}

// ═══════════════════════════════════════════════
//  PARTICLES
// ═══════════════════════════════════════════════
function spawnBlood(x, y) {
  for (let i = 0; i < 8; i++) {
    const a = Math.random() * Math.PI * 2;
    const s = 50 + Math.random() * 100;
    Game.particles.push({
      x, y,
      vx: Math.cos(a) * s,
      vy: Math.sin(a) * s,
      life: 0.5,
      maxLife: 0.5,
      size: 2 + Math.random() * 2,
      color: '#DC2626'
    });
  }
}

function spawnMuzzleFlash(x, y, angle, color = '#FFD700') {
  for (let i = 0; i < 5; i++) {
    const a = angle + (Math.random() - 0.5) * 0.8;
    const s = 150 + Math.random() * 200;
    Game.particles.push({
      x, y,
      vx: Math.cos(a) * s,
      vy: Math.sin(a) * s,
      life: 0.15,
      maxLife: 0.15,
      size: 2 + Math.random() * 2,
      color: color
    });
  }
}

function spawnImpact(x, y) {
  for (let i = 0; i < 4; i++) {
    const a = Math.random() * Math.PI * 2;
    const s = 30 + Math.random() * 80;
    Game.particles.push({
      x, y,
      vx: Math.cos(a) * s,
      vy: Math.sin(a) * s,
      life: 0.3,
      maxLife: 0.3,
      size: 1 + Math.random() * 2,
      color: '#888'
    });
  }
}

function spawnExplosion(x, y) {
  for (let i = 0; i < 20; i++) {
    const a = Math.random() * Math.PI * 2;
    const s = 100 + Math.random() * 250;
    Game.particles.push({
      x, y,
      vx: Math.cos(a) * s,
      vy: Math.sin(a) * s,
      life: 0.8,
      maxLife: 0.8,
      size: 2 + Math.random() * 4,
      color: Math.random() > 0.5 ? '#DC2626' : '#F59E0B'
    });
  }
}

function spawnDamageNumber(x, y, dmg) {
  const el = document.createElement('div');
  el.className = 'damage-num';
  el.textContent = '-' + Math.round(dmg);
  const sx = x - Game.camera.x;
  const sy = y - Game.camera.y;
  el.style.left = sx + 'px';
  el.style.top = sy + 'px';
  document.body.appendChild(el);
  setTimeout(() => el.remove(), 800);
}

function updateParticles(dt) {
  for (let i = Game.particles.length - 1; i >= 0; i--) {
    const p = Game.particles[i];
    p.x += p.vx * dt;
    p.y += p.vy * dt;
    p.vx *= 0.94;
    p.vy *= 0.94;
    p.life -= dt;
    if (p.life <= 0) Game.particles.splice(i, 1);
  }
}

// ═══════════════════════════════════════════════
//  PICKUPS
// ═══════════════════════════════════════════════
function checkPickups() {
  const p = Game.player;
  for (let i = Game.powerups.length - 1; i >= 0; i--) {
    const pu = Game.powerups[i];
    const dx = p.x - pu.x;
    const dy = p.y - pu.y;
    if (dx * dx + dy * dy < (p.radius + 20) * (p.radius + 20)) {
      applyPowerup(pu);
      Game.powerups.splice(i, 1);
      spawnExplosion(pu.x, pu.y);
      playSound('powerup');
    }
  }
}

function applyPowerup(pu) {
  const p = Game.player;
  switch (pu.type) {
    case 'health':
      p.hp = Math.min(p.maxHp, p.hp + 40);
      updateHealthUI();
      toast('❤️ +40 HP');
      break;
    case 'ammo':
      p.ammo = p.maxAmmo;
      updateAmmoUI();
      toast('🔫 تم إعادة التلقيم');
      break;
  }
}

// ═══════════════════════════════════════════════
//  RENDER
// ═══════════════════════════════════════════════
function render() {
  const ctx = Game.ctx;
  ctx.clearRect(0, 0, Game.width, Game.height);
  
  ctx.save();
  ctx.translate(-Game.camera.x, -Game.camera.y);
  
  // Background
  drawBackground();
  
  // Walls
  for (const w of Game.walls) drawWall(w);
  
  // Powerups
  for (const pu of Game.powerups) drawPowerup(pu);
  
  // Enemies
  for (const e of Game.enemies) if (e.alive) drawEnemy(e);
  
  // Bullets
  for (const b of Game.bullets) drawBullet(b);
  
  // Particles
  for (const p of Game.particles) drawParticle(p);
  
  // Player
  if (Game.player && Game.player.alive) drawPlayer(Game.player);
  
  ctx.restore();
}

function drawBackground() {
  const ctx = Game.ctx;
  ctx.fillStyle = '#0a0510';
  ctx.fillRect(0, 0, Game.worldWidth, Game.worldHeight);
  
  // Grid pattern
  ctx.strokeStyle = 'rgba(212, 175, 55, 0.05)';
  ctx.lineWidth = 1;
  const gridSize = 60;
  const startX = Math.floor(Game.camera.x / gridSize) * gridSize;
  const startY = Math.floor(Game.camera.y / gridSize) * gridSize;
  const endX = startX + Game.width + gridSize;
  const endY = startY + Game.height + gridSize;
  
  for (let x = startX; x < endX; x += gridSize) {
    ctx.beginPath();
    ctx.moveTo(x, startY);
    ctx.lineTo(x, endY);
    ctx.stroke();
  }
  for (let y = startY; y < endY; y += gridSize) {
    ctx.beginPath();
    ctx.moveTo(startX, y);
    ctx.lineTo(endX, y);
    ctx.stroke();
  }
}

function drawWall(w) {
  const ctx = Game.ctx;
  ctx.fillStyle = '#1a0f1a';
  ctx.fillRect(w.x, w.y, w.w, w.h);
  ctx.strokeStyle = '#D4AF37';
  ctx.lineWidth = 2;
  ctx.strokeRect(w.x, w.y, w.w, w.h);
  
  // Inner detail
  ctx.strokeStyle = 'rgba(212, 175, 55, 0.2)';
  ctx.lineWidth = 1;
  ctx.strokeRect(w.x + 3, w.y + 3, w.w - 6, w.h - 6);
}

function drawPowerup(pu) {
  const ctx = Game.ctx;
  const pulse = Math.sin(Game.totalTime * 5 + pu.x * 0.01) * 0.2 + 0.8;
  
  // Glow
  ctx.fillStyle = pu.type === 'health' ? 'rgba(220, 38, 38, 0.3)' : 'rgba(212, 175, 55, 0.3)';
  ctx.beginPath();
  ctx.arc(pu.x, pu.y, 30 * pulse, 0, Math.PI * 2);
  ctx.fill();
  
  // Icon
  ctx.font = '32px serif';
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillText(pu.type === 'health' ? '❤️' : '🔫', pu.x, pu.y);
}

function drawEnemy(e) {
  const ctx = Game.ctx;
  
  // Vision cone
  if (e.state === 'chase') {
    ctx.fillStyle = 'rgba(220, 38, 38, 0.08)';
    ctx.beginPath();
    ctx.moveTo(e.x, e.y);
    ctx.arc(e.x, e.y, e.visionRange, e.aimAngle - 0.5, e.aimAngle + 0.5);
    ctx.closePath();
    ctx.fill();
  }
  
  // Body
  ctx.save();
  ctx.translate(e.x, e.y);
  
  // Shadow
  ctx.fillStyle = 'rgba(0, 0, 0, 0.5)';
  ctx.beginPath();
  ctx.ellipse(0, e.radius * 0.7, e.radius, e.radius * 0.5, 0, 0, Math.PI * 2);
  ctx.fill();
  
  // Circle
  ctx.fillStyle = e.color;
  ctx.beginPath();
  ctx.arc(0, 0, e.radius, 0, Math.PI * 2);
  ctx.fill();
  ctx.strokeStyle = '#DC2626';
  ctx.lineWidth = 2;
  ctx.stroke();
  
  // Aim indicator
  ctx.rotate(e.aimAngle);
  ctx.fillStyle = '#DC2626';
  ctx.fillRect(e.radius - 3, -3, 12, 6);
  
  ctx.restore();
  
  // HP bar
  const hpPct = e.hp / e.maxHp;
  const barW = 30;
  const barX = e.x - barW / 2;
  const barY = e.y - e.radius - 10;
  ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
  ctx.fillRect(barX - 1, barY - 1, barW + 2, 5);
  ctx.fillStyle = hpPct > 0.5 ? '#10B981' : hpPct > 0.25 ? '#F59E0B' : '#DC2626';
  ctx.fillRect(barX, barY, barW * hpPct, 3);
}

function drawPlayer(p) {
  const ctx = Game.ctx;
  ctx.save();
  ctx.translate(p.x, p.y);
  
  // Shadow
  ctx.fillStyle = 'rgba(0, 0, 0, 0.6)';
  ctx.beginPath();
  ctx.ellipse(0, p.radius * 0.8, p.radius * 1.1, p.radius * 0.5, 0, 0, Math.PI * 2);
  ctx.fill();
  
  // Aura
  const aura = Math.sin(Game.totalTime * 3) * 0.2 + 0.8;
  ctx.fillStyle = 'rgba(16, 185, 129, 0.15)';
  ctx.beginPath();
  ctx.arc(0, 0, p.radius + 8 * aura, 0, Math.PI * 2);
  ctx.fill();
  
  // Body
  ctx.fillStyle = '#10B981';
  ctx.beginPath();
  ctx.arc(0, 0, p.radius, 0, Math.PI * 2);
  ctx.fill();
  ctx.strokeStyle = '#D4AF37';
  ctx.lineWidth = 3;
  ctx.stroke();
  
  // Rotate for aim
  ctx.rotate(p.aimAngle);
  
  // Gun
  ctx.fillStyle = '#444';
  ctx.fillRect(p.radius - 4, -4, 20, 8);
  ctx.fillStyle = '#D4AF37';
  ctx.fillRect(p.radius + 12, -3, 4, 6);
  
  // Visor
  ctx.fillStyle = '#D4AF37';
  ctx.beginPath();
  ctx.arc(0, 0, p.radius * 0.5, 0, Math.PI * 2);
  ctx.fill();
  
  ctx.restore();
}

function drawBullet(b) {
  const ctx = Game.ctx;
  ctx.fillStyle = b.color;
  ctx.shadowColor = b.color;
  ctx.shadowBlur = 8;
  ctx.beginPath();
  ctx.arc(b.x, b.y, b.size || 3, 0, Math.PI * 2);
  ctx.fill();
  ctx.shadowBlur = 0;
}

function drawParticle(p) {
  const ctx = Game.ctx;
  const alpha = p.life / p.maxLife;
  ctx.globalAlpha = alpha;
  ctx.fillStyle = p.color;
  ctx.beginPath();
  ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
  ctx.fill();
  ctx.globalAlpha = 1;
}

// ═══════════════════════════════════════════════
//  INPUT
// ═══════════════════════════════════════════════
function setupInput() {
  const joy = document.getElementById('joystick');
  const joyBase = joy.querySelector('.joystick-base');
  const joyKnob = joy.querySelector('.joystick-knob');
  
  let touchId = null;
  let baseX = 0, baseY = 0;
  
  function startJoy(e, t) {
    const rect = joyBase.getBoundingClientRect();
    baseX = rect.left + rect.width / 2;
    baseY = rect.top + rect.height / 2;
    touchId = t.identifier;
    Game.input.joystick.active = true;
    moveJoy(t.clientX, t.clientY);
  }
  
  function moveJoy(x, y) {
    const dx = x - baseX;
    const dy = y - baseY;
    const dist = Math.sqrt(dx * dx + dy * dy);
    const maxDist = 50;
    
    const clamp = Math.min(dist, maxDist);
    const nx = dx / dist;
    const ny = dy / dist;
    
    const kx = nx * clamp;
    const ky = ny * clamp;
    
    joyKnob.style.transform = `translate(calc(-50% + ${kx}px), calc(-50% + ${ky}px))`;
    
    Game.input.joystick.dx = nx * (clamp / maxDist);
    Game.input.joystick.dy = ny * (clamp / maxDist);
  }
  
  function endJoy() {
    touchId = null;
    Game.input.joystick.active = false;
    Game.input.joystick.dx = 0;
    Game.input.joystick.dy = 0;
    joyKnob.style.transform = 'translate(-50%, -50%)';
  }
  
  joyBase.addEventListener('touchstart', (e) => {
    e.preventDefault();
    startJoy(e, e.changedTouches[0]);
  }, { passive: false });
  
  joyBase.addEventListener('touchmove', (e) => {
    e.preventDefault();
    for (const t of e.changedTouches) {
      if (t.identifier === touchId) moveJoy(t.clientX, t.clientY);
    }
  }, { passive: false });
  
  joyBase.addEventListener('touchend', (e) => {
    for (const t of e.changedTouches) {
      if (t.identifier === touchId) endJoy();
    }
  }, { passive: false });
  
  joyBase.addEventListener('touchcancel', endJoy);
  
  // Shoot button
  const btnShoot = document.getElementById('btn-shoot');
  btnShoot.addEventListener('touchstart', (e) => {
    e.preventDefault();
    Game.input.shooting = true;
  }, { passive: false });
  btnShoot.addEventListener('touchend', (e) => {
    e.preventDefault();
    Game.input.shooting = false;
  }, { passive: false });
  
  // Reload button
  document.getElementById('btn-reload').addEventListener('touchstart', (e) => {
    e.preventDefault();
    reload();
  }, { passive: false });
  
  // Weapon switch
  document.getElementById('btn-weapon').addEventListener('touchstart', (e) => {
    e.preventDefault();
    cycleWeapon();
  }, { passive: false });
}

function reload() {
  const p = Game.player;
  if (p.reloading > 0 || p.ammo === p.maxAmmo) return;
  p.reloading = p.weapon.reloadTime;
  toast('🔄 جاري التلقيم...');
}

function cycleWeapon() {
  const p = Game.player;
  const weapons = Object.keys(WEAPONS);
  const currentIdx = weapons.indexOf(p.weaponKey);
  const nextIdx = (currentIdx + 1) % weapons.length;
  p.weaponKey = weapons[nextIdx];
  p.weapon = WEAPONS[p.weaponKey];
  p.ammo = p.weapon.maxAmmo;
  p.maxAmmo = p.weapon.maxAmmo;
  p.reloading = 0;
  document.getElementById('weapon-icon').textContent = p.weapon.icon;
  updateAmmoUI();
  toast(p.weapon.icon + ' ' + p.weapon.name);
}

// ═══════════════════════════════════════════════
//  WEAPONS
// ═══════════════════════════════════════════════
const WEAPONS = {
  pistol: {
    name: 'مسدس',
    icon: '🔫',
    damage: 20,
    fireRate: 4,
    bulletSpeed: 700,
    maxAmmo: 30,
    reloadTime: 1.2,
    spread: 0.05,
    pellets: 1,
    bulletColor: '#F4D97A',
    bulletSize: 3
  },
  rifle: {
    name: 'رشاش',
    icon: '🔫',
    damage: 12,
    fireRate: 12,
    bulletSpeed: 900,
    maxAmmo: 60,
    reloadTime: 1.8,
    spread: 0.15,
    pellets: 1,
    bulletColor: '#FFA500',
    bulletSize: 2
  },
  shotgun: {
    name: 'شوزن',
    icon: '💥',
    damage: 10,
    fireRate: 1.2,
    bulletSpeed: 600,
    maxAmmo: 8,
    reloadTime: 2.2,
    spread: 0.5,
    pellets: 8,
    bulletColor: '#FF8C00',
    bulletSize: 4
  },
  sniper: {
    name: 'قناصة',
    icon: '🎯',
    damage: 80,
    fireRate: 0.8,
    bulletSpeed: 1200,
    maxAmmo: 5,
    reloadTime: 2.5,
    spread: 0.01,
    pellets: 1,
    bulletColor: '#FF0000',
    bulletSize: 5
  }
};

// ═══════════════════════════════════════════════
//  UI UPDATES
// ═══════════════════════════════════════════════
function updateHealthUI() {
  const p = Game.player;
  const pct = Math.max(0, p.hp / p.maxHp);
  document.getElementById('health-fill').style.width = (pct * 100) + '%';
  document.getElementById('health-text').textContent = Math.max(0, Math.round(p.hp));
}

function updateAmmoUI() {
  const p = Game.player;
  document.getElementById('ammo-current').textContent = p.ammo;
  document.getElementById('ammo-max').textContent = p.maxAmmo;
}

function updateKillsUI() {
  document.getElementById('kill-count').textContent = Game.kills;
  document.getElementById('kill-total').textContent = Game.killsTarget;
}

function updateTimerUI() {
  const t = Math.max(0, Math.floor(Game.missionTime));
  const m = Math.floor(t / 60);
  const s = t % 60;
  document.getElementById('timer').textContent = 
    String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
}

function toast(text) {
  const el = document.getElementById('toast');
  el.textContent = text;
  el.classList.remove('hidden');
  clearTimeout(el._to);
  el._to = setTimeout(() => el.classList.add('hidden'), 2000);
}

// ═══════════════════════════════════════════════
//  SOUND (Web Audio placeholder)
// ═══════════════════════════════════════════════
let audioCtx = null;
function playSound(name) {
  // Simple synth sounds (no external files)
  if (!audioCtx) {
    try {
      audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    } catch (e) { return; }
  }
  
  if (audioCtx.state === 'suspended') audioCtx.resume();
  
  const sounds = {
    shoot: { freq: 800, dur: 0.05, type: 'square' },
    enemy_shoot: { freq: 600, dur: 0.05, type: 'square' },
    kill: { freq: 200, dur: 0.2, type: 'sawtooth' },
    hurt: { freq: 150, dur: 0.15, type: 'sawtooth' },
    powerup: { freq: 1200, dur: 0.2, type: 'sine' }
  };
  
  const s = sounds[name];
  if (!s) return;
  
  try {
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.type = s.type;
    osc.frequency.value = s.freq;
    gain.gain.value = 0.08;
    gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + s.dur);
    osc.connect(gain);
    gain.connect(audioCtx.destination);
    osc.start();
    osc.stop(audioCtx.currentTime + s.dur);
  } catch (e) {}
}

// ═══ EXPOSE ═══
window.initGame = initGame;
window.startMission = startMission;
window.exitGame = exitGame;

// ═══ INIT ON LOAD ═══
document.addEventListener('DOMContentLoaded', initGame);
