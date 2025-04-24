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
    private static final Vector2 ENEMY_SPAWN  = new Vector2(200, 150);

    private enum Phase { INTRO, FIGHT, ROUND_END }
    private Phase phase = Phase.INTRO;
    private float phaseTimer = INTRO_TIME;

    private int roundNo = 1;
    private int playerWins = 0, enemyWins = 0;

    public RoundManager(int priority) {
        super(priority);
    }

    @Override
    public void update(float dt) {
        phaseTimer -= dt;
        switch (phase) {
            case INTRO:
                if (phaseTimer <= 0f) {
                    CoreResources.setRoundFrozen(false);
                    phase      = Phase.FIGHT;
                    phaseTimer = ROUND_TIME;
                }
                break;

            case FIGHT:
                if (phaseTimer <= 0f || someoneDied()) {
                    phase      = Phase.ROUND_END;
                    phaseTimer = END_FREEZE;
                    CoreResources.setRoundFrozen(true);
                    freezeAllBodies();
                }
                break;

            case ROUND_END:
                if (phaseTimer <= 0f) {
                    if (playerWins == 2 || enemyWins == 2) {
                        playerWins = enemyWins = 0;
                        roundNo     = 1;
                    } else {
                        roundNo++;
                    }
                    startNextRound();
                }
                break;
        }
    }

    private boolean someoneDied() {
        if (CoreResources.getPlayerHealth() <= 0) {
            enemyWins++;
            return true;
        }
        if (CoreResources.getEnemyHealth() <= 0) {
            playerWins++;
            return true;
        }
        return false;
    }

    private void startNextRound() {
        Body pBody = CoreResources.getPlayerBody();
        Body eBody = CoreResources.getEnemyBody();

        respawn(pBody, PLAYER_SPAWN, true);
        respawn(eBody, ENEMY_SPAWN, false);

        phase      = Phase.INTRO;
        phaseTimer = INTRO_TIME;
        CoreResources.setRoundFrozen(true);

    }

    private void freezeAllBodies() {
        for (Body b : new Body[]{ CoreResources.getPlayerBody(),
            CoreResources.getEnemyBody() }) {
            if (b == null) continue;
            b.setLinearVelocity(0f, 0f);
            b.setAngularVelocity(0f);
            Object comp = b.getUserData();
            if (comp != null) {
                try {
                    Field f = comp.getClass()
                        .getDeclaredField("needsFreeze");
                    f.setAccessible(true);
                    f.setBoolean(comp, true);
                } catch (Exception ignored) {}
            }
        }
    }

    private void respawn(Body body, Vector2 spawnPoint, boolean isPlayer) {
        if (body == null) return;
        Object comp = body.getUserData();
        if (comp == null) return;

        try {
            Class<?> cls = comp.getClass();

            // Health
            Field healthF = cls.getDeclaredField("health");
            healthF.setAccessible(true);
            healthF.setInt(comp, 100);

            // Jumps
            Field maxJumpsF  = cls.getDeclaredField("maxJumps");
            Field jumpsLeftF = cls.getDeclaredField("jumpsLeft");
            maxJumpsF.setAccessible(true);
            jumpsLeftF.setAccessible(true);
            int maxJumps = maxJumpsF.getInt(comp);
            jumpsLeftF.setInt(comp, maxJumps);

            for (Field f : cls.getDeclaredFields()) {
                if (f.getType() == boolean.class
                    && f.getName().toLowerCase().contains("jump")) {
                    f.setAccessible(true);
                    f.setBoolean(comp, false);
                }
            }

            // Reset state to IDLE
            Field stateF = cls.getDeclaredField("state");
            stateF.setAccessible(true);
            @SuppressWarnings("unchecked")
            Class<Enum> stType = (Class<Enum>) stateF.getType();
            stateF.set(comp, Enum.valueOf(stType, "IDLE"));


            for (String flag : new String[]{
                "attacking", "attackRequested", "isHurt", "needsFreeze"
            }) {
                try {
                    Field f = cls.getDeclaredField(flag);
                    f.setAccessible(true);
                    f.setBoolean(comp, false);
                } catch (Exception ignored) {}
            }
            for (String tm : new String[]{"reactionTimer","recalcTimer"}) {
                try {
                    Field f = cls.getDeclaredField(tm);
                    f.setAccessible(true);
                    f.setFloat(comp, 0f);
                } catch (Exception ignored) {}
            }

            try {
                Field f = cls.getDeclaredField("facingLeft");
                f.setAccessible(true);
                f.setBoolean(comp, isPlayer ? false : true);
            } catch (Exception ignored) {}

            try {
                Field f = cls.getDeclaredField("groundContacts");
                f.setAccessible(true);
                f.setInt(comp, 1);
            } catch (Exception ignored) {}
            try {
                Field f = cls.getDeclaredField("wasGrounded");
                f.setAccessible(true);
                f.setBoolean(comp, true);
            } catch (Exception ignored) {}

            body.setType(BodyDef.BodyType.DynamicBody);
            body.setGravityScale(1f);
            body.setLinearVelocity(0f, 0f);
            body.setAngularVelocity(0f);

            body.setTransform(
                spawnPoint.x / CoreResources.PPM,
                spawnPoint.y / CoreResources.PPM,
                0f
            );

            if (isPlayer) CoreResources.setPlayerHealth(100);
            else          CoreResources.setEnemyHealth (100);

            Gdx.app.log("RoundManager",
                (isPlayer ? "Player" : "Enemy")
                    + " respawned: jumpsLeft="
                    + jumpsLeftF.getInt(comp)
                    + "/" + maxJumps
            );

        } catch (Exception e) {
            Gdx.app.error("RoundManager", "respawn(): reflection failed", e);
        }
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

    public float getRoundTimer() {
        return phase == Phase.FIGHT ? phaseTimer : ROUND_TIME;
    }

    public int getRoundNumber() {
        return roundNo;
    }
}
