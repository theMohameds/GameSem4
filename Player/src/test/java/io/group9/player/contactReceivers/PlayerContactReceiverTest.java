package io.group9.player.contactReceivers;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.badlogic.gdx.math.Vector2;
import io.group9.player.contactReceivers.PlayerContactReceiver;
import io.group9.player.components.PlayerComponent;
import locators.PlayerServiceLocator;
import services.player.IPlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PlayerContactReceiverTest {
    private PlayerContactReceiver receiver;
    private IPlayerService playerService;
    private Contact contact;
    private Fixture playerFixture, otherFixture;
    private Body playerBody;
    private PlayerComponent pc;

    @BeforeEach
    void setUp() throws Exception {
        playerService = mock(IPlayerService.class);
        Field instField = PlayerServiceLocator.class.getDeclaredField("instance");
        instField.setAccessible(true);
        instField.set(null, playerService);

        receiver = new PlayerContactReceiver();

        contact = mock(Contact.class);
        playerFixture = mock(Fixture.class);
        otherFixture = mock(Fixture.class);
        when(contact.getFixtureA()).thenReturn(playerFixture);
        when(contact.getFixtureB()).thenReturn(otherFixture);

        playerBody = mock(Body.class);
        when(playerFixture.getBody()).thenReturn(playerBody);
        when(otherFixture.getBody()).thenReturn(mock(Body.class));
        when(playerService.getPlayerBody()).thenReturn(playerBody);

        pc = new PlayerComponent();
        when(playerBody.getUserData()).thenReturn(pc);

        // Always return a non-null WorldManifold to avoid NPE
        WorldManifold defaultWm = mock(WorldManifold.class);
        when(contact.getWorldManifold()).thenReturn(defaultWm);
        when(defaultWm.getNormal()).thenReturn(new Vector2(0f, 1f));
    }

    @Test
    void beginContact_resetsJumps_onGroundCollision() {
        when(otherFixture.getUserData()).thenReturn("ground");
        receiver.beginContact(contact);
        assertEquals(pc.maxJumps, pc.jumpsLeft);
        assertFalse(pc.wallHanging);
        assertEquals(PlayerComponent.State.IDLE, pc.state);
    }

    @Test
    void beginContact_initiatesWallHang_onWallCollision() {
        when(otherFixture.getUserData()).thenReturn("wall");
        // Override manifold for wall
        WorldManifold wm = mock(WorldManifold.class);
        when(contact.getWorldManifold()).thenReturn(wm);
        when(wm.getNormal()).thenReturn(new Vector2(1f, 0f));

        pc.jumpsLeft = 1;
        pc.maxJumps = 2;

        receiver.beginContact(contact);

        assertTrue(pc.wallHanging);
        assertEquals(PlayerComponent.State.LAND_WALL, pc.state);
        assertTrue(pc.wallOnLeft);
    }

    @Test
    void endContact_doesNothing() {
        receiver.endContact(contact);
        assertEquals(PlayerComponent.State.IDLE, pc.state);
    }
}
