package io.group9.enemy.ai;

import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;
import io.group9.CoreResources;

public class PlayerLocation implements Location<Vector2> {
    private final float offsetX;

    public PlayerLocation(float offsetX) {
        this.offsetX = offsetX;
    }

    @Override
    public Vector2 getPosition() {
        Vector2 p = CoreResources.getPlayerBody().getPosition();
        return p;
    }

    @Override public float getOrientation() { return 0; }
    @Override public void setOrientation(float orientation) { }
    @Override public Location<Vector2> newLocation() { return new PlayerLocation(offsetX); }
    @Override public float vectorToAngle(Vector2 v) { return (float)Math.atan2(v.y, v.x); }
    @Override public Vector2 angleToVector(Vector2 out, float angle) {
        out.x = (float)Math.cos(angle);
        out.y = (float)Math.sin(angle);
        return out;
    }
}
