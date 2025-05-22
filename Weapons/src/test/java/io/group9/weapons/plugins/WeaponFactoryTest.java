// src/test/java/io/group9/weapons/plugins/WeaponFactoryTest.java
package io.group9.weapons.plugins;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import data.WorldProvider;
import io.group9.weapons.components.SwordComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import services.weapon.IWeapon;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class WeaponFactoryTest {

    private World mockWorld;
    private Body mockBody;
    private Fixture mockFixture;

    @BeforeEach
    void setUp() {
        mockWorld = mock(World.class);
        mockBody = mock(Body.class);
        mockFixture = mock(Fixture.class);

        when(mockWorld.createBody(any(BodyDef.class))).thenReturn(mockBody);
        when(mockBody.createFixture(any(FixtureDef.class))).thenReturn(mockFixture);
    }

    @Test
    void spawnWeapon_SetsFixtureUserData() {
        IWeapon customWeapon = new SwordComponent();

        try (MockedStatic<WorldProvider> wp = Mockito.mockStatic(WorldProvider.class)) {
            wp.when(WorldProvider::getWorld).thenReturn(mockWorld);

            // call static method
            WeaponFactory.spawnWeapon(customWeapon, 100f, 200f);
        }

        // verify a dynamic body was created
        verify(mockWorld).createBody(argThat(def -> def.type == BodyDef.BodyType.DynamicBody));
        // verify fixture user data is set to weapon
        verify(mockFixture).setUserData(customWeapon);
    }

    @Test
    void spawnSword_UsesSwordComponent() {
        try (MockedStatic<WorldProvider> wp = Mockito.mockStatic(WorldProvider.class)) {
            wp.when(WorldProvider::getWorld).thenReturn(mockWorld);

            WeaponFactory.spawnSword(50f, 75f);
        }

        verify(mockFixture).setUserData(argThat(data -> data instanceof SwordComponent));
    }
}
