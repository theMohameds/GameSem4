package io.group9.enemy;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.group9.CoreResources;
import io.group9.enemy.components.EnemyComponent;
import components.CollisionCategories;

public final class EnemyFactory {
    private EnemyFactory() { }

    public static void spawn(Engine engine) {
        World world = CoreResources.getWorld();

        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.fixedRotation = true;
        bd.position.set(200f / CoreResources.PPM, 150f / CoreResources.PPM);
        Body body = world.createBody(bd);
        body.setLinearDamping(0f);
        body.setSleepingAllowed(false);

        // Main fixture: ground & walls only, same negative group as player
        PolygonShape mainShape = new PolygonShape();
        mainShape.setAsBox(8f / CoreResources.PPM, 15f / CoreResources.PPM);
        FixtureDef mainFD = new FixtureDef();
        mainFD.shape = mainShape;
        mainFD.density = 0.5f;
        mainFD.friction = 0f;
        mainFD.filter.categoryBits = CollisionCategories.ENEMY;
        mainFD.filter.maskBits     = (short)(
            CollisionCategories.GROUND |
                CollisionCategories.WALL
        );
        mainFD.filter.groupIndex   = -1;  // never collide with same group
        body.createFixture(mainFD).setUserData("enemyBody");
        mainShape.dispose();

        // Foot sensor (ground detection)
        PolygonShape footShape = new PolygonShape();
        footShape.setAsBox(
            6f / CoreResources.PPM,
            2f / CoreResources.PPM,
            new Vector2(0f, -15f / CoreResources.PPM),
            0f
        );
        FixtureDef footFD = new FixtureDef();
        footFD.shape = footShape;
        footFD.isSensor = true;
        footFD.filter.categoryBits = CollisionCategories.ENEMY;
        footFD.filter.maskBits     = (short)(
            CollisionCategories.GROUND |
                CollisionCategories.WALL
        );
        Fixture footFx = body.createFixture(footFD);
        footFx.setUserData("footSensor");
        footShape.dispose();

        // Hurtbox sensor (hit by player attacks)
        PolygonShape hurtShape = new PolygonShape();
        hurtShape.setAsBox(8f / CoreResources.PPM, 15f / CoreResources.PPM);
        FixtureDef hurtFD = new FixtureDef();
        hurtFD.shape = hurtShape;
        hurtFD.isSensor = true;
        hurtFD.filter.categoryBits = CollisionCategories.ENEMY_HURTBOX;
        hurtFD.filter.maskBits     = CollisionCategories.ATTACK;
        Fixture hurtFx = body.createFixture(hurtFD);
        hurtFx.setUserData("enemyHurtbox");
        hurtShape.dispose();

        EnemyComponent ec = new EnemyComponent();
        ec.body          = body;
        ec.footSensor    = footFx;
        ec.hurtboxSensor = hurtFx;
        body.setUserData(ec);

        Entity e = new Entity();
        e.add(ec);
        engine.addEntity(e);
    }
}
