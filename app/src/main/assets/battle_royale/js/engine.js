/* ═══════════════════════════════════════════════
   Game Engine — Rendering + Physics
   ═══════════════════════════════════════════════ */

const Game = {
  canvas: null,
  ctx: null,
  width: 0,
  height: 0,
  dpr: 1,
  
  worldWidth: 2400,
  worldHeight: 1600,
  
  camera: { x: 0, y: 0 },
  
  me: null,
  players: {},
  bullets: [],
  particles: [],
  walls: [],
  
  state: 'menu', // menu | playing | dead | victory
  
  lastTime: 0,
  totalTime: 0,
  
  input: {
    joystick: { dx: 0, dy: 0, active: false },
    shooting: false
  },
  
  // Circle
  circle: { x: 1200, y: 800, r: 1200, targetR: 1200, phase: 1 },
  circleTimer: 0
};

// ═══ INIT ═══
function initEngine() {
  Game.canvas = document.getElementById('game');
  Game.ctx = Game.canvas.getContext('2d');
  resizeCanvas();
  window.addEventListener('resize', resizeCanvas);
  
  // Build map
  buildMap();
  
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

function buildMap() {
  // Simple map with some walls
  Game.walls = [
    // Border
    { x: 0, y: 0, w: Game.worldWidth, h: 20 },
    { x: 0, y: Game.worldHeight - 20, w: Game.worldWidth, h: 20 },
    { x: 0, y: 0, w: 20, h: Game.worldHeight },
    { x: Game.worldWidth - 20, y: 0, w: 20, h: Game.worldHeight },
    
    // Obstacles
    { x: 400, y: 300, w: 200, h: 40 },
    { x: 800, y: 300, w: 40, h: 300 },
    { x: 1400, y: 400, w: 300, h: 40 },
    { x: 600, y: 700, w: 40, h: 300 },
    { x: 1000, y: 900, w: 200, h: 40 },
    { x: 1600, y: 800, w: 40, h: 300 },
    { x: 500, y: 1200, w: 400, h: 40 },
    { x: 1200, y: 500, w: 200, h: 40 },
    { x: 1800, y: 1100, w: 40, h: 300 },
    { x: 300, y: 1000, w: 200, h: 40 }
  ];
}

// ═══ GAME LOOP ═══
function gameLoop(now) {
  const dt = Math.min((now - Game.lastTime) / 1000, 0.05);
  Game.lastTime = now;
  Game.totalTime += dt;
  
  if (Game.state === 'playing') {
    update(dt);
    render();
  } else {
    Game.ctx.clearRect(0, 0, Game.width, Game.height);
  }
  
  requestAnimationFrame(gameLoop);
}

function update(dt) {
  // Update me
  if (Game.me && Game.me.alive) {
    const speed = 250;
    const dx = Game.input.joystick.dx;
    const dy = Game.input.joystick.dy;
    const mag = Math.sqrt(dx * dx + dy * dy);
    
    if (mag > 0.15) {
      Game.me.vx = (dx / mag) * speed;
      Game.me.vy = (dy / mag) * speed;
      Game.me.aimAngle = Math.atan2(dy, dx);
    } else {
      Game.me.vx = 0;
      Game.me.vy = 0;
    }
    
    Game.me.update(dt);
    
    if (Game.input.shooting) {
      Game.me.shoot();
    }
    
    // Sync position
    Network.syncPosition(Game.me);
  }
  
  // Update other players
  for (const id in Game.players) {
    if (id !== Network.userId) {
      Game.players[id].update(dt);
    }
  }
  
  // Update bullets
  updateBullets(dt);
  
  // Update particles
  updateParticles(dt);
  
  // Update circle
  updateCircle(dt);
  
  // Camera
  updateCamera();
}

function updateCamera() {
  if (!Game.me) return;
  const targetX = Game.me.x - Game.width / 2;
  const targetY = Game.me.y - Game.height / 2;
  Game.camera.x += (targetX - Game.camera.x) * 0.15;
  Game.camera.y += (targetY - Game.camera.y) * 0.15;
  
  Game.camera.x = Math.max(0, Math.min(Game.worldWidth - Game.width, Game.camera.x));
  Game.camera.y = Math.max(0, Math.min(Game.worldHeight - Game.height, Game.camera.y));
}

function updateBullets(dt) {
  for (let i = Game.bullets.length - 1; i >= 0; i--) {
    const b = Game.bullets[i];
    b.x += b.vx * dt;
    b.y += b.vy * dt;
    b.life -= dt;
    
    // Check collision with players
    let hit = false;
    for (const id in Game.players) {
      if (id === b.owner) continue;
      const p = Game.players[id];
      if (!p.alive) continue;
      
      const dx = p.x - b.x;
      const dy = p.y - b.y;
      if (dx * dx + dy * dy < (p.radius + 4) * (p.radius + 4)) {
        hit = true;
        if (p.isMe) {
          // I got hit
          const died = p.takeDamage(b.damage);
          spawnBlood(b.x, b.y);
          updateHealthUI();
          if (died) onLocalDeath();
        }
        break;
      }
    }
    
    if (hit || b.life <= 0 || b.x < 0 || b.x > Game.worldWidth || b.y < 0 || b.y > Game.worldHeight) {
      Game.bullets.splice(i, 1);
    }
  }
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

function updateCircle(dt) {
  Game.circleTimer += dt;
  
  // Shrink every 30 seconds
  const phaseDuration = 30;
  const phase = Math.floor(Game.circleTimer / phaseDuration) + 1;
  if (phase !== Game.circle.phase && Game.circle.r > 200) {
    Game.circle.phase = phase;
    Game.circle.targetR = Math.max(200, Game.circle.r - 200);
  }
  
  // Smooth shrink
  if (Math.abs(Game.circle.r - Game.circle.targetR) > 1) {
    Game.circle.r += (Game.circle.targetR - Game.circle.r) * 0.005;
  }
}

// ═══ RENDER ═══
function render() {
  const ctx = Game.ctx;
  ctx.clearRect(0, 0, Game.width, Game.height);
  
  ctx.save();
  ctx.translate(-Game.camera.x, -Game.camera.y);
  
  // Background
  drawBackground();
  
  // Circle (shrink zone)
  drawDangerZone();
  
  // Walls
  for (const w of Game.walls) drawWall(w);
  
  // Players
  for (const id in Game.players) {
    Game.players[id].draw(ctx);
  }
  
  // Bullets
  for (const b of Game.bullets) {
    ctx.fillStyle = b.color;
    ctx.shadowColor = b.color;
    ctx.shadowBlur = 8;
    ctx.beginPath();
    ctx.arc(b.x, b.y, b.size || 3, 0, Math.PI * 2);
    ctx.fill();
    ctx.shadowBlur = 0;
  }
  
  // Particles
  for (const p of Game.particles) {
    ctx.globalAlpha = p.life / p.maxLife;
    ctx.fillStyle = p.color;
    ctx.beginPath();
    ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
    ctx.fill();
  }
  ctx.globalAlpha = 1;
  
  ctx.restore();
  
  // Minimap render
  renderMinimap();
}

function drawBackground() {
  const ctx = Game.ctx;
  
  // Base
  ctx.fillStyle = '#0a0510';
  ctx.fillRect(0, 0, Game.worldWidth, Game.worldHeight);
  
  // Grid
  ctx.strokeStyle = 'rgba(212, 175, 55, 0.06)';
  ctx.lineWidth = 1;
  const gs = 80;
  const sx = Math.floor(Game.camera.x / gs) * gs;
  const sy = Math.floor(Game.camera.y / gs) * gs;
  for (let x = sx; x < sx + Game.width + gs; x += gs) {
    ctx.beginPath();
    ctx.moveTo(x, sy);
    ctx.lineTo(x, sy + Game.height + gs);
    ctx.stroke();
  }
  for (let y = sy; y < sy + Game.height + gs; y += gs) {
    ctx.beginPath();
    ctx.moveTo(sx, y);
    ctx.lineTo(sx + Game.width + gs, y);
    ctx.stroke();
  }
}

function drawDangerZone() {
  const ctx = Game.ctx;
  const c = Game.circle;
  
  // Danger zone (outside the safe circle)
  ctx.fillStyle = 'rgba(220, 38, 38, 0.15)';
  ctx.beginPath();
  ctx.rect(0, 0, Game.worldWidth, Game.worldHeight);
  ctx.arc(c.x, c.y, c.r, 0, Math.PI * 2, true);
  ctx.fill('evenodd');
  
  // Circle border
  ctx.strokeStyle = '#DC2626';
  ctx.lineWidth = 4;
  ctx.setLineDash([10, 10]);
  ctx.beginPath();
  ctx.arc(c.x, c.y, c.r, 0, Math.PI * 2);
  ctx.stroke();
  ctx.setLineDash([]);
}

function drawWall(w) {
  const ctx = Game.ctx;
  ctx.fillStyle = '#1a0f1a';
  ctx.fillRect(w.x, w.y, w.w, w.h);
  ctx.strokeStyle = 'rgba(212, 175, 55, 0.5)';
  ctx.lineWidth = 2;
  ctx.strokeRect(w.x, w.y, w.w, w.h);
}

function renderMinimap() {
  const mctx = document.getElementById('minimap-canvas').getContext('2d');
  const size = 120;
  const scale = size / Math.max(Game.worldWidth, Game.worldHeight);
  
  mctx.clearRect(0, 0, size, size);
  
  // Scale
  const sx = (Game.worldWidth * scale) / 2;
  const sy = (Game.worldHeight * scale) / 2;
  
  // Draw players
  for (const id in Game.players) {
    const p = Game.players[id];
    if (!p.alive) continue;
    const px = (p.x * scale) - sx + size / 2;
    const py = (p.y * scale) - sy + size / 2;
    mctx.fillStyle = p.isMe ? '#10B981' : '#DC2626';
    mctx.beginPath();
    mctx.arc(px, py, p.isMe ? 4 : 3, 0, Math.PI * 2);
    mctx.fill();
  }
  
  // Circle
  const cx = (Game.circle.x * scale) - sx + size / 2;
  const cy = (Game.circle.y * scale) - sy + size / 2;
  const cr = Game.circle.r * scale;
  mctx.strokeStyle = '#DC2626';
  mctx.lineWidth = 1.5;
  mctx.beginPath();
  mctx.arc(cx, cy, cr, 0, Math.PI * 2);
  mctx.stroke();
}

// ═══ HELPERS ═══
function spawnBlood(x, y) {
  for (let i = 0; i < 6; i++) {
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

function spawnMuzzleFlash(x, y, angle) {
  for (let i = 0; i < 4; i++) {
    const a = angle + (Math.random() - 0.5) * 0.5;
    const s = 100 + Math.random() * 150;
    Game.particles.push({
      x, y,
      vx: Math.cos(a) * s,
      vy: Math.sin(a) * s,
      life: 0.12,
      maxLife: 0.12,
      size: 2,
      color: '#F4D97A'
    });
  }
}

window.initEngine = initEngine;
window.buildMap = buildMap;
