package io.group9.player.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import io.group9.CoreResources;
import io.group9.player.components.PlayerComponent;
import java.util.EnumMap;
import com.badlogic.gdx.graphics.Color;

public class PlayerAnimationRenderer extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private SpriteBatch batch;
    private float stateTime = 0f;
    private EnumMap<PlayerComponent.State, Animation<TextureRegion>> animations;
    Animation<TextureRegion> oldAnim;

    @Override
    public void addedToEngine(com.badlogic.ashley.core.Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
        batch = new SpriteBatch();
        animations = new EnumMap<>(PlayerComponent.State.class);

        animations.put(PlayerComponent.State.IDLE, loadAnimation("character/Enemy_idle.png", 0.066f, 10, true));
        animations.put(PlayerComponent.State.RUN, loadAnimation("character/Enemy_run.png", 0.066f, 8,true));
        animations.put(PlayerComponent.State.JUMP, loadAnimation("character/Enemy_jump.png", 0.066f, 6,true));
        animations.put(PlayerComponent.State.AIRSPIN, loadAnimation("character/Enemy_AirSpin.png", 0.066f, 6,true));
        animations.put(PlayerComponent.State.HEAVY_ATTACK, loadAnimation("character/Enemy_punchCross.png", 0.066f, 7,true));
        animations.put(PlayerComponent.State.LAND_WALL, loadAnimation("character/Enemy_landWall.png", 0.297f, 6,true));
        animations.put(PlayerComponent.State.LIGHT_ATTACK, loadAnimation("character/Enemy_punchJab.png", 0.066f, 10,true));
        animations.put(PlayerComponent.State.HURT, loadAnimation("character/Enemy_hurt.png", 0.066f, 4,true ));
        animations.put(PlayerComponent.State.DEAD, loadAnimation("character/Enemy_dead.png",0.100f,10,false));
        //animations.put(PlayerComponent.State.DASH, loadAnimation("character/Enemy_dash.png",0.100f,10,true));
    }

    private Animation<TextureRegion> loadAnimation(String path, float dur, int frames, boolean loop) {
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

        OrthographicCamera cam = CoreResources.getCamera();
        if (cam == null) {
            Gdx.app.error("PlayerAnimationRenderer", "Camera is null, using fallback.");
            cam = new OrthographicCamera(40, 22.5f);
            cam.position.set(20, 11.25f, 0);
            cam.update();
        } else {
            cam.update();
        }
        batch.setProjectionMatrix(cam.combined);


        batch.begin();
        batch.setColor(Color.LIME);
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            PlayerComponent pc = entity.getComponent(PlayerComponent.class);
            if (pc.body == null) continue;
            Vector2 pos = pc.body.getPosition();

            Animation<TextureRegion> currentAnim = animations.get(pc.state);
            if (currentAnim == null) {
                currentAnim = animations.get(PlayerComponent.State.IDLE);
            }

            if (currentAnim != oldAnim){
                stateTime = 0;
                oldAnim = currentAnim;
            }

            stateTime += deltaTime;

            TextureRegion frame;



            if (currentAnim == animations.get(PlayerComponent.State.JUMP)){
                if (pc.body.getLinearVelocity().y > 0){
                    if (stateTime > currentAnim.getFrameDuration() * 1){
                        stateTime = currentAnim.getFrameDuration() * 1;
                    }
                    frame = currentAnim.getKeyFrame(stateTime, false);
                }else {
                    float frameDuration = currentAnim.getFrameDuration();
                    float startTime = frameDuration * 2; // frame 2 starts
                    float endTime = frameDuration * 6;   // frame 5 starts

                    if (stateTime < startTime) {
                        stateTime = startTime;
                    }
                    if (stateTime > endTime) {
                        stateTime = endTime - 0.0001f; // Stay just before frame 6 starts
                    }

                    frame = currentAnim.getKeyFrame(stateTime, false); // Don't loop
                }
            } else if (currentAnim == animations.get(PlayerComponent.State.AIRSPIN)) {
                float airspinDuration = currentAnim.getAnimationDuration();

                if (stateTime < airspinDuration) {
                    // Still playing airspin normally
                    float playTime = Math.min(stateTime, airspinDuration - 0.0001f);
                    frame = currentAnim.getKeyFrame(playTime, false);

                } else {
                    Animation<TextureRegion> jumpAnim = animations.get(PlayerComponent.State.JUMP);
                    float playTime = 0;
                    float clipStartTime = 0;
                    if (pc.body.getLinearVelocity().y < 0){
                        // AIRSPIN finished: play only the last 4 frames of JUMP
                        int totalJumpFrames = jumpAnim.getKeyFrames().length;
                        int clipFrameCount = 4;

                        // Calculate start frame index (e.g., if totalJumpFrames=6, startFrame=2)
                        int startFrameIndex = Math.max(0, totalJumpFrames - clipFrameCount);

                        // Convert frame index to time
                        float frameDuration = jumpAnim.getFrameDuration();
                        clipStartTime = startFrameIndex * frameDuration;
                        float clipDuration = clipFrameCount * frameDuration;

                        // Calculate how long we've been into the jump clip
                        float elapsedSinceAirspin = stateTime - airspinDuration;
                        playTime = Math.min(elapsedSinceAirspin, clipDuration - 0.0001f);

                        // Get the appropriate frame from the jump animation

                    }

                    frame = jumpAnim.getKeyFrame(clipStartTime + playTime, false);

                }
            }else {
                frame = currentAnim.getKeyFrame(stateTime);
            }

            float targetWidth = 48f / CoreResources.PPM;
            float targetHeight = 48f / CoreResources.PPM;

            if (pc.facingLeft) {
                batch.draw(frame, pos.x + targetWidth / 2, pos.y - targetHeight / 2, -targetWidth, targetHeight);
            } else {
                batch.draw(frame, pos.x - targetWidth / 2, pos.y - targetHeight / 2, targetWidth, targetHeight);
            }
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public void removedFromEngine(Engine engine) {
        batch.dispose();
    }
}


