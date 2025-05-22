package io.group9.weapons.ContactReceivers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Contact;
import io.group9.weapons.ContactReceivers.WeaponPickupReceiver;
import io.group9.weapons.systems.BodyDestroySystem;
import locators.EnemyServiceLocator;
import locators.InventoryServiceLocator;
import locators.PlayerServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import services.enemy.IEnemyService;
import services.player.IPlayerService;
import services.weapon.IPickable;
import services.weapon.IInventoryService;
import services.weapon.IWeapon;

import static org.mockito.Mockito.*;

class WeaponPickupReceiverTest {

    private Fixture pickFx;
    private Fixture actorFx;
    private Contact contact;

    @BeforeEach
    void setUp() {
        pickFx = mock(Fixture.class);
        actorFx = mock(Fixture.class);
        contact = mock(Contact.class);
        when(contact.getFixtureA()).thenReturn(pickFx);
        when(contact.getFixtureB()).thenReturn(actorFx);
    }

    @Test
    void whenPlayerPicksUp_thenPickableOnPickUpInventoryAddAndBodyDestroy() {
        // Arrange player service
        IPlayerService playerSvc = mock(IPlayerService.class);
        Entity playerEntity = new Entity();
        Body playerBody = mock(Body.class);
        when(playerSvc.getPlayerBody()).thenReturn(playerBody);
        when(playerSvc.getPlayerEntity()).thenReturn(playerEntity);

        // Arrange enemy service
        IEnemyService enemySvc = mock(IEnemyService.class);

        // Arrange inventory service
        IInventoryService invSvc = mock(IInventoryService.class);

        // Pickable userdata
        IPickable pickable = mock(IPickable.class);
        when(pickFx.getUserData()).thenReturn(pickable);
        when(actorFx.getBody()).thenReturn(playerBody);

        try (MockedStatic<PlayerServiceLocator> mps = Mockito.mockStatic(PlayerServiceLocator.class);
             MockedStatic<EnemyServiceLocator> mes = Mockito.mockStatic(EnemyServiceLocator.class);
             MockedStatic<InventoryServiceLocator> mis = Mockito.mockStatic(InventoryServiceLocator.class);
             MockedStatic<BodyDestroySystem> mbs = Mockito.mockStatic(BodyDestroySystem.class)) {

            mps.when(PlayerServiceLocator::get).thenReturn(playerSvc);
            mes.when(EnemyServiceLocator::get).thenReturn(enemySvc);
            mis.when(InventoryServiceLocator::getInventoryService).thenReturn(invSvc);

            WeaponPickupReceiver receiver = new WeaponPickupReceiver();

            // Act
            receiver.beginContact(contact);

            // Assert
            verify(pickable).onPickUp(playerEntity);
            verify(invSvc).add(playerEntity, (IWeapon) pickable);
            mbs.verify(() -> BodyDestroySystem.schedule(pickFx.getBody()));
        }
    }
}
