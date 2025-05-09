package io.group9.player.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import io.group9.CoreResources;
import io.group9.player.components.PlayerComponent;
import locators.InventoryServiceLocator;
import services.IInventoryService;
import services.IWeapon;

import java.util.EnumMap;
import java.util.Optional;
import java.util.ServiceLoader;

public class PlayerAnimationRenderer extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private SpriteBatch batch;
    private float stateTime = 0f;
    private EnumMap<PlayerComponent.State, Animation<TextureRegion>> anims;
    private Animation<TextureRegion> oldAnim;

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
        batch    = new SpriteBatch();
        anims    = new EnumMap<>(PlayerComponent.State.class);

        // Base animations
        anims.put(PlayerComponent.State.IDLE, load("character/Enemy_idle.png",    0.066f, 10, true));
        anims.put(PlayerComponent.State.RUN, load("character/Enemy_run.png",     0.066f,  8, true));
        anims.put(PlayerComponent.State.JUMP, load("character/Enemy_jump.png",    0.066f,  6, true));
        anims.put(PlayerComponent.State.AIRSPIN, load("character/Enemy_AirSpin.png", 0.066f,  6, true));
        anims.put(PlayerComponent.State.LIGHT_ATTACK, load("character/Enemy_punchJab.png",   0.066f, 10, false));
        anims.put(PlayerComponent.State.HEAVY_ATTACK, load("character/Enemy_punchCross.png", 0.066f,  7, false));
        anims.put(PlayerComponent.State.LAND_WALL, load("character/Enemy_landWall.png",   0.297f,  6, false));
        anims.put(PlayerComponent.State.HURT, load("character/Enemy_hurt.png",       0.066f,  4, false));
        anims.put(PlayerComponent.State.DEAD, load("character/Enemy_dead.png",       0.100f, 10, false));

        // Sword animations
        anims.put(PlayerComponent.State.SWORD_IDLE, load("character/sword/idle.png",   0.066f, 10, true));
        anims.put(PlayerComponent.State.SWORD_RUN, load("character/sword/run.png",    0.066f,  8, true));
        anims.put(PlayerComponent.State.SWORD_ATTACK, load("character/sword/attack.png", 0.066f,  6, false));
        anims.put(PlayerComponent.State.BLOCK,load("character/push_pull.png", 0.066f, 8, true));

    }

    private Animation<TextureRegion> load(
        String path, float frameDuration,
        int frames, boolean loop
    ) {
        Texture tex = new Texture(Gdx.files.internal(path));
        int fw = tex.getWidth()  / frames;
        int fh = tex.getHeight();
        TextureRegion[][] split = TextureRegion.split(tex, fw, fh);
        TextureRegion[] arr = new TextureRegion[frames];
        for (int i = 0; i < frames; i++) arr[i] = split[0][i];
        Animation<TextureRegion> a = new Animation<>(frameDuration, arr);
        a.setPlayMode(loop
            ? Animation.PlayMode.LOOP
            : Animation.PlayMode.NORMAL);
        return a;
    }

    @Override
    public void update(float deltaTime) {
        IInventoryService inv = InventoryServiceLocator.getInventoryService();

        OrthographicCamera cam = CoreResources.getCamera();
        if (cam == null) {
            cam = new OrthographicCamera(40, 22.5f);
            cam.position.set(20, 11.25f, 0);
        }
        cam.update();
        batch.setProjectionMatrix(cam.combined);

        batch.begin();

        for (Entity e : entities) {
            PlayerComponent pc = e.getComponent(PlayerComponent.class);
            if (pc.body == null) continue;
            batch.setColor(pc.color !=null ? pc.color : Color.WHITE);
            Vector2 pos = pc.body.getPosition();

            PlayerComponent.State state = pc.state;

            if (inv != null && inv.getCurrentWeapon(e)
                .map(w -> "Sword".equals(w.getName()))
                .orElse(false)) {
                switch (state) {
                    case IDLE:         state = PlayerComponent.State.SWORD_IDLE;  break;
                    case RUN:          state = PlayerComponent.State.SWORD_RUN;   break;
                    case LIGHT_ATTACK:
                    case HEAVY_ATTACK:
                    case SWORD_ATTACK: state = PlayerComponent.State.SWORD_ATTACK;break;
                    default:
                }
            }

            Animation<TextureRegion> anim =
                anims.getOrDefault(state, anims.get(PlayerComponent.State.IDLE));
            if (anim != oldAnim) {
                stateTime = 0f;
                oldAnim   = anim;
            }
            stateTime += deltaTime;

            TextureRegion frame = anim.getKeyFrame(stateTime);

            if (anim == anims.get(PlayerComponent.State.JUMP)) {
                boolean up = pc.body.getLinearVelocity().y > 0;
                float maxUp = anim.getFrameDuration() * 1;
                if (up) {
                    if (stateTime > maxUp) stateTime = maxUp;
                    frame = anim.getKeyFrame(stateTime, false);
                } else {
                    float fd = anim.getFrameDuration();
                    float start = fd * 2, end = fd * 6;
                    if (stateTime < start) stateTime = start;
                    if (stateTime > end)   stateTime = end - 0.0001f;
                    frame = anim.getKeyFrame(stateTime, false);
                }
            } else if (anim == anims.get(PlayerComponent.State.AIRSPIN)) {
                float dur = anim.getAnimationDuration();
                if (stateTime < dur) {
                    frame = anim.getKeyFrame(stateTime, false);
                } else {
                    Animation<TextureRegion> j = anims.get(PlayerComponent.State.JUMP);
                    float fd = j.getFrameDuration();
                    int total = j.getKeyFrames().length;
                    int clipF = 4;
                    int start = Math.max(0, total - clipF);
                    float clipStart = start * fd;
                    float el = stateTime - dur;
                    float pt = Math.min(el, clipF * fd - 0.0001f);
                    frame = j.getKeyFrame(clipStart + pt, false);
                }
            }

            float w2 = 48f / CoreResources.PPM;
            float h2 = 48f / CoreResources.PPM;
            if (pc.facingLeft) {
                batch.draw(frame,
                    pos.x + w2/2, pos.y - h2/2,
                    -w2, h2);
            } else {
                batch.draw(frame,
                    pos.x - w2/2, pos.y - h2/2,
                    w2, h2);
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
