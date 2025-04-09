package io.group9.enemy.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;
import io.group9.CoreResources;
import io.group9.enemy.components.EnemyComponent;

import java.util.EnumMap;

public class EnemyAnimationRenderer extends EntitySystem {
    private SpriteBatch batch;
    private EnumMap<EnemyComponent.State, Animation<TextureRegion>> animations;
    private ImmutableArray<Entity> enemies;
    private float stateTime = 0f;

    @Override
    public void addedToEngine(Engine engine) {
        enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
        batch = new SpriteBatch();
        animations = new EnumMap<>(EnemyComponent.State.class);

        animations.put(EnemyComponent.State.IDLE, loadAnimation("enemy/Enemy_idle.png", 0.066f, 10));
        animations.put(EnemyComponent.State.HURT, loadAnimation("enemy/Enemy_hurt.png", 0.066f, 4));
    }

    private Animation<TextureRegion> loadAnimation(String path, float duration, int cols) {
        Texture texture = new Texture(Gdx.files.internal(path));
        int frameWidth = texture.getWidth() / cols;
        int frameHeight = texture.getHeight();
        TextureRegion[][] tmp = TextureRegion.split(texture, frameWidth, frameHeight);
        TextureRegion[] frames = new TextureRegion[cols];
        for (int i = 0; i < cols; i++) {
            frames[i] = tmp[0][i];
        }
        return new Animation<>(duration, frames);
    }

    @Override
    public void update(float deltaTime) {
        stateTime += deltaTime;
        OrthographicCamera cam = CoreResources.getCamera();
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        batch.begin();

        for (Entity e : enemies) {
            EnemyComponent ec = e.getComponent(EnemyComponent.class);
            Vector2 pos = ec.body.getPosition();

            Animation<TextureRegion> anim = animations.getOrDefault(ec.state, animations.get(EnemyComponent.State.IDLE));
            TextureRegion frame = anim.getKeyFrame(stateTime, true);
            float width = 48f / CoreResources.PPM;
            float height = 48f / CoreResources.PPM;

            if (ec.facingLeft) {
                batch.draw(frame, pos.x + width / 2, pos.y - height / 2, -width, height);
            } else {
                batch.draw(frame, pos.x - width / 2, pos.y - height / 2, width, height);
            }
        }

        batch.end();
    }
}
