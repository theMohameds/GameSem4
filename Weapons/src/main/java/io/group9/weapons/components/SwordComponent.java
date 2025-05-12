package io.group9.weapons.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import services.weapon.IWeapon;
import services.weapon.IPickable;

public class SwordComponent implements Component, IWeapon, IPickable {
    private Entity owner;

    private static final TextureRegion SPRITE = new TextureRegion(new Texture(Gdx.files.internal("weapons/sword_item_32.png")));

    @Override
    public String getName() {
        return "Sword";
    }

    @Override
    public void use(Entity user) {
    }

    @Override
    public void onPickUp(Entity picker) {
        this.owner = picker;
    }

    @Override
    public void onDrop(Entity picker) {
        if (picker == owner) {
            this.owner = null;
        }
    }

    @Override
    public TextureRegion getSprite() {
        return SPRITE;
    }
}

