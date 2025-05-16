package locators;
import contact.IContactDispatcherService;

import java.util.ServiceLoader;

public final class ContactDispatcherLocator {
    private static IContactDispatcherService INSTANCE;

    private ContactDispatcherLocator() { }

    public static IContactDispatcherService get() {
        if (INSTANCE == null) {
            INSTANCE = ServiceLoader.load(IContactDispatcherService.class).findFirst().orElseThrow(() -> new IllegalStateException("No IContactDispatcher implementation found on the classpath"));
        }
        return INSTANCE;
    }
}
