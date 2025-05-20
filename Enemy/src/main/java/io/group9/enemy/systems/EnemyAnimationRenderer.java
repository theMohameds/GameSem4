package io.group9.enemy.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import data.util.CoreResources;
import io.group9.enemy.ai.EnemyState;
import io.group9.enemy.components.EnemyComponent;
import locators.CameraServiceLocator;

import java.util.EnumMap;

public class EnemyAnimationRenderer extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private SpriteBatch batch;
    private float stateTime = 0f;
    private EnumMap<EnemyState, Animation<TextureRegion>> animations;
    Animation<TextureRegion> oldAnim;

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
        batch = new SpriteBatch();
        animations = new EnumMap<>(EnemyState.class);

        animations.put(EnemyState.IDLE, load("character/Enemy_idle.png",     0.066f, 10, true));
        animations.put(EnemyState.RUN, load("character/Enemy_run.png",      0.066f,  8, true));
        animations.put(EnemyState.JUMP, load("character/Enemy_jump.png",     0.066f,  6, true));
        animations.put(EnemyState.AIRSPIN, load("character/Enemy_AirSpin.png",  0.066f,  6, true));
        animations.put(EnemyState.ATTACK, load("character/Enemy_punchJab.png", 0.066f, 10, true));
        animations.put(EnemyState.HURT, load("character/Enemy_hurt.png",     0.066f,  4, true));
        animations.put(EnemyState.DEAD, load("character/Enemy_dead.png",     0.100f, 10, false));
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
    public void update(float deltaTime) {

        OrthographicCamera cam = CameraServiceLocator.get().getCamera();
        if (cam == null) {
            return;
        } else {
            cam.update();
        }
        batch.setProjectionMatrix(cam.combined);


        batch.begin();
        batch.setColor(Color.ROYAL);
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            EnemyComponent ec = entity.getComponent(EnemyComponent.class);
            if (ec.body == null) continue;
            Vector2 pos = ec.body.getPosition();

            Animation<TextureRegion> currentAnim = animations.get(ec.state);
            if (currentAnim == null) {
                currentAnim = animations.get(EnemyState.IDLE);
            }

            if (currentAnim != oldAnim){
                stateTime = 0;
                oldAnim = currentAnim;
            }

            stateTime += deltaTime;

            TextureRegion frame;



            if (currentAnim == animations.get(EnemyState.JUMP)){
                if (ec.body.getLinearVelocity().y > 0){
                    if (stateTime > currentAnim.getFrameDuration() * 1){
                        stateTime = currentAnim.getFrameDuration() * 1;
                    }
                    frame = currentAnim.getKeyFrame(stateTime, false);
                }else {
                    float frameDuration = currentAnim.getFrameDuration();
                    float startTime = frameDuration * 2;
                    float endTime = frameDuration * 6;

                    if (stateTime < startTime) {
                        stateTime = startTime;
                    }
                    if (stateTime > endTime) {
                        stateTime = endTime - 0.0001f;
                    }

                    frame = currentAnim.getKeyFrame(stateTime, false);
                }
            } else if (currentAnim == animations.get(EnemyState.AIRSPIN)) {
                float airspinDuration = currentAnim.getAnimationDuration();

                if (stateTime < airspinDuration) {
                    float playTime = Math.min(stateTime, airspinDuration - 0.0001f);
                    frame = currentAnim.getKeyFrame(playTime, false);

                } else {
                    Animation<TextureRegion> jumpAnim = animations.get(EnemyState.JUMP);
                    float playTime = 0;
                    float clipStartTime = 0;
                    if (ec.body.getLinearVelocity().y < 0){
                        int totalJumpFrames = jumpAnim.getKeyFrames().length;
                        int clipFrameCount = 4;

                        int startFrameIndex = Math.max(0, totalJumpFrames - clipFrameCount);

                        float frameDuration = jumpAnim.getFrameDuration();
                        clipStartTime = startFrameIndex * frameDuration;
                        float clipDuration = clipFrameCount * frameDuration;

                        float elapsedSinceAirspin = stateTime - airspinDuration;
                        playTime = Math.min(elapsedSinceAirspin, clipDuration - 0.0001f);


                    }

                    frame = jumpAnim.getKeyFrame(clipStartTime + playTime, false);

                }
            }else {
                frame = currentAnim.getKeyFrame(stateTime);
            }

            float targetWidth = 48f / CoreResources.PPM;
            float targetHeight = 48f / CoreResources.PPM;

            if (ec.facingLeft) {
                batch.draw(frame, pos.x + targetWidth / 2, pos.y - targetHeight / 2, -targetWidth, targetHeight);
            } else {
                batch.draw(frame, pos.x - targetWidth / 2, pos.y - targetHeight / 2, targetWidth, targetHeight);
            }
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }


    @Override public void removedFromEngine(Engine e) { batch.dispose(); }
}
