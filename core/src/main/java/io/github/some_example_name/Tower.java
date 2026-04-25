package io.github.some_example_name;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Tower {
    public final Vector2 position;
    public float range;
    public float cooldown;
    public float cooldownTimer;
    public float angle;

    public Tower(float x, float y) {
        this.position = new Vector2(x, y);
        this.range = 250f;
        this.cooldown = 1.0f;
        this.cooldownTimer = 0f;
        this.angle = 0f;
    }

    public Bloon findTarget(Array<Bloon> bloons) {
        Bloon nearest = null;
        float nearestDist = range;

        for (int i = 0; i < bloons.size; i++) {
            Bloon b = bloons.get(i);
            if (!b.alive) continue;
            float dist = position.dst(b.position);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = b;
            }
        }
        if (nearest != null) {
            float dx = nearest.position.x - position.x;
            float dy = nearest.position.y - position.y;
            angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        }
        return nearest;
    }

    public boolean updateAndCanShoot(float delta) {
        cooldownTimer -= delta;
        if (cooldownTimer <= 0f) {
            cooldownTimer = cooldown;
            return true;
        }
        return false;
    }
}
