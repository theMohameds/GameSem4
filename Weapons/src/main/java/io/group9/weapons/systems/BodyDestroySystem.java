package io.group9.weapons.systems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import data.WorldProvider;

import java.util.ArrayList;
import java.util.List;

public class BodyDestroySystem extends EntitySystem {
    private static final List<Body> queue = new ArrayList<>();
    World world = WorldProvider.getWorld();


    public static void schedule(Body b) {
        synchronized (queue) { queue.add(b); }
    }

    @Override
    public void update(float dt) {
        synchronized (queue) {
            for (Body b : queue) {
                world.destroyBody(b);
            }
            queue.clear();
        }
    }
}
