package io.group9;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import java.util.ArrayList;
import java.util.List;


public class CoreContactDispatcher implements ContactListener {

    private final List<ContactReceiver> receivers = new ArrayList<>();

    public void addReceiver(ContactReceiver receiver) {
        receivers.add(receiver);
    }
    public void removeReceiver(ContactReceiver receiver) {
        receivers.remove(receiver);
    }

    @Override
    public void beginContact(Contact contact) {
        for (ContactReceiver r : receivers) {
            r.beginContact(contact);
        }
    }

    @Override
    public void endContact(Contact contact) {
        for (ContactReceiver r : receivers) {
            r.endContact(contact);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
