package io.group9.gameManager;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import data.util.CoreResources;
import locators.EnemyServiceLocator;
import locators.PlayerServiceLocator;
import services.enemy.IEnemyService;
import services.player.IPlayerService;
import java.util.Arrays;



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

    private final IPlayerService playerSvc = PlayerServiceLocator.get();
    private final IEnemyService enemySvc = EnemyServiceLocator.get();

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
        Body pBody = playerSvc.getPlayerBody();
        int  pHealth = playerSvc.getHealth();
        if (pHealth <= 0 || (pBody != null && pBody.getPosition().y < -1)) {
            playerSvc.setHealth(0);
            enemyWins++;
            return true;
        }
        if (enemySvc.getHealth() <= 0 || enemySvc.getEnemyBody().getPosition().y < -1) {
            enemySvc.setHealth(0);
            playerWins++;
            return true;
        }
        return false;
    }

    public void freezeAllBodies() {
        Body pBody = playerSvc.getPlayerBody();
        Body eBody = enemySvc.getEnemyBody();
        for (Body b : Arrays.asList(pBody, eBody)) {
            if (b == null) continue;
            b.setLinearVelocity(0f, 0f);
            b.setAngularVelocity(0f);
        }

        playerSvc.freezeForRound();
        enemySvc .freezeForRound();
    }

    void startNextRound() {
        boolean playerDied = playerSvc.getHealth() <= 0;
        boolean enemyDied  = enemySvc .getHealth() <= 0;

        playerSvc.resetForRound(PLAYER_SPAWN);
        enemySvc .resetForRound(ENEMY_SPAWN, enemyDied);

        phase = Phase.INTRO;
        phaseTimer = INTRO_TIME;
        fightStarted = false;
        CoreResources.setRoundFrozen(true);
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
