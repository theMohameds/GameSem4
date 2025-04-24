package components;

public class CollisionCategories {
    public static final short PLAYER = 0x0001;  // 0001
    public static final short ENEMY  = 0x0002;  // 0010
    public static final short ATTACK = 0x0004;  // 0100

    public static final short ENEMY_HURTBOX = 0x0008;
    // Optionally, add more, such as ground, projectiles, etc.
}
