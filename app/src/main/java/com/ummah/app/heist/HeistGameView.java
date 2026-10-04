package com.ummah.app.heist;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * HeistGameView — محرك اللعبة (Canvas Native)
 *
 * يحتوي:
 *  - اللاعب (أنت)
 *  - شرطة (Bots)
 *  - رصاص
 *  - خزنة/مال
 *  - منطقة الهروب
 *  - Joystick
 *  - زر الإطلاق
 */
public class HeistGameView extends View {

    // ═══ Callbacks ═══
    public interface GameListener {
        void onGameUpdate(int hp, int money, int alivePolice, long timeLeft);
        void onGameEnd(boolean won, int moneyCollected, int kills, long duration);
        void onObjective(String text);
    }

    // ═══ World ═══
    public static final int WORLD_W = 2400;
    public static final int WORLD_H = 1600;

    private static final float PLAYER_RADIUS = 28f;
    private static final float POLICE_RADIUS = 26f;
    private static final float BULLET_RADIUS = 6f;

    private static final float PLAYER_SPEED = 320f;    // px/sec
    private static final float POLICE_SPEED = 180f;
    private static final float BULLET_SPEED = 900f;

    private static final long GAME_DURATION_MS = 3 * 60 * 1000L; // 3 دقائق
    private static final int MAX_POLICE = 8;

    // ═══ State ═══
    private HeistRole myRole = HeistRole.LEADER;
    private GameListener listener;

    // Player
    private float playerX, playerY;
    private float playerAimAngle = 0f;
    private int playerHp = 100;
    private int playerMaxHp = 100;
    private boolean playerAlive = true;
    private float playerSpeedMult = 1.0f;
    private long playerFireCooldown = 0;
    private int playerAmmo = 30;
    private int playerMaxAmmo = 30;
    private long playerReloadAt = 0;
    private int playerDamage = 25;

    // Police
    private static class Police {
        float x, y;
        int hp = 50;
        int maxHp = 50;
        float aimAngle = 0;
        long fireCooldown = 0;
        boolean alive = true;
        float patrolTargetX, patrolTargetY;
        long lastSeenPlayer = 0;
    }

    // Bullets
    private static class Bullet {
        float x, y, vx, vy;
        boolean fromPlayer;
        float size = 4f;
        int damage = 15;
        long life = 2000;
    }

    // Effects
    private static class Particle {
        float x, y, vx, vy, size;
        long life, maxLife;
        int color;
    }

    // Money piles
    private static class MoneyPile {
        float x, y;
        int amount;
        boolean collected = false;
    }

    private final List<Police> polices = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<MoneyPile> moneys = new ArrayList<>();

    // Camera
    private float camX = 0, camY = 0;

    // Input
    private boolean joystickActive = false;
    private float joyBaseX, joyBaseY, joyKnobX, joyKnobY, joyDX, joyDY;
    private boolean shooting = false;
    private boolean shootBtnPressed = false;
    private float shootBtnX, shootBtnY;

    // Timing
    private long startTime = 0;
    private long lastFrame = 0;
    private boolean running = false;

    // Money
    private int moneyCollected = 0;
    private int kills = 0;

    // Random
    private final Random random = new Random();

