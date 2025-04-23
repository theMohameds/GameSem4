package io.group9.player.system;

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

public class PlayerAnimationRenderer extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private SpriteBatch batch;
    public float stateTime = 0f;
    private EnumMap<PlayerComponent.State, Animation<TextureRegion>> animations;
    Animation<TextureRegion> oldAnim;

    @Override
    public void addedToEngine(com.badlogic.ashley.core.Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
        batch = new SpriteBatch();
        animations = new EnumMap<>(PlayerComponent.State.class);

        // Load animations from assets.

        animations.put(PlayerComponent.State.IDLE, loadAnimation("player/Player_idle.png", 0.066f, 10));
        animations.put(PlayerComponent.State.RUN, loadAnimation("player/Player_run.png", 0.066f, 8));
        animations.put(PlayerComponent.State.JUMP, loadAnimation("player/Player_jump.png", 0.066f, 6));
        animations.put(PlayerComponent.State.AIRSPIN, loadAnimation("player/Player_airSpin.png", 0.066f, 6));
        animations.put(PlayerComponent.State.HEAVY_ATTACK, loadAnimation("player/Punch_cross.png", 0.066f, 7));
        animations.put(PlayerComponent.State.LAND_WALL, loadAnimation("player/Player_landWall.png", 0.297f, 6));
        animations.put(PlayerComponent.State.LIGHT_ATTACK, loadAnimation("player/Punch_jab.png", 0.033f, 10));
        animations.put(PlayerComponent.State.DASH, loadAnimation("player/Player_dash.png", 0.066f, 9));

    }

    private Animation<TextureRegion> loadAnimation(String filePath, float frameDuration, int numFrames) {
        Texture texture = new Texture(Gdx.files.internal(filePath));
        int frameWidth = texture.getWidth() / numFrames;
        int frameHeight = texture.getHeight(); // one row.
        TextureRegion[][] tmp = TextureRegion.split(texture, frameWidth, frameHeight);
        TextureRegion[] frames = new TextureRegion[numFrames];
        for (int i = 0; i < numFrames; i++) {
            frames[i] = tmp[0][i];
        }
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(Animation.PlayMode.LOOP);
        return anim;
    }

    @Override
    public void update(float deltaTime) {

        // Mom get the camera.
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
                float airspinEndTime = airspinDuration - 0.0001f;
                stateTime = Math.min(stateTime, airspinEndTime);
                frame = currentAnim.getKeyFrame(stateTime, false);

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
        batch.end();
    }

    @Override
    public void removedFromEngine(com.badlogic.ashley.core.Engine engine) {
        batch.dispose();
    }
}


