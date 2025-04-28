package services;

import com.badlogic.ashley.core.Entity;

public interface IPickable {
    void onPickUp(Entity picker);
}
