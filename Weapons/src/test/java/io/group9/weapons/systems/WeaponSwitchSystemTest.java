package io.group9.weapons.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import locators.InventoryServiceLocator;
import locators.PlayerServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import services.weapon.IInventoryService;
import services.weapon.IWeapon;
import services.player.IPlayerService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class WeaponSwitchSystemTest {
    private Input originalInput;

    @BeforeEach
    void setUp() {
        // Save and replace Gdx.input
        originalInput = Gdx.input;
    }

    @AfterEach
    void tearDown() {
        // Restore original
        Gdx.input = originalInput;
    }

    @Test
    void pressingNum1WithNoCurrentWeapon_selectsSlotZero() {
        // Arrange services
        IPlayerService playerSvc = mock(IPlayerService.class);
        Entity player = new Entity();
        when(playerSvc.getPlayerEntity()).thenReturn(player);

        IInventoryService invSvc = mock(IInventoryService.class);
        IWeapon weapon = mock(IWeapon.class);
        List<IWeapon> list = Collections.singletonList(weapon);
        when(invSvc.getInventory(player)).thenReturn(list);
        when(invSvc.getCurrentWeapon(player)).thenReturn(Optional.empty());

        // Mock static locators
        try (MockedStatic<PlayerServiceLocator> mps = Mockito.mockStatic(PlayerServiceLocator.class);
             MockedStatic<InventoryServiceLocator> mis = Mockito.mockStatic(InventoryServiceLocator.class)) {
            mps.when(PlayerServiceLocator::get).thenReturn(playerSvc);
            mis.when(InventoryServiceLocator::getInventoryService).thenReturn(invSvc);

            // Stub input
            Input mockInput = mock(Input.class);
            when(mockInput.isKeyJustPressed(Input.Keys.NUM_1)).thenReturn(true);
            Gdx.input = mockInput;

            // Inject engine
            Engine engine = new Engine();
            WeaponSwitchSystem system = new WeaponSwitchSystem();
            system.addedToEngine(engine);

            // Act
            system.update(0f);

            // Assert: select slot 0
            verify(invSvc).selectSlot(player, 0);
        }
    }

    @Test
    void pressingNum1WithCurrentWeapon_selectsInvalidSlot() {
        // Arrange services
        IPlayerService playerSvc = mock(IPlayerService.class);
        Entity player = new Entity();
        when(playerSvc.getPlayerEntity()).thenReturn(player);

        IInventoryService invSvc = mock(IInventoryService.class);
        IWeapon weapon = mock(IWeapon.class);
        List<IWeapon> list = Collections.singletonList(weapon);
        when(invSvc.getInventory(player)).thenReturn(list);
        when(invSvc.getCurrentWeapon(player)).thenReturn(Optional.of(weapon));

        // Mock static locators
        try (MockedStatic<PlayerServiceLocator> mps = Mockito.mockStatic(PlayerServiceLocator.class);
             MockedStatic<InventoryServiceLocator> mis = Mockito.mockStatic(InventoryServiceLocator.class)) {
            mps.when(PlayerServiceLocator::get).thenReturn(playerSvc);
            mis.when(InventoryServiceLocator::getInventoryService).thenReturn(invSvc);

            // Stub input
            Input mockInput = mock(Input.class);
            when(mockInput.isKeyJustPressed(Input.Keys.NUM_1)).thenReturn(true);
            Gdx.input = mockInput;

            // Inject engine
            Engine engine = new Engine();
            WeaponSwitchSystem system = new WeaponSwitchSystem();
            system.addedToEngine(engine);

            // Act
            system.update(0f);

            // Assert: deselect (-1)
            verify(invSvc).selectSlot(player, -1);
        }
    }
}
