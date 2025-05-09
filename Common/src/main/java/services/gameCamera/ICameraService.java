package services.gameCamera;

import com.badlogic.gdx.graphics.OrthographicCamera;

public interface ICameraService {
    OrthographicCamera getCamera();
    void setCamera(OrthographicCamera cam);
}
