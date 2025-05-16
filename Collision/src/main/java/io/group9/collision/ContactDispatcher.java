package io.group9.collision;

import com.badlogic.gdx.physics.box2d.*;
import contact.ContactReceiver;
import contact.IContactDispatcherService;

import java.util.ArrayList;
import java.util.List;

public class ContactDispatcher implements IContactDispatcherService {
    private final List<ContactReceiver> receivers = new ArrayList<>();

    @Override
    public void addReceiver(ContactReceiver r) {
        receivers.add(r);
    }

    @Override
    public void removeReceiver(ContactReceiver r) {
        receivers.remove(r);
    }

    @Override
    public void beginContact(Contact contact) {
        receivers.forEach(r -> r.beginContact(contact));
    }

    @Override
    public void endContact(Contact contact) {
        receivers.forEach(r -> r.endContact(contact));
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) { }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) { }
}

