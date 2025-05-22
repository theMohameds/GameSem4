package io.group9.weapons.systems;

import com.badlogic.ashley.core.Engine;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import services.weapon.IWeapon;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WeaponRenderSystemTest {
    private WeaponRenderSystem system;
    private SpriteBatch mockBatch;

    @BeforeEach
    void setUp() throws Exception {
        system = new WeaponRenderSystem();
        mockBatch = mock(SpriteBatch.class);
        Field batchField = WeaponRenderSystem.class.getDeclaredField("batch");
        batchField.setAccessible(true);
        batchField.set(system, mockBatch);
    }

    @Test
    void update_WithNullCamera_DoesNothing() {
        MockedStatic<CameraServiceLocator> camLoc = Mockito.mockStatic(CameraServiceLocator.class);
        camLoc.when(CameraServiceLocator::get).thenReturn(null);

        system.update(0f);

        verify(mockBatch, never()).begin();
        verify(mockBatch, never()).end();
        camLoc.close();
    }

    @Test
    void update_WithNoWeapons_OnlyBeginEnd() {
        OrthographicCamera cam = new OrthographicCamera();
        cam.combined.idt();

        World mockWorld = mock(World.class);
        doAnswer(inv -> {
            Array<Body> bodies = inv.getArgument(0);
            bodies.clear();
            return null;
        }).when(mockWorld).getBodies(any(Array.class));

        MockedStatic<CameraServiceLocator> camLoc = Mockito.mockStatic(CameraServiceLocator.class);
        MockedStatic<WorldProvider> wp = Mockito.mockStatic(WorldProvider.class);
        camLoc.when(CameraServiceLocator::get).thenReturn(cam);
        wp.when(WorldProvider::getWorld).thenReturn(mockWorld);

        system.update(0f);

        verify(mockBatch).setProjectionMatrix(cam.combined);
        verify(mockBatch).begin();
        verify(mockBatch).end();
        verify(mockBatch, never()).draw(any(TextureRegion.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());

        camLoc.close();
        wp.close();
    }

    @Test
    void update_WithWeapon_DrawsSpriteAtBodyPosition() {
        OrthographicCamera cam = new OrthographicCamera();
        cam.combined.idt();

        World mockWorld = mock(World.class);
        Body mockBody = mock(Body.class);
        Fixture mockFixture = mock(Fixture.class);
        IWeapon mockWeapon = mock(IWeapon.class);
        TextureRegion region = mock(TextureRegion.class);

        when(region.getRegionWidth()).thenReturn(100);
        when(region.getRegionHeight()).thenReturn(200);
        when(mockWeapon.getSprite()).thenReturn(region);
        when(mockFixture.getUserData()).thenReturn(mockWeapon);
        when(mockBody.getPosition()).thenReturn(new Vector2(5f, 10f));
        when(mockBody.getFixtureList()).thenReturn(new Array<>(new Fixture[]{mockFixture}));

        doAnswer(inv -> {
            Array<Body> bodies = inv.getArgument(0);
            bodies.clear();
            bodies.add(mockBody);
            return null;
        }).when(mockWorld).getBodies(any(Array.class));

        MockedStatic<CameraServiceLocator> camLoc = Mockito.mockStatic(CameraServiceLocator.class);
        MockedStatic<WorldProvider> wp = Mockito.mockStatic(WorldProvider.class);
        camLoc.when(CameraServiceLocator::get).thenReturn(cam);
        wp.when(WorldProvider::getWorld).thenReturn(mockWorld);

        system.update(0f);

        camLoc.close();
        wp.close();

        float origW = 100f / CoreResources.PPM;
        float origH = 200f / CoreResources.PPM;
        float w = origW * 0.6f;
        float h = origH * 0.6f;
        float x = 5f - w / 2f;
        float y = 10f - h / 2f;

        verify(mockBatch).draw(region, x, y, w, h);
    }
}
