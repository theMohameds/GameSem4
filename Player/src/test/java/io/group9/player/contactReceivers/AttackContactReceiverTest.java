package io.group9.player.contactReceivers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Body;
import io.group9.player.contactReceivers.AttackContactReceiver;
import io.group9.player.components.PlayerComponent;
import locators.PlayerServiceLocator;
import services.player.IPlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AttackContactReceiverTest {
    private AttackContactReceiver receiver;
    private IPlayerService playerService;
    private Contact contact;
    private Fixture fixtureA, fixtureB;
    private Body otherBody, playerBody;
    private PlayerComponent pc;

    @BeforeEach
    void setUp() throws Exception {
        // Create mock player service
        playerService = mock(IPlayerService.class);
        // Inject into PlayerServiceLocator via reflection
        Field instField = PlayerServiceLocator.class.getDeclaredField("instance");
        instField.setAccessible(true);
        instField.set(null, playerService);

        receiver = new AttackContactReceiver();

        // Set up contact and fixtures
        contact = mock(Contact.class);
        fixtureA = mock(Fixture.class);
        fixtureB = mock(Fixture.class);
        when(contact.getFixtureA()).thenReturn(fixtureA);
        when(contact.getFixtureB()).thenReturn(fixtureB);

        otherBody = mock(Body.class);
        playerBody = mock(Body.class);
        when(fixtureA.getBody()).thenReturn(otherBody);
        when(fixtureB.getBody()).thenReturn(playerBody);
        when(playerService.getPlayerBody()).thenReturn(playerBody);

        // Prepare PlayerComponent on the player entity
        pc = new PlayerComponent();
        Entity playerEntity = new Entity();
        playerEntity.add(pc);
        when(playerService.getPlayerEntity()).thenReturn(playerEntity);
    }

    @Test
    void beginContact_appliesDamage_whenEnemyAttackHitsPlayer() {
        // Simulate enemy attack fixture
        when(fixtureA.getUserData()).thenReturn("enemyAttack");
        when(fixtureB.getUserData()).thenReturn(null);

        receiver.beginContact(contact);

        // Default health is 100, enemyAttack deals 10
        assertEquals(90, pc.health);
        verify(playerService).setHealth(90);
        assertEquals(PlayerComponent.State.HURT, pc.state);
        assertTrue(pc.isHurt);
    }

    @Test
    void beginContact_doesNotApplyDamage_whenNoEnemyAttack() {
        when(fixtureA.getUserData()).thenReturn(null);
        when(fixtureB.getUserData()).thenReturn(null);

        receiver.beginContact(contact);

        // No change
        assertEquals(100, pc.health);
        verify(playerService, never()).setHealth(anyInt());
    }

    @Test
    void beginContact_killsPlayer_whenHealthDropsToZero() {
        pc.health = 5;
        when(fixtureA.getUserData()).thenReturn("enemyAttack");
        when(fixtureB.getUserData()).thenReturn(null);

        receiver.beginContact(contact);

        // 5 - 10 = -5
        assertEquals(-5, pc.health);
        verify(playerService).setHealth(-5);
        assertEquals(PlayerComponent.State.DEAD, pc.state);
    }

    @Test
    void endContact_doesNothing() {
        receiver.endContact(contact);
        verifyNoInteractions(playerService);
    }
}
