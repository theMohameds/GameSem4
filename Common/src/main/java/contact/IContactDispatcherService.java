package contact;

import com.badlogic.gdx.physics.box2d.ContactListener;

public interface IContactDispatcherService extends ContactListener {
    void addReceiver(ContactReceiver receiver);
    void removeReceiver(ContactReceiver receiver);
}
