package services.gameCamera;

import com.badlogic.gdx.graphics.OrthographicCamera;

public class NoopCameraService implements ICameraService {
    @Override public OrthographicCamera getCamera() {
        return null;
    }
    @Override public void setCamera(OrthographicCamera cam) {
        // no-op
    }
}

