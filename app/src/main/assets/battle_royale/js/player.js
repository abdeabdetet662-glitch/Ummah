/* ═══════════════════════════════════════════════
   Player Class
   ═══════════════════════════════════════════════ */

class Player {
  constructor(userId, userName, isMe = false) {
    this.userId = userId;
    this.userName = userName;
    this.isMe = isMe;
    
    this.x = 200;
    this.y = 200;
    this.vx = 0;
    this.vy = 0;
    this.radius = 18;
    
    this.hp = 100;
    this.maxHp = 100;
    
    this.ammo = 30;
    this.maxAmmo = 30;
    this.reloading = 0;
    this.fireCooldown = 0;
    
    this.weaponKey = 'pistol';
    this.weapon = WEAPONS.pistol;
    
    this.alive = true;
    this.kills = 0;
    
    this.aimAngle = 0;
    this.targetX = this.x;
    this.targetY = this.y;
    
    // For smooth interpolation
    this.lastUpdate = 0;
    this.color = isMe ? '#10B981' : '#DC2626';
  }
  
  update(dt) {
    if (!this.isMe) {
      // Interpolate position of remote players
      this.x += (this.targetX - this.x) * 0.2;
      this.y += (this.targetY - this.y) * 0.2;
      return;
    }
    
    // My player - physics
    const speed = 250;
    const mag = Math.sqrt(this.vx * this.vx + this.vy * this.vy);
    
    if (mag > 0) {
      this.x += this.vx * dt;
      this.y += this.vy * dt;
      
      // World bounds
      this.x = Math.max(this.radius, Math.min(Game.worldWidth - this.radius, this.x));
      this.y = Math.max(this.radius, Math.min(Game.worldHeight - this.radius, this.y));
    }
    
    // Cooldowns
    this.fireCooldown = Math.max(0, this.fireCooldown - dt);
    if (this.reloading > 0) {
      this.reloading -= dt;
      if (this.reloading <= 0) {
        this.ammo = this.maxAmmo;
        updateAmmoUI();
      }
    }
  }
  
  shoot() {
    if (this.ammo <= 0 || this.fireCooldown > 0 || this.reloading > 0) return false;
    
    const w = this.weapon;
    
    for (let i = 0; i < w.pellets; i++) {
      const a = this.aimAngle + (Math.random() - 0.5) * w.spread * (w.pellets > 1 ? 2 : 0);
      const mx = this.x + Math.cos(this.aimAngle) * (this.radius + 8);
      const my = this.y + Math.sin(this.aimAngle) * (this.radius + 8);
      
      const bullet = {
        x: mx,
        y: my,
        vx: Math.cos(a) * w.bulletSpeed,
        vy: Math.sin(a) * w.bulletSpeed,
        damage: w.damage,
        owner: this.userId,
        ownerName: this.userName,
        life: 1.5,
        color: w.bulletColor,
        size: w.bulletSize
      };
      
      Game.bullets.push(bullet);
      
      // Sync to network
      if (Network.matchRef) {
        Network.spawnBullet(bullet);
      }
    }
    
    this.ammo--;
    this.fireCooldown = 1 / w.fireRate;
    updateAmmoUI();
    
    spawnMuzzleFlash(this.x + Math.cos(this.aimAngle) * 20, this.y + Math.sin(this.aimAngle) * 20, this.aimAngle);
    
    return true;
  }
  
  reload() {
    if (this.reloading > 0 || this.ammo === this.maxAmmo) return;
    this.reloading = this.weapon.reloadTime;
  }
  
  switchWeapon() {
    const idx = WEAPON_KEYS.indexOf(this.weaponKey);
    const next = (idx + 1) % WEAPON_KEYS.length;
    this.weaponKey = WEAPON_KEYS[next];
    this.weapon = WEAPONS[this.weaponKey];
    this.ammo = this.weapon.maxAmmo;
    this.maxAmmo = this.weapon.maxAmmo;
    this.reloading = 0;
    updateAmmoUI();
    return this.weapon;
  }
  
  takeDamage(dmg) {
    this.hp -= dmg;
    if (this.hp <= 0) {
      this.hp = 0;
      this.alive = false;
      return true; // died
    }
    return false;
  }
  
  draw(ctx) {
    ctx.save();
    ctx.translate(this.x, this.y);
    
    // Shadow
    ctx.fillStyle = 'rgba(0, 0, 0, 0.6)';
    ctx.beginPath();
    ctx.ellipse(0, this.radius * 0.8, this.radius * 1.1, this.radius * 0.5, 0, 0, Math.PI * 2);
    ctx.fill();
    
    // Aura for self
    if (this.isMe) {
      const aura = Math.sin(Game.totalTime * 3) * 0.3 + 0.7;
      ctx.fillStyle = `rgba(16, 185, 129, ${0.15 * aura})`;
      ctx.beginPath();
      ctx.arc(0, 0, this.radius + 12, 0, Math.PI * 2);
      ctx.fill();
    }
    
    // Body
    ctx.fillStyle = this.color;
    ctx.beginPath();
    ctx.arc(0, 0, this.radius, 0, Math.PI * 2);
    ctx.fill();
    ctx.strokeStyle = this.isMe ? '#F4D97A' : '#991B1B';
    ctx.lineWidth = 3;
    ctx.stroke();
    
    // Aim direction
    ctx.rotate(this.aimAngle);
    
    // Gun
    ctx.fillStyle = '#333';
    ctx.fillRect(this.radius - 4, -4, 22, 8);
    ctx.fillStyle = '#D4AF37';
    ctx.fillRect(this.radius + 14, -3, 4, 6);
    
    // Visor
    ctx.fillStyle = this.isMe ? '#F4D97A' : '#DC2626';
    ctx.beginPath();
    ctx.arc(0, 0, this.radius * 0.45, 0, Math.PI * 2);
    ctx.fill();
    
    ctx.restore();
    
    // Name tag (for remote players)
    if (!this.isMe) {
      ctx.save();
      ctx.font = 'bold 11px Cairo, sans-serif';
      ctx.fillStyle = '#fff';
      ctx.textAlign = 'center';
      ctx.strokeStyle = '#000';
      ctx.lineWidth = 3;
      ctx.strokeText(this.userName, this.x, this.y - this.radius - 12);
      ctx.fillText(this.userName, this.x, this.y - this.radius - 12);
      ctx.restore();
    }
    
    // HP bar
    if (!this.isMe && this.hp < this.maxHp) {
      const barW = 34;
      const barX = this.x - barW / 2;
      const barY = this.y - this.radius - 20;
      ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
      ctx.fillRect(barX - 1, barY - 1, barW + 2, 6);
      const hpPct = this.hp / this.maxHp;
      ctx.fillStyle = hpPct > 0.5 ? '#10B981' : hpPct > 0.25 ? '#F59E0B' : '#DC2626';
      ctx.fillRect(barX, barY, barW * hpPct, 4);
    }
  }
}
