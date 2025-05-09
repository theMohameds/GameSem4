package locators;

import services.gameCamera.ICameraService;
import java.util.ServiceLoader;

public final class CameraServiceLocator {
    private static ICameraService instance;

    private CameraServiceLocator() {}

    public static ICameraService get() {
        if (instance == null) {
            ServiceLoader<ICameraService> loader =
                ServiceLoader.load(ICameraService.class);
            for (ICameraService svc : loader) {
                System.out.println("SPI FOUND: " + svc.getClass().getName());
            }
            instance = loader.findFirst().orElseThrow(() -> new IllegalStateException("No ICameraService found"));
        }
        return instance;
    }

}
