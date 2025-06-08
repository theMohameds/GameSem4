package locators;

import contact.IContactDispatcherService;
import contact.NoopDispatcherService;

import java.util.ServiceLoader;

public final class ContactDispatcherLocator {
    private static IContactDispatcherService INSTANCE;

    private ContactDispatcherLocator() { }

    public static IContactDispatcherService get() {
        if (INSTANCE == null) {
            INSTANCE = ServiceLoader.load(IContactDispatcherService.class)
                .findFirst()
                .orElse(new NoopDispatcherService());
        }
        return INSTANCE;
    }
}
