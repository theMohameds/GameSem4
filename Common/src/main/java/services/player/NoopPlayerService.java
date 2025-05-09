package services.player;


import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Body;

public class NoopPlayerService implements IPlayerService {
    @Override public int getHealth() { return 0; }
    @Override public void setHealth(int health) {}
    @Override public int getMaxHealth() { return 0; }
    @Override public void setMaxHealth(int max) {}
    @Override public Entity getPlayerEntity() { return null; }
    @Override public void setPlayerEntity(Entity e) {}
    @Override public Body getPlayerBody() { return null; }
    @Override public void setPlayerBody(Body b) {}
}
