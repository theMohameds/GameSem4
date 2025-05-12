package locators;

import services.gameCamera.ICameraService;
import services.gameCamera.NoopCameraService;

import java.util.ServiceLoader;

public final class CameraServiceLocator {
    private static ICameraService instance;

    private CameraServiceLocator() {}

    public static ICameraService get() {
        if (instance == null) {
            instance = ServiceLoader.load(ICameraService.class).findFirst().orElse(new NoopCameraService());
        }
        return instance;
    }
}
