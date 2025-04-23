package io.group9.enemy.ai;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;
import io.group9.enemy.components.EnemyComponent;

public class EnemyLocation implements Steerable<Vector2>, Location<Vector2> {
    private final EnemyComponent ec;
    private float zeroThreshold = 0.01f;

    public EnemyLocation(EnemyComponent ec) {
        this.ec = ec;
    }

    // Location<Vector2>
    @Override public Vector2 getPosition() { return ec.body.getPosition(); }
    @Override public float getOrientation() { return ec.body.getAngle(); }
    @Override public void setOrientation(float o) { ec.body.setTransform(getPosition(), o); }
    @Override public Location<Vector2> newLocation() { return new EnemyLocation(ec); }
    public Vector2 newVector() { return new Vector2(); }
    @Override public float vectorToAngle(Vector2 v) { return (float)Math.atan2(v.y, v.x); }
    @Override public Vector2 angleToVector(Vector2 o, float a) {
        o.x = (float)Math.cos(a); o.y = (float)Math.sin(a); return o;
    }

    // Steerable<Vector2>
    @Override public Vector2 getLinearVelocity() { return ec.body.getLinearVelocity(); }
    @Override public float getAngularVelocity() { return 0f; }
    @Override public float getMaxLinearSpeed() { return ec.maxLinearSpeed; }
    @Override public void setMaxLinearSpeed(float v) { ec.maxLinearSpeed = v; }
    @Override public float getMaxLinearAcceleration() { return ec.maxLinearAcceleration; }
    @Override public void setMaxLinearAcceleration(float a){ ec.maxLinearAcceleration = a; }
    @Override public float getMaxAngularSpeed() { return ec.maxAngularSpeed; }
    @Override public void setMaxAngularSpeed(float s) { ec.maxAngularSpeed = s; }
    @Override public float getMaxAngularAcceleration() { return ec.maxAngularAcceleration; }
    @Override public void setMaxAngularAcceleration(float a){ ec.maxAngularAcceleration = a; }
    @Override public float getBoundingRadius() { return ec.boundingRadius; }
    @Override public boolean isTagged() { return false; }
    @Override public void setTagged(boolean t) { }
    @Override public float getZeroLinearSpeedThreshold() { return zeroThreshold; }
    @Override public void setZeroLinearSpeedThreshold(float t) { zeroThreshold = t; }
}

