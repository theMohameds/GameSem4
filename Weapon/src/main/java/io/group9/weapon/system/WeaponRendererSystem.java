package io.group9.weapon.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.group9.CoreResources;
import io.group9.common.WeaponType;
import io.group9.weapon.components.WeaponComponent;

public class WeaponRendererSystem extends IteratingSystem {
    private SpriteBatch batch;
    private Texture swordTexture;
    private Texture knifeTexture;

    public WeaponRendererSystem() {
        super(Family.all(WeaponComponent.class).get());
        batch = new SpriteBatch();
        // Load the textures for each weapon type
        swordTexture = new Texture(Gdx.files.internal("weapons/sword.png"));
        knifeTexture = new Texture(Gdx.files.internal("weapons/knife.png"));
    }

    @Override
    public void update(float deltaTime) {
        batch.setProjectionMatrix(CoreResources.getCamera().combined);
        batch.begin();
        super.update(deltaTime);
        batch.end();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        WeaponComponent wc = entity.getComponent(WeaponComponent.class);
        if (wc == null || !wc.isActive) return;

        // Compare the weapon's type using the enum from io.group9.common.WeaponType
        Texture texture = (wc.type == WeaponType.SWORD) ? swordTexture : knifeTexture;
        Vector2 pos = wc.body.getPosition();
        float width = 8f / CoreResources.PPM;
        float height = 8f / CoreResources.PPM;
        batch.draw(texture, pos.x - width / 2, pos.y - height / 2, width, height);
    }

    public void dispose() {
        batch.dispose();
        swordTexture.dispose();
        knifeTexture.dispose();
    }
}

