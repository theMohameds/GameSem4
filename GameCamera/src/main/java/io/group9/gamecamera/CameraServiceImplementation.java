package io.group9.gamecamera;

import com.badlogic.gdx.graphics.OrthographicCamera;
import services.gameCamera.ICameraService;

public class CameraServiceImplementation implements ICameraService {
    private OrthographicCamera camera;
    @Override public OrthographicCamera getCamera() {
        return camera;
    }
    @Override public void setCamera(OrthographicCamera cam) {
        this.camera = cam;
    }
}
