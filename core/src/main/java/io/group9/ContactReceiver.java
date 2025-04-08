package io.group9;

import com.badlogic.gdx.physics.box2d.Contact;

public interface ContactReceiver {
    void beginContact(Contact contact);
    void endContact(Contact contact);
}
