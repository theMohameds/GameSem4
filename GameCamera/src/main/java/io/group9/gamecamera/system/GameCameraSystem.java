package io.group9.gamecamera.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.group9.CoreResources;
import plugins.GameMapProvider;

public class GameCameraSystem extends EntitySystem {
    private final OrthographicCamera camera;
    private final boolean followPlayer;

    public GameCameraSystem(float viewportWidth, float viewportHeight, float initialX, float initialY, boolean followPlayer) {
        camera = new OrthographicCamera(viewportWidth, viewportHeight);
        camera.position.set(initialX, initialY, 0);
        camera.update();
        this.followPlayer = followPlayer;
    }

    @Override
    public void update(float deltaTime) {
        if (followPlayer && CoreResources.getPlayerBody() != null) {
            Vector2 playerPos = CoreResources.getPlayerBody().getPosition();
            camera.position.set(playerPos.x, playerPos.y, 0);
        }

        GameMapProvider map = CoreResources.getGameMapProvider();
        if (map != null) {
            float mapWidth  = map.getLayerWidth()  * map.getCellSizeMeters();
            float mapHeight = map.getLayerHeight() * map.getCellSizeMeters();

            float halfW = camera.viewportWidth  * 0.5f;
            float halfH = camera.viewportHeight * 0.5f;

            camera.position.x = MathUtils.clamp(
                camera.position.x,
                halfW,
                mapWidth  - halfW
            );
            camera.position.y = MathUtils.clamp(
                camera.position.y,
                halfH,
                mapHeight - halfH
            );
        }

        camera.update();
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public void setCameraPos(float x, float y) {
        camera.position.set(x, y, 0);
        camera.update();
    }
}

