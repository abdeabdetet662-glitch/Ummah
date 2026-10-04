/* ═══════════════════════════════════════════════
   Weapons
   ═══════════════════════════════════════════════ */

const WEAPONS = {
  pistol: {
    name: 'مسدس',
    icon: '🔫',
    damage: 20,
    fireRate: 5,
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
    damage: 15,
    fireRate: 1.2,
    bulletSpeed: 600,
    maxAmmo: 8,
    reloadTime: 2.2,
    spread: 0.5,
    pellets: 6,
    bulletColor: '#FF8C00',
    bulletSize: 4
  },
  sniper: {
    name: 'قناصة',
    icon: '🎯',
    damage: 75,
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

const WEAPON_KEYS = Object.keys(WEAPONS);
