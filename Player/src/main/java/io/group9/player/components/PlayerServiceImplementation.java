package io.group9.player.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Body;
import services.player.IPlayerService;

public class PlayerServiceImplementation implements IPlayerService {
    private Entity playerEntity;
    private Body playerBody;
    private int health, maxHealth;

    @Override public int getHealth()           { return health; }
    @Override public void setHealth(int h)     { health = h;       }
    @Override public int getMaxHealth()        { return maxHealth; }
    @Override public void setMaxHealth(int mh) { maxHealth = mh;   }
    @Override public Entity getPlayerEntity()  { return playerEntity;   }
    @Override public void setPlayerEntity(Entity e) { playerEntity = e; }
    @Override public Body getPlayerBody()      { return playerBody;     }
    @Override public void setPlayerBody(Body b){ playerBody = b;   }
}

