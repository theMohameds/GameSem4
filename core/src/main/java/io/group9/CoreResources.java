package io.group9;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import java.util.List;

public class CoreResources {
    public static World getWorld() {
        return world;
    }

    public static void setWorld(World world) {
        CoreResources.world = world;
    }

    public static World world;

    public static Engine getEngine() {
        return engine;
    }

    public static void setEngine(Engine engine) {
        CoreResources.engine = engine;
    }

    public static Engine engine;

    public static OrthographicCamera getCamera() {
        return camera;
    }

    public static void setCamera(OrthographicCamera camera) {
        CoreResources.camera = camera;
    }

    public static OrthographicCamera camera = new OrthographicCamera();

}

