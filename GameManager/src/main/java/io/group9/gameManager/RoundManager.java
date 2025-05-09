package io.group9.gameManager;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import io.group9.CoreResources;

import java.lang.reflect.Field;


public final class RoundManager extends EntitySystem {

    private static final float INTRO_TIME = 5f;
    private static final float ROUND_TIME = 60f;
    private static final float END_FREEZE = 3f;

    private static final Vector2 PLAYER_SPAWN = new Vector2(100, 150);
    private static final Vector2 ENEMY_SPAWN  = new Vector2(500, 150);

    private enum Phase { INTRO, FIGHT, ROUND_END }
    private Phase phase = Phase.INTRO;
    private float phaseTimer = INTRO_TIME;

    private int roundNo = 1;
    private int playerWins = 0, enemyWins = 0;

    private boolean fightStarted = false;

    public RoundManager(int priority) {
        super(priority);
    }

    @Override
    public void update(float dt) {
        phaseTimer -= dt;

        if(!fightStarted && phase == Phase.INTRO && phaseTimer <= 1f) {
            fightStarted = true;
            CoreResources.setRoundFrozen(true);
        }

        switch (phase) {
            case INTRO:
                if (phaseTimer <= 0f) {
                    CoreResources.setRoundFrozen(false);
                    phase = Phase.FIGHT;
                    phaseTimer = ROUND_TIME;
                }
                break;

            case FIGHT:
                if (phaseTimer <= 0f || someoneDied()) {
                    phase = Phase.ROUND_END;
                    phaseTimer = END_FREEZE;
                    CoreResources.setRoundFrozen(true);
                    freezeAllBodies();
                }
                break;

            case ROUND_END:
                if (phaseTimer <= 0f) {
                    if (playerWins == 2 || enemyWins == 2) {
                        playerWins = enemyWins = 0;
                        roundNo = 1;
                    } else {
                        roundNo++;
                    }
                    startNextRound();
                }
                break;
        }
    }

    private boolean someoneDied() {
        if (CoreResources.getPlayerHealth() <= 0 || CoreResources.getPlayerBody().getPosition().y < -1) {
            CoreResources.setPlayerHealth(0);
            enemyWins++;
            return true;
        }
        if (CoreResources.getEnemyHealth() <= 0 || CoreResources.getEnemyBody().getPosition().y < -1) {
            CoreResources.setEnemyHealth(0);
            playerWins++;
            return true;
        }
        return false;
    }

    public void freezeAllBodies() {
        for (Body b : new Body[]{
            CoreResources.getPlayerBody(),
            CoreResources.getEnemyBody()
        }) {
            if (b == null) continue;
            b.setLinearVelocity(0f, 0f);
            b.setAngularVelocity(0f);
            Object comp = b.getUserData();
            if (comp != null) {
                try {
                    Field f = comp.getClass().getDeclaredField("needsFreeze");
                    f.setAccessible(true);
                    f.setBoolean(comp, true);
                } catch (Exception ignored) {}
            }
        }
    }

    void startNextRound() {
        // figure out who actually died
        boolean playerDied = CoreResources.getPlayerHealth() <= 0;
        boolean enemyDied  = CoreResources.getEnemyHealth()  <= 0;

        Body pBody = CoreResources.getPlayerBody();
        Body eBody = CoreResources.getEnemyBody();

        respawnPlayer(pBody, PLAYER_SPAWN);
        respawnEnemy(eBody, ENEMY_SPAWN, enemyDied);

        phase = Phase.INTRO;
        phaseTimer = INTRO_TIME;
        fightStarted = false;
        CoreResources.setRoundFrozen(true);
    }

    private void respawnPlayer(Body body, Vector2 spawnPoint) {
        if (body == null) return;

        // CoreResources health
        CoreResources.setPlayerHealth(100);

        Object comp = body.getUserData();
        if (comp != null) {
            Class<?> cls = comp.getClass();

            safelySetInt(comp, cls, "health", 100);
            int maxJ = safelyGetInt(comp, cls, "maxJumps", 0);
            safelySetInt(comp, cls, "jumpsLeft", maxJ);

            clearBooleanFlagsContaining(comp, cls, "jump");
            safelySetEnum(comp, cls, "state", "IDLE");
            for (String f : new String[]{"attacking","attackRequested","isHurt","needsFreeze"})
                safelySetBoolean(comp, cls, f, false);
            for (String t : new String[]{"reactionTimer","recalcTimer","hurtTimer","animTime"})
                safelySetFloat(comp, cls, t, 0f);

            // Force “on ground” so player always starts able to jump
            safelySetInt(comp, cls, "groundContacts", 1);
            safelySetBoolean(comp, cls, "wasGrounded",   true);

            safelySetBoolean(comp, cls, "facingLeft", false);
        }

        // Physics & position
        resetBodyPhysics(body);
        body.setTransform(
            spawnPoint.x / CoreResources.PPM,
            spawnPoint.y / CoreResources.PPM,
            0f
        );

        Gdx.app.log("RoundManager", "Player respawned");
    }

