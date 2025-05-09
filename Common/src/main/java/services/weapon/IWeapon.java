package services.weapon;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface IWeapon {
    String getName();
    void use(Entity user);
    void onDrop(Entity owner);

    TextureRegion getSprite();
}