    // Paints
    private final Paint pBg = new Paint();
    private final Paint pGrid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pPlayer = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pPolice = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pBullet = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pUI = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pMoney = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pJoyBase = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pJoyKnob = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            if (!running) return;
            long now = System.currentTimeMillis();
            float dt = Math.min((now - lastFrame) / 1000f, 0.05f);
            lastFrame = now;
            update(dt);
            invalidate();
            handler.postDelayed(this, 16); // ~60 FPS
        }
    };

    public HeistGameView(Context c) { super(c); init(); }
    public HeistGameView(Context c, AttributeSet a) { super(c, a); init(); }

    // ═══════════════════════════════════════════
    //  INIT
    // ═══════════════════════════════════════════
    private void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);

        pBg.setColor(Color.parseColor("#0a0510"));
        pGrid.setColor(Color.parseColor("#20D4AF37"));
        pGrid.setStrokeWidth(1f);
        pText.setColor(Color.WHITE);
        pText.setTextSize(28f);
        pText.setFakeBoldText(true);
        pUI.setColor(Color.parseColor("#D4AF37"));
        pMoney.setColor(Color.parseColor("#10B981"));

        pJoyBase.setColor(Color.parseColor("#30D4AF37"));
        pJoyBase.setStyle(Paint.Style.STROKE);
        pJoyBase.setStrokeWidth(6f);

        pJoyKnob.setColor(Color.parseColor("#D4AF37"));
    }

    public void setRole(HeistRole role) {
        this.myRole = role;
        this.playerMaxHp = role.baseHp;
        this.playerHp = role.baseHp;
        this.playerSpeedMult = role.speedMultiplier;
        this.playerDamage = role.weaponDamage;
        this.playerMaxAmmo = 30;
        this.playerAmmo = 30;
        pPlayer.setColor(Color.parseColor(role.colorHex));
    }

    public void setListener(GameListener l) { this.listener = l; }

    // ═══════════════════════════════════════════
    //  START GAME
    // ═══════════════════════════════════════════
    public void startGame() {
        playerX = 200;
        playerY = WORLD_H - 200;
        playerHp = playerMaxHp;
        playerAlive = true;
        playerAmmo = playerMaxAmmo;
        moneyCollected = 0;
        kills = 0;
        bullets.clear();
        particles.clear();
        polices.clear();
        moneys.clear();

        // Spawn police
        for (int i = 0; i < MAX_POLICE; i++) {
            Police p = new Police();
            p.x = 600 + random.nextInt(WORLD_W - 800);
            p.y = 200 + random.nextInt(WORLD_H - 600);
            p.patrolTargetX = p.x;
            p.patrolTargetY = p.y;
            polices.add(p);
        }

        // Spawn money (hidden in chest area — center)
        float chestX = WORLD_W / 2f;
        float chestY = WORLD_H / 2f;
        for (int i = 0; i < 5; i++) {
            MoneyPile m = new MoneyPile();
            m.x = chestX + (random.nextFloat() - 0.5f) * 200;
            m.y = chestY + (random.nextFloat() - 0.5f) * 200;
            m.amount = 20_000; // كل واحدة 20,000 Đ
            moneys.add(m);
        }

        startTime = System.currentTimeMillis();
        lastFrame = startTime;
        running = true;
        handler.post(ticker);
    }

    public void stopGame() {
        running = false;
        handler.removeCallbacks(ticker);
    }

    // ═══════════════════════════════════════════
    //  UPDATE
    // ═══════════════════════════════════════════
    private void update(float dt) {
        if (!playerAlive) return;

        long now = System.currentTimeMillis();
        long elapsed = now - startTime;
        long timeLeft = GAME_DURATION_MS - elapsed;

        if (timeLeft <= 0) {
            endGame(false);
            return;
        }

        // ═══ Player movement ═══
        float speed = PLAYER_SPEED * playerSpeedMult;
        float mag = (float) Math.sqrt(joyDX * joyDX + joyDY * joyDY);

        if (mag > 0.15f) {
            float nx = joyDX / mag;
            float ny = joyDY / mag;
            playerX += nx * speed * dt;
            playerY += ny * speed * dt;
            playerAimAngle = (float) Math.atan2(ny, nx);
        }

        // Bounds
        playerX = Math.max(PLAYER_RADIUS, Math.min(WORLD_W - PLAYER_RADIUS, playerX));
        playerY = Math.max(PLAYER_RADIUS, Math.min(WORLD_H - PLAYER_RADIUS, playerY));

        // ═══ Shooting ═══
        playerFireCooldown -= (long)(dt * 1000);
        if (playerReloadAt > 0 && now >= playerReloadAt) {
            playerAmmo = playerMaxAmmo;
            playerReloadAt = 0;
        }

        if (shooting && playerAmmo > 0 && playerFireCooldown <= 0 && playerReloadAt == 0) {
            shootPlayer();
            playerFireCooldown = 180; // ms
        }

        if (playerAmmo <= 0 && playerReloadAt == 0) {
            playerReloadAt = now + 1500;
        }

        // ═══ Police AI ═══
        for (Police p : polices) {
            if (!p.alive) continue;
            updatePolice(p, dt, now);
        }

        // ═══ Bullets ═══
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.x += b.vx * dt;
            b.y += b.vy * dt;
            b.life -= (long)(dt * 1000);

            boolean hit = false;

            if (b.fromPlayer) {
                // Hit police?
                for (Police p : polices) {
                    if (!p.alive) continue;
                    float dx = p.x - b.x;
                    float dy = p.y - b.y;
                    if (dx * dx + dy * dy < (POLICE_RADIUS + BULLET_RADIUS) *
                        (POLICE_RADIUS + BULLET_RADIUS)) {
                        p.hp -= b.damage;
                        spawnBlood(b.x, b.y);
                        if (p.hp <= 0) {
                            p.alive = false;
                            kills++;
                            spawnExplosion(p.x, p.y);
                        }
                        hit = true;
                        break;
                    }
                }
            } else {
                // Hit player?
                float dx = playerX - b.x;
                float dy = playerY - b.y;
                if (dx * dx + dy * dy < (PLAYER_RADIUS + BULLET_RADIUS) *
                    (PLAYER_RADIUS + BULLET_RADIUS)) {
                    playerHp -= b.damage;
                    spawnBlood(b.x, b.y);
                    if (playerHp <= 0) {
                        playerHp = 0;
                        playerAlive = false;
                        endGame(false);
                        return;
                    }
                    hit = true;
                }
            }

            if (hit || b.life <= 0 ||
                b.x < 0 || b.x > WORLD_W || b.y < 0 || b.y > WORLD_H) {
                bullets.remove(i);
            }
        }

        // ═══ Money pickup ═══
        for (MoneyPile m : moneys) {
            if (m.collected) continue;
            float dx = m.x - playerX;
            float dy = m.y - playerY;
            if (dx * dx + dy * dy < (PLAYER_RADIUS + 30) * (PLAYER_RADIUS + 30)) {
                m.collected = true;
                moneyCollected += m.amount;
                spawnMoneyEffect(m.x, m.y);
                if (listener != null) {
                    listener.onObjective("💰 +" + m.amount + " Đ");
                }

                // نتحققو إذا جمعنا كل المال
                boolean allCollected = true;
                for (MoneyPile mm : moneys) if (!mm.collected) allCollected = false;
                if (allCollected && listener != null) {
                    listener.onObjective("🎯 كل المال تم جمعه! اهرب للخارج!");
                }
            }
        }

        // ═══ Particles ═══
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.vx *= 0.94f;
            p.vy *= 0.94f;
            p.life -= (long)(dt * 1000);
            if (p.life <= 0) particles.remove(i);
        }

        // ═══ Camera ═══
        float targetCamX = playerX - getWidth() / 2f;
        float targetCamY = playerY - getHeight() / 2f;
        camX += (targetCamX - camX) * 0.12f;
        camY += (targetCamY - camY) * 0.12f;
        camX = Math.max(0, Math.min(WORLD_W - getWidth(), camX));
        camY = Math.max(0, Math.min(WORLD_H - getHeight(), camY));

        // ═══ Update Listener ═══
        if (listener != null) {
            int alivePolice = 0;
            for (Police p : polices) if (p.alive) alivePolice++;
            listener.onGameUpdate(playerHp, moneyCollected, alivePolice, timeLeft);
        }

        // ═══ Win check ═══
        if (moneyCollected >= 100_000) {
            endGame(true);
        }
    }

    private void updatePolice(Police p, float dt, long now) {
        float dx = playerX - p.x;
        float dy = playerY - p.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < 400) {
            // Chase
            float nx = dx / dist;
            float ny = dy / dist;
            p.x += nx * POLICE_SPEED * dt;
            p.y += ny * POLICE_SPEED * dt;
            p.aimAngle = (float) Math.atan2(ny, nx);
            p.lastSeenPlayer = now;

            // Shoot
            if (now - p.fireCooldown > 800 && dist < 350) {
                shootPolice(p);
                p.fireCooldown = now;
            }
        } else {
            // Patrol
            if (Math.abs(p.x - p.patrolTargetX) < 20 &&
                Math.abs(p.y - p.patrolTargetY) < 20) {
                p.patrolTargetX = 200 + random.nextInt(WORLD_W - 400);
                p.patrolTargetY = 200 + random.nextInt(WORLD_H - 400);
            }
            float pdx = p.patrolTargetX - p.x;
            float pdy = p.patrolTargetY - p.y;
            float pd = (float) Math.sqrt(pdx * pdx + pdy * pdy);
            if (pd > 5) {
                p.x += (pdx / pd) * POLICE_SPEED * 0.5f * dt;
                p.y += (pdy / pd) * POLICE_SPEED * 0.5f * dt;
                p.aimAngle = (float) Math.atan2(pdy, pdx);
            }
        }
    }

    private void shootPlayer() {
        Bullet b = new Bullet();
        float spread = 0.06f;
        float angle = playerAimAngle + (random.nextFloat() - 0.5f) * spread;
        b.x = playerX + (float) Math.cos(angle) * (PLAYER_RADIUS + 10);
        b.y = playerY + (float) Math.sin(angle) * (PLAYER_RADIUS + 10);
        b.vx = (float) Math.cos(angle) * BULLET_SPEED;
        b.vy = (float) Math.sin(angle) * BULLET_SPEED;
        b.fromPlayer = true;
        b.damage = playerDamage;
        bullets.add(b);
        playerAmmo--;

        // Muzzle flash
        for (int i = 0; i < 4; i++) {
            Particle pt = new Particle();
            float a = angle + (random.nextFloat() - 0.5f) * 0.6f;
            pt.x = b.x;
            pt.y = b.y;
            pt.vx = (float) Math.cos(a) * 250;
            pt.vy = (float) Math.sin(a) * 250;
            pt.size = 3f;
            pt.life = 120;
            pt.maxLife = 120;
            pt.color = Color.parseColor("#F4D97A");
            particles.add(pt);
        }
    }

    private void shootPolice(Police p) {
        Bullet b = new Bullet();
        float spread = 0.15f;
        float angle = p.aimAngle + (random.nextFloat() - 0.5f) * spread;
        b.x = p.x + (float) Math.cos(angle) * (POLICE_RADIUS + 8);
        b.y = p.y + (float) Math.sin(angle) * (POLICE_RADIUS + 8);
        b.vx = (float) Math.cos(angle) * BULLET_SPEED * 0.8f;
        b.vy = (float) Math.sin(angle) * BULLET_SPEED * 0.8f;
        b.fromPlayer = false;
        b.damage = 12;
        bullets.add(b);
    }

    private void spawnBlood(float x, float y) {
        for (int i = 0; i < 6; i++) {
            Particle p = new Particle();
            float a = random.nextFloat() * (float)Math.PI * 2;
            float s = 80 + random.nextFloat() * 120;
            p.x = x;
            p.y = y;
            p.vx = (float) Math.cos(a) * s;
            p.vy = (float) Math.sin(a) * s;
            p.size = 3f + random.nextFloat() * 3f;
            p.life = 500;
            p.maxLife = 500;
            p.color = Color.parseColor("#DC2626");
            particles.add(p);
        }
    }

    private void spawnExplosion(float x, float y) {
        for (int i = 0; i < 20; i++) {
            Particle p = new Particle();
            float a = random.nextFloat() * (float)Math.PI * 2;
            float s = 100 + random.nextFloat() * 250;
            p.x = x;
            p.y = y;
            p.vx = (float) Math.cos(a) * s;
            p.vy = (float) Math.sin(a) * s;
            p.size = 4f + random.nextFloat() * 4f;
            p.life = 800;
            p.maxLife = 800;
            p.color = random.nextBoolean()
                ? Color.parseColor("#DC2626")
                : Color.parseColor("#F59E0B");
            particles.add(p);
        }
    }

    private void spawnMoneyEffect(float x, float y) {
        for (int i = 0; i < 15; i++) {
            Particle p = new Particle();
            float a = random.nextFloat() * (float)Math.PI * 2;
            float s = 100 + random.nextFloat() * 150;
            p.x = x;
            p.y = y;
            p.vx = (float) Math.cos(a) * s;
            p.vy = (float) Math.sin(a) * s;
            p.size = 4f;
            p.life = 900;
            p.maxLife = 900;
            p.color = Color.parseColor("#10B981");
            particles.add(p);
        }
    }

    private void endGame(boolean won) {
        running = false;
        handler.removeCallbacks(ticker);
        long duration = (System.currentTimeMillis() - startTime) / 1000;
        if (listener != null) {
            listener.onGameEnd(won, moneyCollected, kills, duration);
        }
    }

    // ═══════════════════════════════════════════
    //  RENDER
    // ═══════════════════════════════════════════
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#0a0510"));

        canvas.save();
        canvas.translate(-camX, -camY);

        // Grid
        drawGrid(canvas);

        // Chest outline (in the middle)
        drawChest(canvas);

        // Money piles
        for (MoneyPile m : moneys) {
            if (!m.collected) {
                pMoney.setColor(Color.parseColor("#10B981"));
                canvas.drawCircle(m.x, m.y, 22, pMoney);
                pText.setColor(Color.parseColor("#0a0510"));
                pText.setTextSize(24);
                pText.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("💰", m.x, m.y + 8, pText);
            }
        }

        // Police
        for (Police p : polices) {
            if (!p.alive) continue;
            drawPolice(canvas, p);
        }

        // Bullets
        for (Bullet b : bullets) {
            pBullet.setColor(b.fromPlayer
                ? Color.parseColor("#F4D97A")
                : Color.parseColor("#FF4444"));
            canvas.drawCircle(b.x, b.y, BULLET_RADIUS, pBullet);
        }

        // Particles
        for (Particle p : particles) {
            int alpha = (int)(255f * p.life / p.maxLife);
            pBullet.setColor(p.color);
            pBullet.setAlpha(alpha);
            canvas.drawCircle(p.x, p.y, p.size, pBullet);
        }
        pBullet.setAlpha(255);

        // Player
        if (playerAlive) {
            drawPlayer(canvas);
        }

        canvas.restore();

        // UI (لا يتحرك مع الكاميرا)
        drawHUD(canvas);
        drawJoystick(canvas);
        drawShootButton(canvas);
    }

    private void drawGrid(Canvas c) {
        int gs = 80;
        int startX = (int)(camX / gs) * gs;
        int startY = (int)(camY / gs) * gs;
        int endX = startX + getWidth() + gs;
        int endY = startY + getHeight() + gs;

        for (int x = startX; x < endX; x += gs) {
            c.drawLine(x, startY, x, endY, pGrid);
        }
        for (int y = startY; y < endY; y += gs) {
            c.drawLine(startX, y, endX, y, pGrid);
        }
    }

    private void drawChest(Canvas c) {
        float chestX = WORLD_W / 2f;
        float chestY = WORLD_H / 2f;
        pPlayer.setColor(Color.parseColor("#40D4AF37"));
        pPlayer.setStyle(Paint.Style.STROKE);
        pPlayer.setStrokeWidth(4f);
        canvas_drawRect(c, chestX - 200, chestY - 200, chestX + 200, chestY + 200, pPlayer);
        pPlayer.setStyle(Paint.Style.FILL);
    }

    private void canvas_drawRect(Canvas c, float l, float t, float r, float b, Paint p) {
        c.drawRect(l, t, r, b, p);
    }

    private void drawPolice(Canvas c, Police p) {
        // Shadow
        Paint sh = new Paint(Paint.ANTI_ALIAS_FLAG);
        sh.setColor(Color.parseColor("#40000000"));
        c.drawCircle(p.x, p.y + 8, POLICE_RADIUS * 1.1f, sh);

        // Body
        pPolice.setColor(Color.parseColor("#DC2626"));
        c.drawCircle(p.x, p.y, POLICE_RADIUS, pPolice);
        pPolice.setStyle(Paint.Style.STROKE);
        pPolice.setStrokeWidth(3f);
        pPolice.setColor(Color.parseColor("#7F1D1D"));
        c.drawCircle(p.x, p.y, POLICE_RADIUS, pPolice);
        pPolice.setStyle(Paint.Style.FILL);

        // Direction (gun)
        float gx = p.x + (float) Math.cos(p.aimAngle) * (POLICE_RADIUS + 12);
        float gy = p.y + (float) Math.sin(p.aimAngle) * (POLICE_RADIUS + 12);
        Paint gp = new Paint(Paint.ANTI_ALIAS_FLAG);
        gp.setColor(Color.parseColor("#333333"));
        c.drawCircle(gx, gy, 6, gp);

        // HP bar
        if (p.hp < p.maxHp) {
            float hp = p.hp / (float) p.maxHp;
            Paint hpBg = new Paint();
            hpBg.setColor(Color.parseColor("#80000000"));
            c.drawRect(p.x - 24, p.y - POLICE_RADIUS - 14,
                       p.x + 24, p.y - POLICE_RADIUS - 8, hpBg);
            Paint hpFg = new Paint();
            hpFg.setColor(hp > 0.5f ? Color.parseColor("#10B981") :
                          hp > 0.25f ? Color.parseColor("#F59E0B") :
                          Color.parseColor("#DC2626"));
            c.drawRect(p.x - 24, p.y - POLICE_RADIUS - 14,
                       p.x - 24 + 48 * hp, p.y - POLICE_RADIUS - 8, hpFg);
        }
    }

    private void drawPlayer(Canvas c) {
        // Shadow
        Paint sh = new Paint(Paint.ANTI_ALIAS_FLAG);
        sh.setColor(Color.parseColor("#60000000"));
        c.drawCircle(playerX, playerY + 10, PLAYER_RADIUS * 1.1f, sh);

        // Aura
        Paint aura = new Paint(Paint.ANTI_ALIAS_FLAG);
        aura.setColor(Color.parseColor("#30F4D97A"));
        c.drawCircle(playerX, playerY, PLAYER_RADIUS + 12, aura);

        // Body
        c.drawCircle(playerX, playerY, PLAYER_RADIUS, pPlayer);

        // Border
        pPlayer.setStyle(Paint.Style.STROKE);
        pPlayer.setStrokeWidth(3f);
        pPlayer.setColor(Color.parseColor("#F4D97A"));
        c.drawCircle(playerX, playerY, PLAYER_RADIUS, pPlayer);
        pPlayer.setStyle(Paint.Style.FILL);

        // Gun
        float gx = playerX + (float) Math.cos(playerAimAngle) * (PLAYER_RADIUS + 14);
        float gy = playerY + (float) Math.sin(playerAimAngle) * (PLAYER_RADIUS + 14);
        Paint gp = new Paint(Paint.ANTI_ALIAS_FLAG);
        gp.setColor(Color.parseColor("#D4AF37"));
        c.drawCircle(gx, gy, 8, gp);

        // Visor (يمثل الدور)
        Paint visor = new Paint(Paint.ANTI_ALIAS_FLAG);
        visor.setColor(Color.parseColor("#F4D97A"));
        c.drawCircle(playerX, playerY, PLAYER_RADIUS * 0.4f, visor);
    }

    private void drawHUD(Canvas c) {
        // Health bar (أسفل يسار)
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        // HP bg
        p.setColor(Color.parseColor("#80000000"));
        c.drawRoundRect(new RectF(30, getHeight() - 110, 280, getHeight() - 70), 12, 12, p);

        // HP fill
        float hpPct = playerHp / (float) playerMaxHp;
        int hpColor = hpPct > 0.5f ? Color.parseColor("#10B981") :
                      hpPct > 0.25f ? Color.parseColor("#F59E0B") :
                      Color.parseColor("#DC2626");
        p.setColor(hpColor);
        c.drawRoundRect(new RectF(30, getHeight() - 110,
                                   30 + 250 * hpPct, getHeight() - 70), 12, 12, p);

        // HP text
        pText.setColor(Color.WHITE);
        pText.setTextSize(22);
        pText.setTextAlign(Paint.Align.CENTER);
        c.drawText("❤️ " + playerHp, 155, getHeight() - 82, pText);

        // Ammo (فوق HP)
        p.setColor(Color.parseColor("#80000000"));
        c.drawRoundRect(new RectF(30, getHeight() - 160, 180, getHeight() - 125), 12, 12, p);
        pText.setColor(Color.parseColor("#D4AF37"));
        c.drawText("🔫 " + playerAmmo + " / " + playerMaxAmmo, 105, getHeight() - 138, pText);

        // Money (فوق)
        p.setColor(Color.parseColor("#80000000"));
        c.drawRoundRect(new RectF(getWidth() - 280, 80, getWidth() - 30, 130), 12, 12, p);
        pText.setColor(Color.parseColor("#10B981"));
        pText.setTextSize(22);
        c.drawText("💰 " + moneyCollected + " Đ", getWidth() - 155, 112, pText);

        // Timer (وسط فوق)
        long elapsed = System.currentTimeMillis() - startTime;
        long left = Math.max(0, GAME_DURATION_MS - elapsed) / 1000;
        long mm = left / 60;
        long ss = left % 60;
        p.setColor(Color.parseColor("#80DC2626"));
        c.drawRoundRect(new RectF(getWidth() / 2f - 80, 30, getWidth() / 2f + 80, 80), 12, 12, p);
        pText.setColor(Color.parseColor("#DC2626"));
        pText.setTextSize(28);
        c.drawText(String.format("%02d:%02d", mm, ss), getWidth() / 2f, 68, pText);

        // Kills (يمين فوق)
        p.setColor(Color.parseColor("#80000000"));
        c.drawRoundRect(new RectF(getWidth() - 130, 140, getWidth() - 30, 190), 12, 12, p);
        pText.setColor(Color.parseColor("#DC2626"));
        pText.setTextSize(22);
        c.drawText("💀 " + kills, getWidth() - 80, 172, pText);
    }

    private void drawJoystick(Canvas c) {
        float baseX = getWidth() - 180;
        float baseY = getHeight() - 180;
        float baseR = 130;

        if (!joystickActive) {
            joyBaseX = baseX;
            joyBaseY = baseY;
            joyKnobX = baseX;
            joyKnobY = baseY;
        }

        c.drawCircle(joyBaseX, joyBaseY, baseR, pJoyBase);
        c.drawCircle(joyBaseX, joyBaseY, 40, pJoyBase);
        c.drawCircle(joyKnobX, joyKnobY, 60, pJoyKnob);

        // Label
        pText.setColor(Color.parseColor("#80FFFFFF"));
        pText.setTextSize(14);
        pText.setTextAlign(Paint.Align.CENTER);
        c.drawText("حركة", joyBaseX, joyBaseY + baseR + 30, pText);
    }

    private void drawShootButton(Canvas c) {
        shootBtnX = 150;
        shootBtnY = getHeight() - 180;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(shootBtnPressed
            ? Color.parseColor("#80DC2626")
            : Color.parseColor("#40DC2626"));
        c.drawCircle(shootBtnX, shootBtnY, 90, p);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(6f);
        p.setColor(Color.parseColor("#DC2626"));
        c.drawCircle(shootBtnX, shootBtnY, 90, p);
        p.setStyle(Paint.Style.FILL);

        pText.setColor(Color.parseColor("#DC2626"));
        pText.setTextSize(60);
        pText.setTextAlign(Paint.Align.CENTER);
        c.drawText("🔥", shootBtnX, shootBtnY + 22, pText);

        pText.setTextSize(14);
        pText.setColor(Color.parseColor("#CCFFFFFF"));
        c.drawText("إطلاق", shootBtnX, shootBtnY + 120, pText);
    }

    // ═══════════════════════════════════════════
    //  INPUT
    // ═══════════════════════════════════════════
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                int idx = e.getActionIndex();
                handleTouchDown(e.getX(idx), e.getY(idx), e.getPointerId(idx));
                return true;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < e.getPointerCount(); i++) {
                    handleTouchMove(e.getX(i), e.getY(i), e.getPointerId(i));
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                int upIdx = e.getActionIndex();
                handleTouchUp(e.getX(upIdx), e.getY(upIdx), e.getPointerId(upIdx));
                return true;
        }
        return super.onTouchEvent(e);
    }

    private int joyPointerId = -1;
    private int shootPointerId = -1;

    private void handleTouchDown(float x, float y, int pid) {
        // Shoot button first (smaller area)
        float sdx = x - shootBtnX;
        float sdy = y - shootBtnY;
        if (sdx * sdx + sdy * sdy < 100 * 100) {
            shooting = true;
            shootBtnPressed = true;
            shootPointerId = pid;
            return;
        }

        // Joystick
        float baseX = getWidth() - 180;
        float baseY = getHeight() - 180;
        float jdx = x - baseX;
        float jdy = y - baseY;
        if (jdx * jdx + jdy * jdy < 200 * 200) {
            joystickActive = true;
            joyBaseX = x;
            joyBaseY = y;
            joyKnobX = x;
            joyKnobY = y;
            joyDX = 0;
            joyDY = 0;
            joyPointerId = pid;
        }
    }

    private void handleTouchMove(float x, float y, int pid) {
        if (pid == joyPointerId) {
            float dx = x - joyBaseX;
            float dy = y - joyBaseY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            float maxDist = 100;
            if (dist > maxDist) {
                dx = dx / dist * maxDist;
                dy = dy / dist * maxDist;
                dist = maxDist;
            }
            joyKnobX = joyBaseX + dx;
            joyKnobY = joyBaseY + dy;
            joyDX = dx / maxDist;
            joyDY = dy / maxDist;
        }
    }

    private void handleTouchUp(float x, float y, int pid) {
        if (pid == joyPointerId) {
            joystickActive = false;
            joyPointerId = -1;
            joyDX = 0;
            joyDY = 0;
        }
        if (pid == shootPointerId) {
            shooting = false;
            shootBtnPressed = false;
            shootPointerId = -1;
        }
    }

    // ═══ Override to make sure we get touch events properly ═══
    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