    private void respawnEnemy(Body body, Vector2 spawnPoint, boolean died) {
        if (body == null) return;

        // CoreResources health
        CoreResources.setEnemyHealth(100);

        Object comp = body.getUserData();
        int maxJ = 0;
        if (comp != null) {
            Class<?> cls = comp.getClass();

            // Reset reflected fields
            safelySetInt(comp, cls, "health", 100);
            maxJ = safelyGetInt(comp, cls, "maxJumps", 0);
            safelySetInt(comp, cls, "jumpsLeft", maxJ);

            clearBooleanFlagsContaining(comp, cls, "jump");
            safelySetEnum(comp, cls, "state", "IDLE");
            for (String f : new String[]{"attacking","attackRequested","isHurt","needsFreeze"})
                safelySetBoolean(comp, cls, f, false);
            for (String t : new String[]{"reactionTimer","recalcTimer","hurtTimer","animTime"})
                safelySetFloat(comp, cls, t, 0f);

            // Ground logic differs if it actually died vs. if it won
            if (died) {
                safelySetInt(comp, cls, "groundContacts", 0);
                safelySetBoolean(comp, cls, "wasGrounded",   false);
            } else {
                safelySetInt(comp, cls, "groundContacts", 1);
                safelySetBoolean(comp, cls, "wasGrounded",   true);
            }

            // Facing direction
            safelySetBoolean(comp, cls, "facingLeft", true);
        }

        // Physics & position
        resetBodyPhysics(body);
        body.setTransform(
            spawnPoint.x / CoreResources.PPM,
            spawnPoint.y / CoreResources.PPM,
            0f
        );

        Gdx.app.log("RoundManager",
            "Enemy respawned (maxJumps=" + maxJ + ", died=" + died + ")");
    }

    private void safelySetInt(Object comp, Class<?> cls, String name, int val) {
        try {
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            f.setInt(comp, val);
        } catch (Exception ignored) {}
    }
    private int safelyGetInt(Object comp, Class<?> cls, String name, int def) {
        try {
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            return f.getInt(comp);
        } catch (Exception e) {
            return def;
        }
    }
    private void safelySetFloat(Object comp, Class<?> cls, String name, float val) {
        try {
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            f.setFloat(comp, val);
        } catch (Exception ignored) {}
    }
    private void safelySetBoolean(Object comp, Class<?> cls, String name, boolean val) {
        try {
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            f.setBoolean(comp, val);
        } catch (Exception ignored) {}
    }
    private void safelySetEnum(Object comp, Class<?> cls, String name, String enumVal) {
        try {
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            Class<? extends Enum> type = (Class<? extends Enum>)f.getType();
            f.set(comp, Enum.valueOf(type, enumVal));
        } catch (Exception ignored) {}
    }
    private void clearBooleanFlagsContaining(Object comp, Class<?> cls, String substring) {
        for (Field f : cls.getDeclaredFields()) {
            if (f.getType() == boolean.class
                && f.getName().toLowerCase().contains(substring)) {
                try {
                    f.setAccessible(true);
                    f.setBoolean(comp, false);
                } catch (Exception ignored) {}
            }
        }
    }
    private void resetBodyPhysics(Body body) {
        body.setType(BodyDef.BodyType.DynamicBody);
        body.setGravityScale(1f);
        body.setLinearVelocity(0f, 0f);
        body.setAngularVelocity(0f);
        body.setSleepingAllowed(false);
        body.setAwake(true);
    }
    public String getIntroCue() {
        if (phase != Phase.INTRO) return null;
        if (phaseTimer > 4f)
            return roundNo == 3 ? "FINAL ROUND" : "ROUND " + roundNo;
        if (phaseTimer > 3f) return "3";
        if (phaseTimer > 2f) return "2";
        if (phaseTimer > 1f) return "1";
        return "FIGHT";
    }

    public void reset(){
        this.phase = Phase.INTRO;
        this.phaseTimer = INTRO_TIME;
        this.roundNo = 1;
        this.playerWins = 0;
        this.enemyWins = 0;
        fightStarted = false;

        CoreResources.setRoundFrozen(true);

    }

    public float getRoundTimer() {
        return phase == Phase.FIGHT ? phaseTimer : ROUND_TIME;
    }

    public int getRoundNumber() {
        return roundNo;
    }
}
