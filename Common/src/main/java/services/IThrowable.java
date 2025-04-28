package services;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

public interface IThrowable {
    void throwItem(Entity thrower, Vector2 direction);
}
