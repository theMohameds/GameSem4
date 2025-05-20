package io.group9.enemy.plugins;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import data.WorldProvider;
import data.util.CoreResources;
import io.group9.enemy.components.EnemyComponent;
import data.components.CollisionCategories;
import locators.EnemyServiceLocator;
import services.enemy.IEnemyService;

public final class EnemyFactory {
    private EnemyFactory() { }

    public static void spawn(Engine engine) {
        World world = WorldProvider.getWorld();


        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.fixedRotation = true;
        bd.position.set(500f / CoreResources.PPM, 150f / CoreResources.PPM);
        Body body = world.createBody(bd);
        body.setLinearDamping(0f);
        body.setSleepingAllowed(false);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8f / CoreResources.PPM, 15f / CoreResources.PPM);
        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density  = 0.5f;
        fd.friction = 0f;
        fd.filter.categoryBits = CollisionCategories.ENEMY;
        fd.filter.maskBits     = (short)(CollisionCategories.GROUND | CollisionCategories.WALL);
        fd.filter.groupIndex   = -1;
        body.createFixture(fd).setUserData("enemyBody");
        shape.dispose();

        PolygonShape footShape = new PolygonShape();
        footShape.setAsBox(6f / CoreResources.PPM, 2f / CoreResources.PPM, new Vector2(0f, -15f / CoreResources.PPM), 0f);
        FixtureDef footFD = new FixtureDef();
        footFD.shape       = footShape;
        footFD.isSensor    = true;
        footFD.filter.categoryBits = CollisionCategories.ENEMY;
        footFD.filter.maskBits     = (short)(CollisionCategories.GROUND | CollisionCategories.WALL);
        Fixture footFx = body.createFixture(footFD);
        footFx.setUserData("footSensor");
        footShape.dispose();

        PolygonShape hurtShape = new PolygonShape();
        hurtShape.setAsBox(8f / CoreResources.PPM, 15f / CoreResources.PPM);
        FixtureDef hurtFD = new FixtureDef();
        hurtFD.shape       = hurtShape;
        hurtFD.isSensor    = true;
        hurtFD.filter.categoryBits = CollisionCategories.ENEMY_HURTBOX;
        hurtFD.filter.maskBits     = CollisionCategories.ATTACK;
        Fixture hurtFx = body.createFixture(hurtFD);
        hurtFx.setUserData("enemyHurtbox");
        hurtShape.dispose();

        EnemyComponent ec = new EnemyComponent();
        ec.body          = body;
        ec.footSensor    = footFx;
        ec.hurtboxSensor = hurtFx;
        ec.facingLeft = true;;

        body.setUserData(ec);
        Entity e = new Entity();
        e.add(ec);
        engine.addEntity(e);

        IEnemyService enemySvc = EnemyServiceLocator.get();
        enemySvc.setEnemyBody(body);
        enemySvc.setEnemyEntity(e);
        enemySvc.setHealth(ec.health);
    }
}
