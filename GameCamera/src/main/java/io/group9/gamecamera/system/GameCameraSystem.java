package io.group9.gamecamera.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import io.group9.CoreResources;

public class GameCameraSystem extends EntitySystem {
    private final OrthographicCamera camera;
    private final boolean followPlayer;

    public GameCameraSystem(float viewportWidth, float viewportHeight, float initialX, float initialY, boolean followPlayer) {
        camera = new OrthographicCamera(viewportWidth, viewportHeight);
        camera.position.set(initialX, initialY, 0);
        camera.update();
        this.followPlayer = followPlayer;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public void setCameraPos(float x, float y) {
        camera.position.set(x, y, 0);
        camera.update();
    }

    @Override
    public void update(float deltaTime) {
        if (followPlayer && CoreResources.getPlayerBody() != null) {
            Vector2 playerPos = CoreResources.getPlayerBody().getPosition();
            // preserve current Y
            //float y = camera.position.y;
            camera.position.set(playerPos.x, playerPos.y, 0);
        }
        camera.update();
    }
}
