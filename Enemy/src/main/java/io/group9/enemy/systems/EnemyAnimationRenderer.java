package io.group9.enemy.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import io.group9.CoreResources;
import io.group9.enemy.ai.EnemyState;
import io.group9.enemy.components.EnemyComponent;

import java.util.EnumMap;

public class EnemyAnimationRenderer extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private SpriteBatch batch;
    private EnumMap<EnemyState, Animation<TextureRegion>> anims;

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
        batch = new SpriteBatch();
        anims = new EnumMap<>(EnemyState.class);

        anims.put(EnemyState.IDLE, load("character/Enemy_idle.png",     0.066f, 10, true));
        anims.put(EnemyState.RUN, load("character/Enemy_run.png",      0.066f,  8, true));
        anims.put(EnemyState.JUMP, load("character/Enemy_jump.png",     0.066f,  6, true));
        anims.put(EnemyState.AIRSPIN, load("character/Enemy_AirSpin.png",  0.066f,  6, true));
        anims.put(EnemyState.ATTACK, load("character/Enemy_punchJab.png", 0.066f, 10, true));
        anims.put(EnemyState.HURT, load("character/Enemy_hurt.png",     0.066f,  4, true));
        anims.put(EnemyState.DEAD, load("character/Enemy_dead.png",     0.100f, 10, false));
    }

    private Animation<TextureRegion> load(String path, float dur, int frames, boolean loop) {
        Texture tex = new Texture(Gdx.files.internal(path));
        int fw = tex.getWidth() / frames, fh = tex.getHeight();
        TextureRegion[][] split = TextureRegion.split(tex, fw, fh);
        TextureRegion[] arr = new TextureRegion[frames];
        for (int i = 0; i < frames; i++) arr[i] = split[0][i];
        Animation<TextureRegion> anim = new Animation<>(dur, arr);
        anim.setPlayMode(loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        return anim;
    }

    @Override
    public void update(float dt) {
        OrthographicCamera cam = CoreResources.getCamera();
        cam.update();

        batch.setProjectionMatrix(cam.combined);
        batch.begin();

        // tint everything you draw to blue
        batch.setColor(Color.LIGHT_GRAY);

        for (Entity e : entities) {
            EnemyComponent ec = e.getComponent(EnemyComponent.class);
            ec.animTime += dt;

            Animation<TextureRegion> anim = anims.getOrDefault(
                ec.state, anims.get(EnemyState.IDLE));

            TextureRegion frame = anim.getKeyFrame(
                ec.animTime, ec.state != EnemyState.DEAD);

            Vector2 p = ec.body.getPosition();
            float w = 48f / CoreResources.PPM, h = 48f / CoreResources.PPM;
            if (ec.facingLeft) {
                batch.draw(frame, p.x + w / 2, p.y - h / 2, -w, h);
            } else {
                batch.draw(frame, p.x - w / 2, p.y - h / 2,  w, h);
            }
        }

        // reset tint so nothing else gets colored
        batch.setColor(Color.WHITE);
        batch.end();
    }


    @Override public void removedFromEngine(Engine e) { batch.dispose(); }
}
