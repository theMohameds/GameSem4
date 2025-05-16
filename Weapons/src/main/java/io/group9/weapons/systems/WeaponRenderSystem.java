package io.group9.weapons.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import data.WorldProvider;
import util.CoreResources;
import locators.CameraServiceLocator;
import services.weapon.IWeapon;

public class WeaponRenderSystem extends EntitySystem {
    private static final float SCALE = 0.6f;
    private SpriteBatch batch = new SpriteBatch();

    @Override
    public void update(float dt) {
        OrthographicCamera cam = CameraServiceLocator.get().getCamera();
        if (cam == null) {
            return;
        } else {
            cam.update();
        }
        batch.setProjectionMatrix(cam.combined);

        World world = WorldProvider.getWorld();
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);

        batch.begin();
        for (Body body : bodies) {
            for (Fixture f : body.getFixtureList()) {
                Object ud = f.getUserData();
                if (ud instanceof IWeapon) {
                    IWeapon wep = (IWeapon) ud;
                    TextureRegion spr = wep.getSprite();
                    Vector2 pos = body.getPosition();

                    float origW = spr.getRegionWidth()  / CoreResources.PPM;
                    float origH = spr.getRegionHeight() / CoreResources.PPM;

                    float w = origW * SCALE;
                    float h = origH * SCALE;

                    batch.draw(spr, pos.x - w/2f, pos.y - h/2f, w, h);
                    break;
                }
            }
        }
        batch.end();
    }

    @Override
    public void removedFromEngine(Engine engine) {
        batch.dispose();
    }
}
