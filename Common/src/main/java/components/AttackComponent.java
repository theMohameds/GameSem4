package components;

import com.badlogic.ashley.core.Component;

public class AttackComponent implements Component {
    // Whether an attack is currently active.
    public boolean attacking = false;
    // Timer for how long the attack hitbox remains active.
    public float attackDuration = 0.3f; // 300ms attack duration.
    public float attackTimer = 0f;
    // Optionally: damage value.
    public int damage = 10;
}
