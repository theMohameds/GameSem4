package io.group9.weapons;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.physics.box2d.Body;
import io.group9.CoreResources;

import java.util.ArrayList;
import java.util.List;

public class BodyDestroySystem extends EntitySystem {
    private static final List<Body> queue = new ArrayList<>();

    public static void schedule(Body b) {
        synchronized (queue) { queue.add(b); }
    }

    @Override
    public void update(float dt) {
        synchronized (queue) {
            for (Body b : queue) {
                CoreResources.getWorld().destroyBody(b);
            }
            queue.clear();
        }
    }
}
