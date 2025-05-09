package services.weapon;

import com.badlogic.ashley.core.Entity;

public interface IPickable {
    void onPickUp(Entity picker);
}
