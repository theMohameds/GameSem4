package io.group9.weapons.components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SwordComponentTest {

    @Test
    void getName_ShouldReturnSword() {
        SwordComponent sword = new SwordComponent();
        assertEquals("Sword", sword.getName());
    }
}
